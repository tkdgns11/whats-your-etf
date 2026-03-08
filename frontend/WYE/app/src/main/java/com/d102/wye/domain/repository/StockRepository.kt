package com.d102.wye.domain.repository

import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.Stock

interface StockRepository {
    suspend fun getStock(ticker: String): BaseResult<Stock>
}
