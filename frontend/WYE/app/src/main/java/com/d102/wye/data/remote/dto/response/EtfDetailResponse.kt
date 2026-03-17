package com.d102.wye.data.remote.dto.response

import com.google.gson.annotations.SerializedName

// GET /api/v1/etfs/{ticker}
data class EtfDetailResponse(
    @SerializedName("ticker") val ticker: String,
    @SerializedName("name") val name: String,
    @SerializedName("currentPrice") val currentPrice: Long,
    @SerializedName("dailyFluctuation") val dailyFluctuation: Long,
    @SerializedName("dailyFluctuationRatio") val dailyFluctuationRatio: Double,
    @SerializedName("volume") val volume: Long,
    @SerializedName("company") val company: String,
    @SerializedName("riskGrade") val riskGrade: Int,
    @SerializedName("riskType") val riskType: String,
    @SerializedName("expenseRatio") val expenseRatio: Double,
    @SerializedName("per") val per: Double,
    @SerializedName("pbr") val pbr: Double,
    @SerializedName("roe") val roe: Double,
    @SerializedName("aum") val aum: Long,
    @SerializedName("listingDate") val listingDate: String,
    @SerializedName("inav") val inav: Long,
    @SerializedName("inavChangeAmount") val inavChangeAmount: Long,
    @SerializedName("inavChangeRate") val inavChangeRate: Double,
)

// GET /api/v1/etfs/{ticker}/price-history
data class EtfPriceHistoryPageResponse(
    @SerializedName("content") val content: List<EtfPricePointResponse>,
    @SerializedName("pageNumber") val pageNumber: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("totalElements") val totalElements: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("last") val last: Boolean,
)

data class EtfPricePointResponse(
    @SerializedName("date") val date: String,
    @SerializedName("stockPrice") val stockPrice: Long,
    @SerializedName("dailyReturn") val dailyReturn: Double,
    @SerializedName("nav") val nav: Long,
)
