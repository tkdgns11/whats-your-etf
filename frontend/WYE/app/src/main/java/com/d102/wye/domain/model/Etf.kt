package com.d102.wye.domain.model

/**
 * 예시 코드
 * domain/model/ 에는 순수 Kotlin 데이터 클래스를 작성한다
 * -> Android import가 없어야 한다
 */
data class Etf(
    val ticker: String,
    val name: String,
    val currentPrice: Long,
    val changeRate: Double,         // 등락률
    val volume: Long,               // 매매량
    val riskLevel: Int,             // 위험등급 1~5
    val investmentStrategy: String,
    val assetClass: String          // 기초자산
)
