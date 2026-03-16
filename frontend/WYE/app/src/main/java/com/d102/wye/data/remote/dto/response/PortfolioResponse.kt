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