package com.d102.wye.data.mapper

import com.d102.wye.data.local.entity.LikedEtfEntity
import com.d102.wye.domain.model.Etf

fun LikedEtfEntity.toDomain(): Etf = Etf(
    ticker = ticker,
    name = name,
    currentPrice = currentPrice,
    changeRate = changeRate,
    changeAmount = changeAmount,
    volume = volume,
    riskLevel = riskLevel,
    investmentStrategy = investmentStrategy,
    assetClass = assetClass,
    theme = "",
    dividendRate = dividendYield,
    dividendCycle = "",
    hasDerivative = false,
    per = 0.0,
    pbr = 0.0,
    roe = 0.0,
    expenseRatio = 0.0,
    netAsset = 0L,
)

fun Etf.toLikedEntity(): LikedEtfEntity = LikedEtfEntity(
    ticker = ticker,
    name = name,
    currentPrice = currentPrice,
    changeRate = changeRate,
    changeAmount = changeAmount,
    volume = volume,
    riskLevel = riskLevel,
    investmentStrategy = investmentStrategy,
    assetClass = assetClass,
    dividendYield = dividendRate,
)
