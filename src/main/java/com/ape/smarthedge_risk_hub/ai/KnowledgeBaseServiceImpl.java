package com.ape.smarthedge_risk_hub.ai;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * PDF 知识库实现：
 * 解析（按页）→ TokenTextSplitter 切分 → bge-m3 向量化 → SimpleVectorStore 落盘。
 */
@Slf4j
@Service
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final VectorStore vectorStore;

    /** docId -> 文档元信息；docId -> 切片 ID */
    private final Map<String, AiVo.KnowledgeDocVO> docs = new ConcurrentHashMap<>();
    private final Map<String, List<String>> docChunks = new ConcurrentHashMap<>();

    /** MD5 内容判重：md5 -> docId；docId -> md5 */
    private final Map<String, String> md5ToDoc = new ConcurrentHashMap<>();
    private final Map<String, String> docToMd5 = new ConcurrentHashMap<>();

    private final AtomicBoolean indexing = new AtomicBoolean(false);
    private volatile Path knowledgePath;

    @Value("${smarthedge.ai.knowledge-dir:./knowledge}")
    private String knowledgeDir;

    @Value("${smarthedge.ai.vector-store-file:./data/vectorstore.json}")
    private String vectorStoreFile;

    @Value("${smarthedge.ai.top-k:6}")
    private int defaultTopK;

    @Value("${smarthedge.ai.similarity-threshold:0.25}")
    private double similarityThreshold;

    @Value("${spring.ai.ollama.chat.model:}")
    private String chatModel;

    @Value("${spring.ai.ollama.embedding.model:}")
    private String embeddingModel;

    public KnowledgeBaseServiceImpl(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /** 启动时准备知识库目录，必要时后台重建索引 */
    @PostConstruct
    public void init() {
        try {
            knowledgePath = resolveKnowledgeDir();
            scanDocs();
            File store = new File(vectorStoreFile);
            if (store.exists() && store.length() > 100 && fingerprint().equals(readFingerprint())) {
                ((SimpleVectorStore) vectorStore).load(store);
                for (AiVo.KnowledgeDocVO doc : docs.values()) {
                    File pdf = fileOf(doc);
                    List<Document> chunks = parse(new org.springframework.core.io.FileSystemResource(pdf),
                            doc.getDocId(), doc.getFileName(), doc.getCategory());
                    docChunks.put(doc.getDocId(), idsOf(chunks));
                    doc.setChunkCount(chunks.size());
                }
                refreshMd5Index();
                log.info("[知识库] 已从磁盘恢复：{} 篇文档 / {} 个切片", docs.size(), chunkCount());
                return;
            }
            log.info("[知识库] 索引缺失或文档有变化，开始后台重建");
            CompletableFuture.runAsync(this::rebuild);
        } catch (Exception e) {
            log.warn("[知识库] 初始化失败：{}", e.getMessage());
        }
    }

    // ==================================================================
    // 对外方法
    // ==================================================================
    @Override
    public List<AiVo.KnowledgeDocVO> listDocs() {
        List<AiVo.KnowledgeDocVO> list = new ArrayList<>(docs.values());
        list.sort(Comparator.comparing(AiVo.KnowledgeDocVO::getFileName));
        return list;
    }

    @Override
    public synchronized AiVo.OpResultVO upload(MultipartFile file, String category) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的文件");
        }
        String fileName = sanitize(file.getOriginalFilename());
        Path dir;
        try {
            dir = knowledgePath == null ? resolveKnowledgeDir() : knowledgePath;
        } catch (IOException e) {
            throw new RuntimeException("知识库目录创建失败：" + e.getMessage());
        }
        try {
            byte[] bytes = file.getBytes();

            // 1. MD5 内容判重：同一份内容只入库一次
            String md5 = md5(bytes);
            String existDocId = md5ToDoc.get(md5);
            if (existDocId != null && docs.containsKey(existDocId)) {
                log.info("[知识库] MD5 重复，跳过：{}", fileName);
                throw new IllegalArgumentException("该文件内容已存在（MD5 重复），无需重复上传");
            }

            // 2. 保存文件到知识库目录
            Files.createDirectories(dir);
            Path target = dir.resolve(fileName);
            Files.write(target, bytes);

            // 3. 解析切分并写入向量库
            String docId = docIdOf(fileName);
            String cat = (category == null || category.trim().isEmpty()) ? "未分类" : category.trim();
            List<Document> chunks = parse(new org.springframework.core.io.FileSystemResource(target.toFile()),
                    docId, fileName, cat);
            removeChunks(docId);
            vectorStore.add(chunks);
            docChunks.put(docId, idsOf(chunks));
            docs.put(docId, meta(target.toFile(), docId, cat, chunks.size()));

            // 4. 维护 MD5 索引（同名文件内容变更时清掉旧映射）
            String oldMd5 = docToMd5.remove(docId);
            if (oldMd5 != null) {
                md5ToDoc.remove(oldMd5);
            }
            md5ToDoc.put(md5, docId);
            docToMd5.put(docId, md5);

            persist();
            return new AiVo.OpResultVO(docs.size(), chunkCount());
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败：" + e.getMessage());
        }
    }

    @Override
    public synchronized boolean delete(String docId) {
        if (!docs.containsKey(docId)) {
            return false;
        }
        AiVo.KnowledgeDocVO vo = docs.remove(docId);
        removeChunks(docId);
        String md5 = docToMd5.remove(docId);
        if (md5 != null) {
            md5ToDoc.remove(md5);
        }
        File file = fileOf(vo);
        if (file.exists()) {
            file.delete();
        }
        persist();
        return true;
    }

    @Override
    public synchronized AiVo.OpResultVO rebuild() {
        if (!indexing.compareAndSet(false, true)) {
            return new AiVo.OpResultVO(docs.size(), chunkCount());
        }
        try {
            for (String docId : new ArrayList<>(docChunks.keySet())) {
                removeChunks(docId);
            }
            docs.clear();
            docChunks.clear();
            scanDocs();
            for (AiVo.KnowledgeDocVO doc : docs.values()) {
                File file = fileOf(doc);
                if (!file.exists()) {
                    continue;
                }
                List<Document> chunks = parse(new org.springframework.core.io.FileSystemResource(file),
                        doc.getDocId(), doc.getFileName(), doc.getCategory());
                if (chunks.isEmpty()) {
                    continue;
                }
                vectorStore.add(chunks);
                docChunks.put(doc.getDocId(), idsOf(chunks));
                doc.setChunkCount(chunks.size());
            }
            refreshMd5Index();
            persist();
            log.info("[知识库] 重建完成：{} 篇文档 / {} 个切片", docs.size(), chunkCount());
        } catch (Exception e) {
            log.error("[知识库] 重建失败", e);
        } finally {
            indexing.set(false);
        }
        return new AiVo.OpResultVO(docs.size(), chunkCount());
    }

    @Override
    public List<AiVo.CitationVO> search(String query, int topK) {
        List<AiVo.CitationVO> hits = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return hits;
        }
        int k = topK <= 0 ? defaultTopK : topK;
        try {
            List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                    .query(query)
                    .topK(k)
                    .similarityThreshold(similarityThreshold)
                    .build());
            if (documents == null) {
                return hits;
            }
            for (Document doc : documents) {
                Map<String, Object> meta = doc.getMetadata();
                AiVo.CitationVO hit = new AiVo.CitationVO();
                hit.setCategory(str(meta.get("category")));
                hit.setSource(str(meta.get("source")));
                hit.setChunkIndex(intVal(meta.get("chunkIndex")));
                Object distance = meta.get("distance");
                hit.setScore(distance == null ? null
                        : Math.round((1 - Double.parseDouble(distance.toString())) * 1000) / 1000.0);
                String text = doc.getText() == null ? "" : doc.getText().replaceAll("\\s+", " ").trim();
                hit.setSnippet(text.length() > 220 ? text.substring(0, 220) + "…" : text);
                hits.add(hit);
            }
        } catch (Exception e) {
            log.warn("[知识库] 检索失败：{}", e.getMessage());
        }
        return hits;
    }

    @Override
    public AiVo.StatsVO stats() {
        AiVo.StatsVO vo = new AiVo.StatsVO();
        vo.setDocumentCount(docs.size());
        vo.setChunkCount(chunkCount());
        vo.setSimilarityThreshold(similarityThreshold);
        vo.setChatModel(chatModel);
        vo.setEmbeddingModel(embeddingModel);
        return vo;
    }

    // ==================================================================
    // 内部实现
    // ==================================================================
    private Path resolveKnowledgeDir() throws IOException {
        Path dir = Paths.get(knowledgeDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        boolean hasFile;
        try (Stream<Path> stream = Files.list(dir)) {
            hasFile = stream.anyMatch(p -> isSupported(p.getFileName().toString()));
        }
        if (!hasFile) {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath*:knowledge/*.pdf");
            int copied = 0;
            for (Resource resource : resources) {
                String name = resource.getFilename();
                if (name == null) {
                    continue;
                }
                try (InputStream in = resource.getInputStream()) {
                    Files.copy(in, dir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
                    copied++;
                }
            }
            log.info("[知识库] 已从 classpath 释放 {} 个内置 PDF 到 {}", copied, dir);
        }
        return dir;
    }

    private void scanDocs() throws IOException {
        Path dir = knowledgePath == null ? resolveKnowledgeDir() : knowledgePath;
        List<Path> files;
        try (Stream<Path> stream = Files.list(dir)) {
            files = stream.filter(p -> isSupported(p.getFileName().toString()))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .collect(Collectors.toList());
        }
        for (Path p : files) {
            String docId = docIdOf(p.getFileName().toString());
            if (docs.containsKey(docId)) {
                continue;
            }
            docs.put(docId, meta(p.toFile(), docId, "内置知识库", 0));
        }
        log.info("[知识库] 扫描到 {} 个文档，目录：{}", files.size(), dir);
    }

    private AiVo.KnowledgeDocVO meta(File file, String docId, String category, int chunkCount) {
        AiVo.KnowledgeDocVO vo = new AiVo.KnowledgeDocVO();
        vo.setDocId(docId);
        vo.setFileName(file.getName());
        vo.setCategory(category);
        vo.setChunkCount(chunkCount);
        vo.setSizeBytes(file.length());
        vo.setImportedAt(LocalDateTime.now().format(FMT));
        return vo;
    }

    /** 解析并切分：PDF 按页解析，txt/md 直接读取 */
    private List<Document> parse(Resource resource, String docId, String source, String category) {
        List<Document> result = new ArrayList<>();
        TokenTextSplitter splitter = new TokenTextSplitter(450, 120, 5, 2000, true);
        try {
            String name = resource.getFilename() == null ? "" : resource.getFilename().toLowerCase();
            List<Document> raw = new ArrayList<>();
            if (name.endsWith(".pdf")) {
                PagePdfDocumentReader reader = new PagePdfDocumentReader(resource,
                        PdfDocumentReaderConfig.builder()
                                .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())
                                .withPagesPerDocument(1)
                                .build());
                raw.addAll(reader.read());
            } else {
                String text = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                raw.add(new Document(text));
            }
            int page = 1;
            int index = 0;
            for (Document doc : raw) {
                List<Document> parts = splitter.apply(java.util.Collections.singletonList(doc));
                for (Document part : parts) {
                    String text = part.getText();
                    if (text == null || text.trim().length() < 20) {
                        continue;
                    }
                    Map<String, Object> meta = new HashMap<>();
                    meta.put("docId", docId);
                    meta.put("source", source);
                    meta.put("category", category);
                    meta.put("chunkIndex", index++);
                    result.add(new Document(docId + "-c" + index, text, meta));
                }
                page++;
            }
        } catch (Exception e) {
            log.warn("[知识库] 解析失败 {}：{}", source, e.getMessage());
        }
        return result;
    }

    private void removeChunks(String docId) {
        List<String> ids = docChunks.remove(docId);
        if (ids != null && !ids.isEmpty()) {
            try {
                vectorStore.delete(ids);
            } catch (Exception e) {
                log.warn("[知识库] 删除切片失败：{}", e.getMessage());
            }
        }
    }

    private void persist() {
        try {
            File file = new File(vectorStoreFile);
            if (file.getParentFile() != null) {
                Files.createDirectories(file.getParentFile().toPath());
            }
            ((SimpleVectorStore) vectorStore).save(file);
            Files.write(new File(vectorStoreFile + ".fingerprint").toPath(),
                    fingerprint().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.warn("[知识库] 持久化失败：{}", e.getMessage());
        }
    }

    private String fingerprint() {
        StringBuilder sb = new StringBuilder();
        for (AiVo.KnowledgeDocVO doc : docs.values()) {
            File f = fileOf(doc);
            sb.append(doc.getFileName()).append('|').append(f.length()).append('|').append(f.lastModified()).append('\n');
        }
        return Integer.toHexString(sb.toString().hashCode());
    }

    private String readFingerprint() {
        try {
            File fp = new File(vectorStoreFile + ".fingerprint");
            return fp.exists() ? new String(Files.readAllBytes(fp.toPath()), StandardCharsets.UTF_8).trim() : "";
        } catch (IOException e) {
            return "";
        }
    }

    private File fileOf(AiVo.KnowledgeDocVO doc) {
        Path dir = knowledgePath == null ? Paths.get(knowledgeDir).toAbsolutePath().normalize() : knowledgePath;
        return dir.resolve(doc.getFileName()).toFile();
    }

    private List<String> idsOf(List<Document> documents) {
        return documents.stream().map(Document::getId).collect(Collectors.toList());
    }

    private int chunkCount() {
        return docChunks.values().stream().mapToInt(List::size).sum();
    }

    private boolean isSupported(String name) {
        String n = name.toLowerCase();
        return n.endsWith(".pdf") || n.endsWith(".txt") || n.endsWith(".md");
    }

    /** 计算字节内容的 MD5（32 位小写十六进制） */
    private static String md5(byte[] bytes) {
        try {
            byte[] hash = MessageDigest.getInstance("MD5").digest(bytes);
            StringBuilder sb = new StringBuilder(32);
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("MD5 计算失败", e);
        }
    }

    /** 依据知识库目录下的文件重建 MD5 索引 */
    private void refreshMd5Index() {
        md5ToDoc.clear();
        docToMd5.clear();
        for (AiVo.KnowledgeDocVO doc : docs.values()) {
            File file = fileOf(doc);
            if (!file.exists()) {
                continue;
            }
            try {
                String md5 = md5(Files.readAllBytes(file.toPath()));
                md5ToDoc.put(md5, doc.getDocId());
                docToMd5.put(doc.getDocId(), md5);
            } catch (IOException e) {
                log.warn("[知识库] MD5 计算失败 {}：{}", doc.getFileName(), e.getMessage());
            }
        }
    }

    private String docIdOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    private String sanitize(String fileName) {
        String name = fileName == null ? "document.pdf" : fileName.replace("\\", "/");
        int idx = name.lastIndexOf('/');
        if (idx >= 0) {
            name = name.substring(idx + 1);
        }
        name = name.replaceAll("[\\\\/:*?\"<>|]", "_");
        return name.isEmpty() ? "document.pdf" : name;
    }

    private String str(Object o) {
        return o == null ? "" : o.toString();
    }

    private Integer intVal(Object o) {
        try {
            return o == null ? null : Integer.valueOf(o.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 供调试：可用文件类型 */
    public List<String> supportedTypes() {
        return Arrays.asList("pdf", "txt", "md");
    }
}
