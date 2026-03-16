package com.d102.wye.domain.model

data class PortfolioListItem(
    val portfolioId: Long,
    val title: String,
    val createdAt: String,   // "2026-03-15T16:32:02"
    val etfList: List<PortfolioEtf>,
    val totalReturn: Double  // %
)

data class PortfolioEtf(
    val ticker: String,
    val name: String
)