package com.d102.wye.data.remote.api

import com.d102.wye.data.remote.dto.response.BaseResponse
import com.d102.wye.data.remote.dto.response.NewsListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {

    /** 뉴스 목록을 조회한다. 카테고리를 넘기면 해당 카테고리만 조회한다. */
    @GET("news")
    suspend fun getNewsList(
        @Query("category") category: String? = null
    ): Response<BaseResponse<NewsListResponse>>
}
