package com.whatsyouretf.userservice.domain.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 뉴스 목록 응답 DTO (페이징 지원)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsPageResponse {

    /** 뉴스 목록 */
    private List<NewsListResponse> news;

    /** 검색 키워드 (검색 시에만) */
    private String keyword;

    /** 현재 페이지 (1부터 시작) */
    private int page;

    /** 페이지 크기 */
    private int size;

    /** 전체 요소 수 */
    private long totalElements;

    /** 전체 페이지 수 */
    private int totalPages;

    /** 마지막 페이지 여부 */
    private boolean last;
}
