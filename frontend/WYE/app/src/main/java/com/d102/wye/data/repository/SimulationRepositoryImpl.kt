package com.d102.wye.data.repository

import com.d102.wye.data.local.dao.EtfPriceHistoryDao
import com.d102.wye.data.local.entity.EtfPriceHistoryEntity
import com.d102.wye.data.mapper.toDomain
import com.d102.wye.data.remote.api.SimulationApiService
import com.d102.wye.data.remote.dto.request.EtfCount
import com.d102.wye.data.remote.dto.request.SavePortfolioRequest
import com.d102.wye.domain.common.ApiError
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.EtfDividendHistory
import com.d102.wye.domain.model.EtfPriceHistory
import com.d102.wye.domain.model.EtfPricePoint
import com.d102.wye.domain.model.SavePortfolioParams
import com.d102.wye.domain.repository.SimulationRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.supervisorScope
import timber.log.Timber
import javax.inject.Inject

class SimulationRepositoryImpl @Inject constructor(
    private val simulationApiService: SimulationApiService,
    private val priceHistoryDao: EtfPriceHistoryDao
) : SimulationRepository {

    private val SUPPORTED_TICKERS = listOf("069500", "091160", "102780")

    // ─── API ─────────────────────────────────────────────────────────────────

    override suspend fun getEtfPriceHistories(
        tickers: List<String>,
        startDate: String?,
        endDate: String?,
        page: Int
    ): BaseResult<Map<String, EtfPriceHistory>> = runCatching {
        coroutineScope {
            val results = SUPPORTED_TICKERS.map { ticker ->
                async {
                    runCatching {
                        ticker to fetchAllPages(ticker, startDate, endDate)
                    }.getOrElse { e ->
                        Timber.e("[API] ticker=$ticker 조회 실패 | ${e.message}")
                        null
                    }
                }
            }.awaitAll()

            val successMap = results.filterNotNull().toMap()
            Timber.d("[API] 전체 조회 완료 | 성공=${successMap.keys} | 실패=${SUPPORTED_TICKERS - successMap.keys.toSet()}")
            BaseResult.Success(successMap)
        }
    }.getOrElse { e ->
        Timber.e("[API] 가격 이력 전체 조회 실패 | ${e.message}")
        BaseResult.Error(ApiError(code = -1, message = e.message ?: "가격 이력 조회 실패"))
    }

    private suspend fun fetchAllPages(
        ticker: String,
        startDate: String?,
        endDate: String?
    ): EtfPriceHistory = supervisorScope {
        // 1. 첫 페이지 호출 → data 언래핑
        val firstPageData = simulationApiService.getEtfPriceHistory(
            ticker = ticker,
            startDate = startDate,
            endDate = endDate,
            page = 0
        ).data ?: throw IllegalStateException("ticker=$ticker 응답 data가 null")

        Timber.d("[API] ticker=$ticker | totalPages=${firstPageData.totalPages} | totalElements=${firstPageData.totalElements}")

        if (firstPageData.last || firstPageData.totalPages <= 1) {
            return@supervisorScope firstPageData.toDomain(ticker)
        }

        // 2. 나머지 페이지 병렬 호출
        val remainingPages = (1 until firstPageData.totalPages).map { pageIndex ->
            async {
                simulationApiService.getEtfPriceHistory(
                    ticker = ticker,
                    startDate = startDate,
                    endDate = endDate,
                    page = pageIndex
                ).data?.also {
                    Timber.d("[API] ticker=$ticker | page=$pageIndex 완료 | count=${it.content.size}")
                } ?: throw IllegalStateException("ticker=$ticker page=$pageIndex 응답 data가 null")
            }
        }.awaitAll()

        // 3. 전체 content 합산 후 toDomain
        val allContent = firstPageData.content + remainingPages.flatMap { it.content }
        Timber.d("[API] ticker=$ticker | 전체 합산 완료 | totalCount=${allContent.size}")

        firstPageData.copy(content = allContent, last = true).toDomain(ticker)
    }

    override suspend fun getEtfDividendHistories(
        tickers: List<String>,
        startDate: String?,
        endDate: String?
    ): BaseResult<Map<String, EtfDividendHistory>> = runCatching {
        supervisorScope {
            val results = tickers.map { ticker ->
                async {
                    runCatching {
                        val data = simulationApiService.getEtfDividendHistory(
                            ticker = ticker,
                            startDate = startDate,
                            endDate = endDate
                        ).data ?: throw IllegalStateException("ticker=$ticker 배당금 응답 data가 null")
                        ticker to data.toDomain(ticker)
                    }.getOrElse { e ->
                        Timber.e("[API] ticker=$ticker 배당금 조회 실패 | ${e.message}")
                        null
                    }
                }
            }.awaitAll()

            BaseResult.Success(results.filterNotNull().toMap())
        }
    }.getOrElse { e ->
        BaseResult.Error(ApiError(code = -1, message = e.message ?: "배당금 이력 조회 실패"))
    }

    override suspend fun savePortfolio(params: SavePortfolioParams): BaseResult<Unit> =
        runCatching {
            simulationApiService.savePortfolio(
                SavePortfolioRequest(
                    portfolioName = params.portfolioName,
                    investType = params.investType.name,  // "INSTALLMENT" or "LUMP_SUM"
                    investAmount = params.investAmount,
                    investPeriod = params.investPeriod,
                    etfs = params.etfs.map { etf ->
                        EtfCount(
                            ticker = etf.ticker,
                            counts = etf.counts
                        )
                    }
                )
            )
            BaseResult.Success(Unit)
        }.getOrElse { e ->
            Timber.e("[API] 포트폴리오 저장 실패 | ${e.message}")
            BaseResult.Error(ApiError(code = -1, message = e.message ?: "포트폴리오 저장 실패"))
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
        Timber.d("[DB] 저장 시작 | 총 ${entities.size}건")
        priceHistoryDao.insertAll(entities)
        Timber.d("[DB] 저장 완료")
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