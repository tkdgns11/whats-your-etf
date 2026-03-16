package com.d102.wye.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class PortfolioListItem(
    @SerializedName("portfolioId") val portfolioId: Long,
    @SerializedName("title")       val title: String,
    @SerializedName("createdAt")   val createdAt: String,
    @SerializedName("etfList")     val etfList: List<PortfolioEtf>,
    @SerializedName("totalReturn") val totalReturn: Double
)

data class PortfolioEtf(
    @SerializedName("ticker") val ticker: String,
    @SerializedName("name")   val name: String
)