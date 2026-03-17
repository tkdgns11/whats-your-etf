package com.d102.wye.domain.repository

import com.d102.wye.data.remote.dto.request.EtfListRequest
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.EtfClusterData
import com.d102.wye.domain.model.EtfDetail
import com.d102.wye.domain.model.EtfLikeData
import com.d102.wye.domain.model.EtfPage
import com.d102.wye.domain.model.EtfPriceData
import kotlinx.coroutines.flow.Flow

interface EtfRepository {
    suspend fun getEtfList(request: EtfListRequest = EtfListRequest(), page: Int = 0): BaseResult<EtfPage>
    fun getLikedEtfList(): Flow<List<EtfLikeData>>
    suspend fun toggleLike(data: EtfLikeData): BaseResult<Boolean>
    suspend fun getEtfDetail(ticker: String): BaseResult<EtfDetail>
    suspend fun getEtfCluster(ticker: String): BaseResult<EtfClusterData>
    suspend fun getEtfPriceHistory(ticker: String, startDate: String, endDate: String, size: Int = 300): BaseResult<List<EtfPriceData>>
}
