package com.d102.wye.domain.usecase.etf

import com.d102.wye.domain.model.Etf
import com.d102.wye.domain.state.EtfFilterState

class FilterEtfListUseCase {
    operator fun invoke(etfList: List<Etf>, filter: EtfFilterState): List<Etf> {
        return etfList
            .filter { filter.riskLevels.isEmpty() || it.riskLevel in filter.riskLevels }
            .filter { filter.assetClass == null || it.assetClass == filter.assetClass }
            .filter { filter.strategy == null || it.investmentStrategy == filter.strategy }
            .filter { filter.themes.isEmpty() || it.theme in filter.themes }
            .filter { filter.dividendCycle == null || it.dividendCycle == filter.dividendCycle }
            .filter { filter.hasDerivative == null || it.hasDerivative == filter.hasDerivative }
            .filter { etf ->
                filter.dividendRateRange == null || when (filter.dividendRateRange) {
                    "0-5"  -> etf.dividendRate in 0.0..5.0
                    "5-10" -> etf.dividendRate in 5.0..10.0
                    else   -> true
                }
            }
            .filter { etf ->
                filter.peRange == null || when (filter.peRange) {
                    "under10" -> etf.per < 10.0
                    "10-20"   -> etf.per in 10.0..20.0
                    "over20"  -> etf.per > 20.0
                    else      -> true
                }
            }
            .filter { etf ->
                filter.pbRange == null || when (filter.pbRange) {
                    "under1" -> etf.pbr < 1.0
                    "1-3"    -> etf.pbr in 1.0..3.0
                    "over3"  -> etf.pbr > 3.0
                    else     -> true
                }
            }
            .filter { etf ->
                filter.roeRange == null || when (filter.roeRange) {
                    "under5"  -> etf.roe < 5.0
                    "5-15"    -> etf.roe in 5.0..15.0
                    "over15"  -> etf.roe > 15.0
                    else      -> true
                }
            }
            .filter { etf ->
                filter.expenseRatioRange == null || when (filter.expenseRatioRange) {
                    "under0.05"  -> etf.expenseRatio < 0.05
                    "0.05-0.5"   -> etf.expenseRatio in 0.05..0.5
                    "over0.5"    -> etf.expenseRatio > 0.5
                    else         -> true
                }
            }
            .filter { etf ->
                filter.netAssetRange == null || when (filter.netAssetRange) {
                    "under100"  -> etf.netAsset < 100_000_000_000L
                    "100-1000"  -> etf.netAsset in 100_000_000_000L..1_000_000_000_000L
                    "over1000"  -> etf.netAsset > 1_000_000_000_000L
                    else        -> true
                }
            }
            .filter {
                filter.query.isBlank() ||
                it.name.contains(filter.query, ignoreCase = true) ||
                it.ticker.contains(filter.query, ignoreCase = true)
            }
    }
}
