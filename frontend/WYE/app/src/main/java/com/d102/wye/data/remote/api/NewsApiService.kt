package com.d102.wye.data.remote.api

import com.d102.wye.data.remote.dto.response.BaseResponse
import com.d102.wye.data.remote.dto.response.NewsDetailResponse
import com.d102.wye.data.remote.dto.response.NewsListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NewsApiService {

    /**
     * 뉴스 목록을 조회 (카테고리를 넘기면 해당 카테고리만 조회)
     * */
    @GET("news")
    suspend fun getNewsList(
        @Query("category") category: String? = null
    ): Response<BaseResponse<NewsListResponse>>

    /**
     * 뉴스 상세 조회
     * */
    @GET("news/{newsId}")
    suspend fun getNewsDetail(
        @Path("newsId") newsId: Long
    ): Response<BaseResponse<NewsDetailResponse>>
}
