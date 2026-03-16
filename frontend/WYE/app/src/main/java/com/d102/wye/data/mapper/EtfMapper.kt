package com.d102.wye.data.mapper

import com.d102.wye.data.remote.dto.response.EtfDetailResponse
import com.d102.wye.data.remote.dto.response.EtfPeriodReturnResponse
import com.d102.wye.data.remote.dto.response.EtfResponse
import com.d102.wye.data.remote.dto.response.EtfReturnChartResponse
import com.d102.wye.data.remote.dto.response.EtfSectorResponse
import com.d102.wye.data.remote.dto.response.InfluentialStockResponse
import com.d102.wye.domain.model.ChartPoint
import com.d102.wye.domain.model.Etf
import com.d102.wye.domain.model.EtfDetail
import com.d102.wye.domain.model.EtfPeriodReturn
import com.d102.wye.domain.model.EtfReturnChart
import com.d102.wye.domain.model.EtfSector
import com.d102.wye.domain.model.InfluentialStock
import com.d102.wye.domain.model.SectorStock

fun EtfResponse.toDomain() = Etf(
    ticker = ticker,
    name = name,
    currentPrice = currentPrice,
    changeRate = changeRate,
    changeAmount = changeAmount,
    volume = volume,
    riskLevel = riskLevel,
    investmentStrategy = investmentStrategy,
    assetClass = assetClass,
    theme = theme,
    dividendRate = dividendRate,
    dividendCycle = dividendCycle,
    hasDerivative = hasDerivative,
    per = per,
    pbr = pbr,
    roe = roe,
    expenseRatio = expenseRatio,
    netAsset = netAsset,
)

fun EtfDetailResponse.toDomain() = EtfDetail(
    ticker = ticker,
    name = name,
    englishName = englishName,
    riskLevel = riskLevel,
    currentPrice = currentPrice,
    iNav = iNav,
    changeAmount = changeAmount,
    changeRate = changeRate,
    iNavChangeAmount = iNavChangeAmount,
    iNavChangeRate = iNavChangeRate,
    returnRate1M = returnRate1M,
    volume = volume,
    sectors = sectors.map { it.toDomain() },
    influentialStocks = influentialStocks.map { it.toDomain() },
    manager = manager,
    volatility = volatility,
    expenseRatio = expenseRatio,
    netAsset = netAsset,
    listedDate = listedDate,
)

fun EtfSectorResponse.toDomain() = EtfSector(
    name = name,
    percentage = percentage,
    stocks = stocks.map { SectorStock(it.name, it.percentage) },
    aiAnalysis = aiAnalysis,
)

fun InfluentialStockResponse.toDomain() = InfluentialStock(
    ticker = ticker,
    name = name,
    weight = weight,
    currentPrice = currentPrice,
    changeRate = changeRate,
)

fun EtfReturnChartResponse.toDomain() = EtfReturnChart(
    navData = navData.map { ChartPoint(it.date, it.value) },
    priceData = priceData.map { ChartPoint(it.date, it.value) },
    kospiData = kospiData.map { ChartPoint(it.date, it.value) },
    sp500Data = sp500Data.map { ChartPoint(it.date, it.value) },
)

fun EtfPeriodReturnResponse.toDomain() = EtfPeriodReturn(
    nav1M = nav1M, nav3M = nav3M, nav6M = nav6M,
    index1M = index1M, index3M = index3M, index6M = index6M,
    price1M = price1M, price3M = price3M, price6M = price6M,
)
