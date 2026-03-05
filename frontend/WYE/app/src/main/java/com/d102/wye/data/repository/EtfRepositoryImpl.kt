package com.d102.wye.data.repository

import com.d102.wye.core.service.EtfApiService
import com.d102.wye.domain.model.Etf
import com.d102.wye.domain.repository.EtfRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EtfRepositoryImpl @Inject constructor(
    private val etfApiService: EtfApiService,
) : EtfRepository {

    // 1분마다 polling → Flow로 제공
    override fun getEtfList(): Flow<List<Etf>> = flow {
        while (true) {
//            val result = safeApiCall { etfApiService.getEtfList() }
//            if (result is BaseResult.Success) {
//                emit(result.data.map { etfMapper.toDomain(it) })
//            }
//            delay(60_000L)
        }
    }
}