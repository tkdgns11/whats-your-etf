package com.d102.wye.domain.usecase

/**
 * domain/usecase/ 에는 비즈니스 로직을 작성한다
 * ViewModel에서 재사용하며 단위테스트가 필요한 부분이다
 */

// 비중 합산 검증 로직
//class CalculateWeightUseCase {
//    operator fun invoke(portfolios: List<Portfolio>): WeightValidationResult {
//        val total = portfolios.sumOf { it.weightPercent }
//        return when {
//            total > 100.0 -> WeightValidationResult.Exceeded(total)
//            total == 100.0 -> WeightValidationResult.Valid
//            else -> WeightValidationResult.Incomplete(remaining = 100.0 - total)
//        }
//    }
//}