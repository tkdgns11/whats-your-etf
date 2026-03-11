package com.d102.wye.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class NewsListResponse(
    @SerializedName("news")
    val news: List<NewsItemResponse>
)

data class NewsItemResponse(
    @SerializedName("id")
    val id: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("source")
    val source: String,
    @SerializedName("thumbnailUrl")
    val thumbnailUrl: String?,
    @SerializedName("categoryCode")
    val categoryCode: String,
    @SerializedName("categoryName")
    val categoryName: String,
    @SerializedName("publishedAt")
    val publishedAt: String
)
