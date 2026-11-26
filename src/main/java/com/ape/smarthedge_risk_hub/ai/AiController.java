package com.ape.smarthedge_risk_hub.ai;

import com.ape.smarthedge_risk_hub.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

/**
 * 智能问答接口（与主应用同端口，无需跨域）：
 * <ul>
 *   <li>POST /api/ai/ask/stream —— SSE 流式问答（sources → token → done）</li>
 *   <li>GET  /api/ai/kb/stats   —— 知识库统计</li>
 *   <li>GET  /api/ai/kb/list    —— 文档列表</li>
 *   <li>POST /api/ai/kb/upload  —— 上传文档并向量化</li>
 *   <li>DELETE /api/ai/kb/{docId} —— 删除文档</li>
 *   <li>POST /api/ai/kb/rebuild —— 重建索引</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    /** 会话标识：同一浏览器会话内保持上下文 */
    private static final String CONVERSATION_ID = "smart-hedge-cs";

    private final KnowledgeBaseService knowledgeBaseService;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${smarthedge.ai.top-k:6}")
    private int topK;

    public AiController(KnowledgeBaseService knowledgeBaseService, ChatClient chatClient) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.chatClient = chatClient;
    }

    /** 知识库统计 */
    @GetMapping("/kb/stats")
    public Result<AiVo.StatsVO> stats() {
        try {
            return Result.success(knowledgeBaseService.stats());
        } catch (Exception e) {
            return Result.fail(500, "查询失败：" + e.getMessage());
        }
    }

    /** 知识库文档列表 */
    @GetMapping("/kb/list")
    public Result<List<AiVo.KnowledgeDocVO>> list() {
        try {
            List<AiVo.KnowledgeDocVO> docs = knowledgeBaseService.listDocs();
            return Result.success(docs, docs.size());
        } catch (Exception e) {
            return Result.fail(500, "查询失败：" + e.getMessage());
        }
    }

    /** 上传文档并入库 */
    @PostMapping("/kb/upload")
    public Result<AiVo.OpResultVO> upload(@RequestParam("file") MultipartFile file,
                                          @RequestParam(value = "category", required = false) String category) {
        try {
            return Result.success(knowledgeBaseService.upload(file, category));
        } catch (Exception e) {
            return Result.fail(500, "导入失败：" + e.getMessage());
        }
    }

    /** 删除文档 */
    @DeleteMapping("/kb/{docId}")
    public Result<String> delete(@PathVariable("docId") String docId) {
        boolean ok = knowledgeBaseService.delete(docId);
        return ok ? Result.success("已删除") : Result.fail(404, "文档不存在：" + docId);
    }

    /** 重建索引 */
    @PostMapping("/kb/rebuild")
    public Result<AiVo.OpResultVO> rebuild() {
        try {
            return Result.success(knowledgeBaseService.rebuild());
        } catch (Exception e) {
            return Result.fail(500, "重建失败：" + e.getMessage());
        }
    }

    /**
     * 流式问答：先推送引用来源，再逐段推送回答 token，最后推送耗时。
     */
    @PostMapping(value = "/ask/stream", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter askStream(@RequestBody AiVo.AskRequest request) {
        SseEmitter emitter = new SseEmitter(300000L);
        String question = request == null || request.getQuestion() == null ? "" : request.getQuestion().trim();
        if (question.isEmpty()) {
            safeSend(emitter, "error", json("请输入问题"));
            emitter.complete();
            return emitter;
        }

        long start = System.currentTimeMillis();

        // 1. 检索知识库，先推送引用来源
        List<AiVo.CitationVO> citations = knowledgeBaseService.search(question, topK);
        StringBuilder context = new StringBuilder();
        for (AiVo.CitationVO c : citations) {
            context.append("[").append(c.getSource()).append(" 第").append(c.getChunkIndex()).append("段] ")
                    .append(c.getSnippet()).append('\n');
        }
        safeSend(emitter, "sources", json(new AiVo.SourcesEvent(citations)));

        // 2. 交给本地 Ollama 模型生成，逐 token 推送
        String userPrompt = "【知识库参考信息】\n"
                + (context.length() == 0 ? "（未检索到相关片段）" : context.toString())
                + "【用户问题】\n" + question;

        AtomicBoolean finished = new AtomicBoolean(false);
        Disposable disposable;
        try {
            disposable = chatClient.prompt()
                    .user(userPrompt)
                    .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, CONVERSATION_ID))
                    .stream()
                    .content()
                    .subscribe(
                            token -> safeSend(emitter, "token", json(token)),
                            error -> {
                                safeSend(emitter, "error", json("生成失败：" + error.getMessage()));
                                if (finished.compareAndSet(false, true)) {
                                    emitter.complete();
                                }
                            },
                            () -> {
                                safeSend(emitter, "done", json(new AiVo.DoneEvent(System.currentTimeMillis() - start)));
                                if (finished.compareAndSet(false, true)) {
                                    emitter.complete();
                                }
                            });
        } catch (Exception e) {
            safeSend(emitter, "error", json("生成失败：" + e.getMessage()));
            emitter.complete();
            return emitter;
        }

        emitter.onTimeout(() -> {
            if (finished.compareAndSet(false, true)) {
                disposable.dispose();
                emitter.complete();
            }
        });
        emitter.onError(e -> disposable.dispose());
        return emitter;
    }

    /** 发送 SSE 事件，客户端断开时忽略异常 */
    private void safeSend(SseEmitter emitter, String event, String data) {
        try {
            emitter.send(SseEmitter.event().name(event).data(data));
        } catch (IOException | IllegalStateException e) {
            // 客户端已断开
        }
    }

    /** SSE 数据统一 JSON 编码，保证换行、引号等字符不破坏帧结构 */
    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "\"\"";
        }
    }

    /** 健康检查 */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("AI service ready " + MediaType.TEXT_EVENT_STREAM_VALUE);
    }
}
