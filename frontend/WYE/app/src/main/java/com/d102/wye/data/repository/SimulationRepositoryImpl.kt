package com.d102.wye.data.repository

import com.d102.wye.data.mapper.toDomain
import com.d102.wye.data.remote.api.SimulationApiService
import com.d102.wye.domain.common.ApiError
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.EtfDividendHistory
import com.d102.wye.domain.model.EtfPriceHistory
import com.d102.wye.domain.repository.SimulationRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class SimulationRepositoryImpl @Inject constructor(
    private val simulationService: SimulationApiService
) : SimulationRepository {

    /**
     * 여러 ticker를 병렬로 개별 호출 후 Map으로 합산
     * coroutineScope + async로 동시 요청 → 순차 호출 대비 시간 단축
     */
    override suspend fun getEtfPriceHistories(
        tickers: List<String>,
        startDate: String?,
        endDate: String?,
        page: Int
    ): BaseResult<Map<String, EtfPriceHistory>> = runCatching {
        coroutineScope {
            val results = tickers.map { ticker ->
                async {
                    ticker to simulationService.getEtfPriceHistory(
                        ticker = ticker,
                        startDate = startDate,
                        endDate = endDate,
                        page = page
                    ).toDomain(ticker)
                }
            }.awaitAll()

            BaseResult.Success(results.toMap())
        }
    }.getOrElse { e ->
        BaseResult.Error(ApiError(code = -1, message = e.message ?: "가격 이력 조회 실패"))
    }

    override suspend fun getEtfDividendHistories(
        tickers: List<String>,
        startDate: String?,
        endDate: String?
    ): BaseResult<Map<String, EtfDividendHistory>> = runCatching {
        coroutineScope {
            val results = tickers.map { ticker ->
                async {
                    ticker to simulationService.getEtfDividendHistory(
                        ticker = ticker,
                        startDate = startDate,
                        endDate = endDate
                    ).toDomain(ticker)
                }
            }.awaitAll()

            BaseResult.Success(results.toMap())
        }
    }.getOrElse { e ->
        BaseResult.Error(ApiError(code = -1, message = e.message ?: "배당금 이력 조회 실패"))
    }
}