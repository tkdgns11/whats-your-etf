package com.d102.wye.domain.repository

import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.News
import com.d102.wye.domain.model.NewsDetail

interface NewsRepository {
    /** 뉴스 목록을 조회한다. */
    suspend fun getNewsList(category: String? = null): BaseResult<List<News>>

    /** 뉴스 상세를 조회한다. */
    suspend fun getNewsDetail(newsId: Long): BaseResult<NewsDetail>
}
