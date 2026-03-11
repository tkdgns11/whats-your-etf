package com.d102.wye.domain.repository

import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.News

interface NewsRepository {
    /** 뉴스 목록을 조회한다. */
    suspend fun getNewsList(category: String? = null): BaseResult<List<News>>
}
