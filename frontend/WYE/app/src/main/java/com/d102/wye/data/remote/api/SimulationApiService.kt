package com.d102.wye.data.remote.api

import com.d102.wye.data.remote.dto.response.EtfDividendHistoryResponseDto
import com.d102.wye.data.remote.dto.response.EtfPriceHistoryResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SimulationApiService {

    /**
     * ETF 가격 이력 조회
     * GET /etfs/{ticker}/priceHistory
     */
    @GET("etfs/{ticker}/priceHistory")
    suspend fun getEtfPriceHistory(
        @Path("ticker")              ticker: String,
        @Query("startDate")          startDate: String? = null,
        @Query("endDate")            endDate: String? = null,
        @Query("page")               page: Int = 0
    ): EtfPriceHistoryResponseDto

    /**
     * ETF 월별 배당금 이력 조회
     * GET /etfs/{ticker}/dividends
     */
    @GET("etfs/{ticker}/dividends")
    suspend fun getEtfDividendHistory(
        @Path("ticker")     ticker: String,
        @Query("startDate") startDate: String? = null,
        @Query("endDate")   endDate: String? = null
    ): EtfDividendHistoryResponseDto
}