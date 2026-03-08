package com.whatsyouretf.userservice.domain.news.dto;

import com.whatsyouretf.userservice.domain.news.entity.NewsArticle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 뉴스 상세 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsDetailResponse {

    /** 뉴스 ID */
    private Long id;

    /** 뉴스 제목 */
    private String title;

    /** 뉴스 본문 */
    private String content;

    /** 언론사명 */
    private String source;

    /** 원본 URL */
    private String sourceUrl;

    /** 썸네일 URL */
    private String thumbnailUrl;

    /** 카테고리 코드 */
    private String categoryCode;

    /** 카테고리명 */
    private String categoryName;

    /** 발행일시 */
    private LocalDateTime publishedAt;

    /** 관련 종목 목록 */
    private List<RelatedStockResponse> relatedStocks;

    /** 관련 ETF 목록 (종목을 포함하는 ETF) */
    private List<RelatedEtfResponse> relatedEtfs;

    /**
     * Entity -> DTO 변환 (관련 종목 포함)
     */
    public static NewsDetailResponse from(
            NewsArticle article,
            List<RelatedStockResponse> relatedStocks,
            List<RelatedEtfResponse> relatedEtfs
    ) {
        return NewsDetailResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .source(article.getSource())
                .sourceUrl(article.getSourceUrl())
                .thumbnailUrl(article.getThumbnailUrl())
                .categoryCode(article.getCategory() != null ? article.getCategory().getCode() : null)
                .categoryName(article.getCategory() != null ? article.getCategory().getName() : null)
                .publishedAt(article.getPublishedAt())
                .relatedStocks(relatedStocks)
                .relatedEtfs(relatedEtfs)
                .build();
    }
}
