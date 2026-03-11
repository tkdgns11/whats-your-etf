package com.whatsyouretf.userservice.domain.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 뉴스 목록 응답 DTO
 * <p>
 * 페이징 없이 최신 뉴스 20개를 반환합니다.
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
}
