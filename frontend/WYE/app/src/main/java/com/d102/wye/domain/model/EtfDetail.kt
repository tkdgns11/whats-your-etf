package com.d102.wye.domain.model

data class EtfDetail(
    val ticker: String,
    val name: String,
    val englishName: String,
    val riskLevel: Int,
    val currentPrice: Long,
    val iNav: Long,                         // 기준가(iNAV)
    val changeAmount: Long,
    val changeRate: Double,
    val iNavChangeAmount: Long,
    val iNavChangeRate: Double,
    val returnRate1M: Double,               // 수익률(1개월)
    val volume: Long,
    val sectors: List<EtfSector>,           // 클러스터 섹터 목록
    val influentialStocks: List<InfluentialStock>, // 영향 많은 종목
    val manager: String,                    // 운용사
    val volatility: String,                 // 변동성
    val expenseRatio: Double,               // 총보수
    val netAsset: Long,                     // 순자산
    val listedDate: String,                 // 상장일
)

data class EtfSector(
    val name: String,           // 반도체, 금융, 자동차 등
    val percentage: Double,     // 28.4
    val stocks: List<SectorStock>,
    val aiAnalysis: String,     // AI 분석 결과
)

data class SectorStock(
    val name: String,
    val percentage: Double,
)

data class InfluentialStock(
    val ticker: String,
    val name: String,
    val weight: Double,         // 비중 (24.50%)
    val currentPrice: Long,
    val changeRate: Double,
)

data class EtfReturnChart(
    val navData: List<ChartPoint>,
    val priceData: List<ChartPoint>,
    val kospiData: List<ChartPoint>,
    val sp500Data: List<ChartPoint>,
)

data class ChartPoint(
    val date: String,
    val value: Double,
)

data class EtfPeriodReturn(
    val nav1M: Double,   val nav3M: Double,   val nav6M: Double,
    val index1M: Double, val index3M: Double, val index6M: Double,
    val price1M: Double, val price3M: Double, val price6M: Double,
)
