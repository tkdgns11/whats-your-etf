package com.d102.wye.presentation.simulation.progress

import com.d102.wye.domain.model.Portfolio

/**
 * 시뮬레이션 화면 전용 UI 모델
 *
 * - UI 상태(슬라이더 값, 이름 표시 등) 관리 용도
 */
data class PortfolioItem(
    val ticker: String,
    val name: String,
    val weight: Int  // 0~100
)

/** presentation → domain 변환 */
fun PortfolioItem.toDomain(): Portfolio = Portfolio(
    ticker = ticker,
    name = name,
    weightPercent = weight
)

/** presentation 리스트 → domain 리스트 변환 */
fun List<PortfolioItem>.toDomain(): List<Portfolio> = map { it.toDomain() }