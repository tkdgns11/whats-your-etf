package com.d102.wye.domain.state

/**
 * domain/state에는 상태를 정의하는 데이터 클래스를 작성한다
 */
data class EtfFilterState(
    val query: String = "",
    val riskLevels: Set<Int> = emptySet(),      // 위험등급 필터 (1~5)
    val assetClass: String? = null,             // 기초자산 필터
    val strategy: String? = null               // 투자전략 필터
)