package com.d102.wye.domain.repository

import com.d102.wye.domain.model.Etf
import kotlinx.coroutines.flow.Flow

/**
 * domain/repository/ 에는 인터페이스만 작성한다
 */
interface EtfRepository {
    fun getEtfList(): Flow<List<Etf>>                       // 1분 polling
//    suspend fun getEtfDetail(ticker: String): BaseResult<EtfDetail>
}