package com.d102.wye.domain.model

data class News(
    val id: Long,
    val title: String,
    val source: String,
    val thumbnailUrl: String?,
    val categoryCode: String,
    val categoryName: String,
    val publishedAt: String
)
