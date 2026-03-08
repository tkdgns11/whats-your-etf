package com.whatsyouretf.userservice.domain.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 종목 관련 뉴스 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockNewsResponse {

    /** 종목 정보 */
    private StockInfo stock;

    /** 뉴스 목록 */
    private List<StockNewsItem> news;

    /** 전체 뉴스 개수 */
    private long totalCount;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StockInfo {
        private String stockCode;
        private String companyName;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StockNewsItem {
        /** 뉴스 ID */
        private Long id;

        /** 뉴스 제목 */
        private String title;

        /** 언론사명 */
        private String source;

        /** 발행일시 */
        private LocalDateTime publishedAt;
    }
}
