package com.d102.wye.data.repository

import com.d102.wye.data.remote.api.PortfolioApiService
import com.d102.wye.data.remote.dto.request.EtfCount
import com.d102.wye.data.remote.dto.request.SavePortfolioRequest
import com.d102.wye.domain.common.ApiError
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.PortfolioEtf
import com.d102.wye.domain.model.PortfolioListItem
import com.d102.wye.domain.model.SavePortfolioParams
import com.d102.wye.domain.repository.PortfolioRepository
import timber.log.Timber
import javax.inject.Inject

class PortfolioRepositoryImpl @Inject constructor(
    private val portfolioApiService: PortfolioApiService
) : PortfolioRepository {

    override suspend fun savePortfolio(params: SavePortfolioParams): BaseResult<Unit> =
        runCatching {
            portfolioApiService.savePortfolio(
                SavePortfolioRequest(
                    portfolioName = params.portfolioName,
                    portfolioType = params.investType.name,
                    investAmount = params.investAmount,
                    investPeriod = params.investPeriod,
                    etfs = params.etfs.map { EtfCount(ticker = it.ticker, counts = it.counts) }
                )
            )
            BaseResult.Success(Unit)
        }.getOrElse { e ->
            Timber.e("[API] 포트폴리오 저장 실패 | ${e.message}")
            BaseResult.Error(ApiError(code = -1, message = e.message ?: "포트폴리오 저장 실패"))
        }

    override suspend fun getPortfolioList(): BaseResult<List<PortfolioListItem>> =
        runCatching {
            val data = portfolioApiService.getPortfolioList().data
                ?: return BaseResult.Error(ApiError(code = -1, message = "포트폴리오 목록 응답이 없습니다"))

            BaseResult.Success(data.map { dto ->
                PortfolioListItem(
                    portfolioId = dto.portfolioId,
                    title = dto.title,
                    createdAt = dto.createdAt.take(10),  // "2026-03-15" 날짜만
                    etfList = dto.etfList.map { etf ->
                        PortfolioEtf(ticker = etf.ticker, name = etf.name)
                    },
                    totalReturn = dto.totalReturn
                )
            })
        }.getOrElse { e ->
            Timber.e("[API] 포트폴리오 목록 조회 실패 | ${e.message}")
            BaseResult.Error(ApiError(code = -1, message = e.message ?: "포트폴리오 목록 조회 실패"))
        }

    override suspend fun deletePortfolio(portfolioId: Long): BaseResult<Unit> =
        runCatching {
            portfolioApiService.deletePortfolio(portfolioId)
            BaseResult.Success(Unit)
        }.getOrElse { e ->
            Timber.e("[API] 포트폴리오 삭제 실패 | id=$portfolioId | ${e.message}")
            BaseResult.Error(ApiError(code = -1, message = e.message ?: "포트폴리오 삭제 실패"))
        }
}