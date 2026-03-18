package com.d102.wye.data.mapper

import com.d102.wye.data.remote.dto.response.RelatedStockResponse
import com.d102.wye.domain.model.RelatedStock

// GET /api/v1/stocks/{ticker}/related 항목 → RelatedStock
fun RelatedStockResponse.toDomain() = RelatedStock(
    ticker = ticker,
    name = companyName,
    description = "$relationType ($industryName)",
)
