package com.d102.wye.domain.model

data class Stock(
    val ticker: String,
    val name: String,
    val englishName: String,
    val tags: List<String>,            // ["KOSPI", "IT", "반도체"]
    val currentPrice: Long,
    val changeAmount: Long,
    val changeRate: Double,
    val marketCap: Long,
    val description: String,
    val containedEtfs: List<StockEtf>,
    val relatedStocks: List<RelatedStock>,
)

data class StockEtf(
    val ticker: String,
    val name: String,
    val manager: String,               // 삼성자산운용
    val weight: Double,                // 비중 (24.5%)
    val currentPrice: Long,
    val changeRate: Double,
    val netAsset: Long,
)

data class RelatedStock(
    val ticker: String,
    val name: String,
    val description: String,           // 동종 업계 (반도체) — relationType (industryName)
)
