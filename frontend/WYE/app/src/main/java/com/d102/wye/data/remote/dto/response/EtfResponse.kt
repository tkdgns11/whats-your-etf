package com.d102.wye.data.remote.dto.response

data class EtfResponse(
    val ticker: String,
    val name: String,
    val currentPrice: Long,
    val changeRate: Double,
    val changeAmount: Long,
    val volume: Long,
    val riskLevel: Int,
    val investmentStrategy: String,
    val assetClass: String,
    val theme: String,
    val dividendRate: Double,
    val dividendCycle: String,
    val hasDerivative: Boolean,
    val per: Double,
    val pbr: Double,
    val roe: Double,
    val expenseRatio: Double,
    val netAsset: Long,
)
