package com.d102.wye.data.mapper

import com.d102.wye.data.remote.dto.response.EtfResponse
import com.d102.wye.domain.model.Etf

/**
 * DTO를 domain model로 직접 써도 동작에 문제가 없으면 mapper를 미리 만들 필요 없다
 */

//fun EtfResponse.toDomain() = Etf(
//    ticker = Bticker,
//    name = name,
//    currentPrice = currentPrice,
//    changeRate = changeRate,
//    volume = volume,
//    riskLevel = riskLevel,
//    investmentStrategy = investmentStrategy,
//    assetClass = assetClass,
//    dividendYield = dividendYield ?: 0.0   // ← null 처리 등 변환 로직
//)
