package com.d102.wye.domain.repository

import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.Etf
import com.d102.wye.domain.model.EtfDetail
import com.d102.wye.domain.model.EtfPeriodReturn
import com.d102.wye.domain.model.EtfReturnChart
import kotlinx.coroutines.flow.Flow

interface EtfRepository {
    fun getEtfList(): Flow<List<Etf>>
    fun getLikedEtfList(): Flow<List<Etf>>
    suspend fun toggleLike(etf: Etf): BaseResult<Boolean>
    suspend fun getEtfDetail(ticker: String): BaseResult<EtfDetail>
    suspend fun getEtfReturnChart(ticker: String, period: String): BaseResult<EtfReturnChart>
    suspend fun getEtfPeriodReturn(ticker: String): BaseResult<EtfPeriodReturn>
}
