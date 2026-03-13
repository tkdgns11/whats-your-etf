package com.d102.wye.data.repository

import com.d102.wye.data.local.dao.EtfPriceHistoryDao
import com.d102.wye.data.local.entity.EtfPriceHistoryEntity
import com.d102.wye.data.mapper.toDomain
import com.d102.wye.data.remote.api.SimulationApiService
import com.d102.wye.domain.common.ApiError
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.EtfDividendHistory
import com.d102.wye.domain.model.EtfPriceHistory
import com.d102.wye.domain.model.EtfPricePoint
import com.d102.wye.domain.repository.SimulationRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class SimulationRepositoryImpl @Inject constructor(
    private val simulationApiService: SimulationApiService,
    private val priceHistoryDao: EtfPriceHistoryDao
) : SimulationRepository {

    // 임시 지원 ticker 목록 (서버 지원 범위 확정 후 제거)
    private val SUPPORTED_TICKERS = listOf("069500", "091160", "102780")

    // ─── API ─────────────────────────────────────────────────────────────────

    override suspend fun getEtfPriceHistories(
        tickers: List<String>,
        startDate: String?,
        endDate: String?,
        page: Int
    ): BaseResult<Map<String, EtfPriceHistory>> = runCatching {
        val targetTickers = tickers.filter { it in SUPPORTED_TICKERS }
        coroutineScope {
            val results = targetTickers.map { ticker ->
                async {
                    ticker to simulationApiService.getEtfPriceHistory(
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
                    ticker to simulationApiService.getEtfDividendHistory(
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

    // ─── 로컬 DB 캐시 ─────────────────────────────────────────────────────────

    override suspend fun savePriceHistories(histories: Map<String, EtfPriceHistory>) {
        val entities = histories.flatMap { (ticker, history) ->
            history.content.map { point ->
                EtfPriceHistoryEntity(
                    ticker = ticker,
                    date = point.date,
                    stockPrice = point.stockPrice,
                    dailyReturn = point.dailyReturn
                )
            }
        }
        priceHistoryDao.insertAll(entities)
    }

    override suspend fun getCachedPriceHistories(
        tickers: List<String>
    ): Map<String, EtfPriceHistory> {
        return tickers.associateWith { ticker ->
            val entities = priceHistoryDao.getByTicker(ticker)
            EtfPriceHistory(
                ticker = ticker,
                content = entities.map { entity ->
                    EtfPricePoint(
                        date = entity.date,
                        stockPrice = entity.stockPrice,
                        dailyReturn = entity.dailyReturn
                    )
                },
                totalElements = entities.size,
                totalPages = 1,
                last = true
            )
        }.filter { it.value.content.isNotEmpty() }
    }

    override suspend fun hasCachedPriceHistory(ticker: String): Boolean =
        priceHistoryDao.countByTicker(ticker) > 0

    override suspend fun deleteCachedPriceHistory(ticker: String) =
        priceHistoryDao.deleteByTicker(ticker)
}