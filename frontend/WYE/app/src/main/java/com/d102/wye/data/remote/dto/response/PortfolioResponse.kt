package com.d102.wye.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class PortfolioListItemDto(
    @SerializedName("portfolioId") val portfolioId: Long,
    @SerializedName("title")       val title: String,
    @SerializedName("createdAt")   val createdAt: String,
    @SerializedName("etfList")     val etfList: List<PortfolioEtfDto>,
    @SerializedName("totalReturn") val totalReturn: Double
)

data class PortfolioEtfDto(
    @SerializedName("ticker") val ticker: String,
    @SerializedName("name")   val name: String
)

// GET /api/v1/portfolios/{portfolioId}/issues
data class PortfolioIssueDto(
    @SerializedName("localDate")    val localDate: String,
    @SerializedName("title")        val title: String,
    @SerializedName("description")  val description: String
)