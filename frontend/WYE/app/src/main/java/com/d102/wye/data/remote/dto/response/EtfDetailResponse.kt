package com.d102.wye.data.remote.dto.response

data class EtfDetailResponse(
    val ticker: String,
    val name: String,
    val englishName: String,
    val riskLevel: Int,
    val currentPrice: Long,
    val iNav: Long,
    val changeAmount: Long,
    val changeRate: Double,
    val iNavChangeAmount: Long,
    val iNavChangeRate: Double,
    val returnRate1M: Double,
    val volume: Long,
    val sectors: List<EtfSectorResponse>,
    val influentialStocks: List<InfluentialStockResponse>,
    val manager: String,
    val volatility: String,
    val expenseRatio: Double,
    val netAsset: Long,
    val listedDate: String,
)

data class EtfSectorResponse(
    val name: String,
    val percentage: Double,
    val stocks: List<SectorStockResponse>,
    val aiAnalysis: String,
)

data class SectorStockResponse(
    val name: String,
    val percentage: Double,
)

data class InfluentialStockResponse(
    val ticker: String,
    val name: String,
    val weight: Double,
    val currentPrice: Long,
    val changeRate: Double,
)

data class EtfReturnChartResponse(
    val navData: List<ChartPointResponse>,
    val priceData: List<ChartPointResponse>,
    val kospiData: List<ChartPointResponse>,
    val sp500Data: List<ChartPointResponse>,
)

data class ChartPointResponse(
    val date: String,
    val value: Double,
)

data class EtfPeriodReturnResponse(
    val nav1M: Double,   val nav3M: Double,   val nav6M: Double,
    val index1M: Double, val index3M: Double, val index6M: Double,
    val price1M: Double, val price3M: Double, val price6M: Double,
)
