package com.d102.wye.domain.repository

import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.EtfDividendHistory
import com.d102.wye.domain.model.EtfPriceHistory

interface SimulationRepository {

    /**
     * ETF 가격 이력 조회 (ticker 개별 호출 → Map으로 반환)
     * @param tickers   조회할 ETF ticker 목록
     * @param startDate "yyyy-MM-dd" (null → 서버 기본값)
     * @param endDate   "yyyy-MM-dd" (null → 오늘)
     * @param page      페이지 번호 (0-indexed)
     */
    suspend fun getEtfPriceHistories(
        tickers: List<String>,
        startDate: String? = null,
        endDate: String? = null,
        page: Int = 0
    ): BaseResult<Map<String, EtfPriceHistory>>

    /**
     * ETF 월별 배당금 이력 조회 (ticker 개별 호출 → Map으로 반환)
     * @param tickers   조회할 ETF ticker 목록
     * @param startDate "yyyy-MM" (null → 서버 기본값)
     * @param endDate   "yyyy-MM" (null → 현재)
     */
    suspend fun getEtfDividendHistories(
        tickers: List<String>,
        startDate: String? = null,
        endDate: String? = null
    ): BaseResult<Map<String, EtfDividendHistory>>
}