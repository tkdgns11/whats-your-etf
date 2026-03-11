package com.d102.wye.data.remote.dto.response

import com.google.gson.annotations.SerializedName

// ─── 가격 이력 ────────────────────────────────────────────────────────────────

data class EtfPricePointDto(
    @SerializedName("date")        val date: String,
    @SerializedName("stockPrice")  val stockPrice: Long,
    @SerializedName("dailyReturn") val dailyReturn: Double
)

data class EtfPriceHistoryResponseDto(
    @SerializedName("content")       val content: List<EtfPricePointDto>,
    @SerializedName("pageNumber")    val pageNumber: Int,
    @SerializedName("pageSize")      val pageSize: Int,
    @SerializedName("totalElements") val totalElements: Int,
    @SerializedName("totalPages")    val totalPages: Int,
    @SerializedName("last")          val last: Boolean
)

// ─── 배당금 이력 ──────────────────────────────────────────────────────────────

data class EtfMonthlyDividendDto(
    @SerializedName("month")    val month: String,
    @SerializedName("dividend") val dividend: Long
)

data class EtfDividendHistoryResponseDto(
    @SerializedName("etfId")    val etfId: Long,
    @SerializedName("etfName")  val etfName: String,
    @SerializedName("dividends") val dividends: List<EtfMonthlyDividendDto>
)