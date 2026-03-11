package com.d102.wye.data.mapper

import com.d102.wye.data.remote.dto.response.NewsItemResponse
import com.d102.wye.data.remote.dto.response.NewsListResponse
import com.d102.wye.domain.model.News

/** 서버 뉴스 아이템 DTO를 앱에서 사용하는 도메인 모델로 변환한다. */
fun NewsItemResponse.toDomain() = News(
    id = id,
    title = title,
    source = source,
    thumbnailUrl = thumbnailUrl,
    categoryCode = categoryCode,
    categoryName = categoryName,
    publishedAt = publishedAt
)

/** 서버 뉴스 목록 응답을 도메인 뉴스 리스트로 변환한다. */
fun NewsListResponse.toDomain(): List<News> = news.map { it.toDomain() }
