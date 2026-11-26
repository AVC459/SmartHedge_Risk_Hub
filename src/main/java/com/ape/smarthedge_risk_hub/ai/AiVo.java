package com.ape.smarthedge_risk_hub.ai;

import lombok.Data;

import java.util.List;

/**
 * 智能问答模块的视图对象（与前端 aiKnowledge.js 的字段约定保持一致）
 */
public class AiVo {

    /** 知识库文档 */
    @Data
    public static class KnowledgeDocVO {
        private String docId;
        private String fileName;
        private String category;
        private Integer chunkCount;
        private Long sizeBytes;
        private String importedAt;
    }

    /** 引用来源片段 */
    @Data
    public static class CitationVO {
        private String category;
        private String source;
        private Integer chunkIndex;
        private Double score;
        private String snippet;
    }

    /** 知识库统计 */
    @Data
    public static class StatsVO {
        private Integer documentCount;
        private Integer chunkCount;
        private Double similarityThreshold;
        private String chatModel;
        private String embeddingModel;
        private Boolean ollamaConnected;
    }

    /** 提问请求 */
    @Data
    public static class AskRequest {
        private String question;
    }

    /** 上传/重建结果 */
    @Data
    public static class OpResultVO {
        private Integer documentCount;
        private Integer chunkCount;

        public OpResultVO(Integer documentCount, Integer chunkCount) {
            this.documentCount = documentCount;
            this.chunkCount = chunkCount;
        }
    }

    /** SSE 事件载荷：sources / done */
    @Data
    public static class SourcesEvent {
        private List<CitationVO> citations;

        public SourcesEvent(List<CitationVO> citations) {
            this.citations = citations;
        }
    }

    @Data
    public static class DoneEvent {
        private Long elapsedMs;

        public DoneEvent(Long elapsedMs) {
            this.elapsedMs = elapsedMs;
        }
    }
}
