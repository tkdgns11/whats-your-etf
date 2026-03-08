package com.whatsyouretf.userservice.domain.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 뉴스 페이지 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsPageResponse {

    /** 뉴스 목록 */
    private List<NewsListResponse> news;

    /** 현재 페이지 번호 */
    private int page;

    /** 전체 페이지 수 */
    private int totalPages;

    /** 전체 요소 수 */
    private long totalElements;

    /** 검색 키워드 (검색 시에만) */
    private String keyword;
}
