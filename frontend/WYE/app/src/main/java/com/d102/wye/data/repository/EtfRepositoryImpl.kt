package com.d102.wye.data.repository

import com.d102.wye.data.mapper.toDomain
import com.d102.wye.data.remote.api.EtfApiService
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.Etf
import com.d102.wye.domain.model.EtfDetail
import com.d102.wye.domain.model.EtfPeriodReturn
import com.d102.wye.domain.model.EtfReturnChart
import com.d102.wye.domain.repository.EtfRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EtfRepositoryImpl @Inject constructor(
    private val etfApiService: EtfApiService,
) : BaseRepository(), EtfRepository {

    // 1분마다 polling
    override fun getEtfList(): Flow<List<Etf>> = flow {
        while (true) {
            val result = safeApiCall { etfApiService.getEtfList() }
            if (result is BaseResult.Success) {
                emit(result.data.map { it.toDomain() })
            }
            delay(60_000L)
        }
    }

    override suspend fun toggleLike(ticker: String): BaseResult<Boolean> =
        safeApiCall { etfApiService.toggleLike(ticker) }

    override suspend fun getEtfDetail(ticker: String): BaseResult<EtfDetail> {
        return when (val result = safeApiCall { etfApiService.getEtfDetail(ticker) }) {
            is BaseResult.Success -> BaseResult.Success(result.data.toDomain())
            is BaseResult.Error   -> result
        }
    }

    override suspend fun getEtfReturnChart(ticker: String, period: String): BaseResult<EtfReturnChart> {
        return when (val result = safeApiCall { etfApiService.getEtfReturnChart(ticker, period) }) {
            is BaseResult.Success -> BaseResult.Success(result.data.toDomain())
            is BaseResult.Error   -> result
        }
    }

    override suspend fun getEtfPeriodReturn(ticker: String): BaseResult<EtfPeriodReturn> {
        return when (val result = safeApiCall { etfApiService.getEtfPeriodReturn(ticker) }) {
            is BaseResult.Success -> BaseResult.Success(result.data.toDomain())
            is BaseResult.Error   -> result
        }
    }

}
