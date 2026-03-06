package com.d102.wye.data.remote.api

import com.d102.wye.data.remote.dto.response.BaseResponse
import com.d102.wye.data.remote.dto.response.EtfDetailResponse
import com.d102.wye.data.remote.dto.response.EtfPeriodReturnResponse
import com.d102.wye.data.remote.dto.response.EtfResponse
import com.d102.wye.data.remote.dto.response.EtfReturnChartResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface EtfApiService {

    // ETF 전체 목록 (1분 polling)
    @GET("api/etf/list")
    suspend fun getEtfList(): Response<BaseResponse<List<EtfResponse>>>

    // 관심 ETF 토글
    @POST("api/etf/{ticker}/like")
    suspend fun toggleLike(
        @Path("ticker") ticker: String
    ): Response<BaseResponse<Boolean>>

    // ETF 상세
    @GET("api/etf/{ticker}")
    suspend fun getEtfDetail(
        @Path("ticker") ticker: String
    ): Response<BaseResponse<EtfDetailResponse>>

    // 수익률 그래프
    @GET("api/etf/{ticker}/chart")
    suspend fun getEtfReturnChart(
        @Path("ticker") ticker: String,
        @Query("period") period: String   // "1W" / "1M" / "3M" / "1Y" / "3Y" / "ALL"
    ): Response<BaseResponse<EtfReturnChartResponse>>

    // 기간별 수익률 표
    @GET("api/etf/{ticker}/return")
    suspend fun getEtfPeriodReturn(
        @Path("ticker") ticker: String
    ): Response<BaseResponse<EtfPeriodReturnResponse>>
}

//api 명세 작성되면 다시