package com.ape.smarthedge_risk_hub.ai;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * PDF 知识库：解析 → 切分 → 向量化 → 检索
 */
public interface KnowledgeBaseService {

    /** 知识库文档列表 */
    List<AiVo.KnowledgeDocVO> listDocs();

    /** 上传文档（支持 pdf / txt / md），返回文档数与切片数 */
    AiVo.OpResultVO upload(MultipartFile file, String category);

    /** 删除文档及其全部向量切片 */
    boolean delete(String docId);

    /** 清空并重建全部索引 */
    AiVo.OpResultVO rebuild();

    /** 向量检索，返回引用来源 */
    List<AiVo.CitationVO> search(String query, int topK);

    /** 知识库与模型状态 */
    AiVo.StatsVO stats();
}
