package com.d102.wye.data.remote.dto.response

import com.google.gson.annotations.SerializedName

// GET /api/v1/stocks/{ticker}/related
data class RelatedStockResponse(
    @SerializedName("ticker") val ticker: String,
    @SerializedName("companyName") val companyName: String,
    @SerializedName("industryCode") val industryCode: String,
    @SerializedName("industryName") val industryName: String,
    @SerializedName("relationType") val relationType: String,
)
