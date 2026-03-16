package com.d102.wye.data.remote.api

import com.d102.wye.data.remote.dto.request.SavePortfolioRequest
import com.d102.wye.data.remote.dto.response.BaseResponse
import com.d102.wye.data.remote.dto.response.PortfolioListItem
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PortfolioApiService {

    /**
     * 포트폴리오 저장
     */
    @POST("portfolios")
    suspend fun savePortfolio(
        @Body request: SavePortfolioRequest
    ): BaseResponse<Unit>

    @GET("portfolios")
    suspend fun getPortfolioList(): BaseResponse<List<PortfolioListItem>>

    @DELETE("portfolios/{portfolioId}")
    suspend fun deletePortfolio(
        @Path("portfolioId") portfolioId: Long
    ): BaseResponse<Unit>
}