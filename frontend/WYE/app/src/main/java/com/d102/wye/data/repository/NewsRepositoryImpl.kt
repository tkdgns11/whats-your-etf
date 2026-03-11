package com.d102.wye.data.repository

import com.d102.wye.data.mapper.toDomain
import com.d102.wye.data.remote.api.NewsApiService
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.common.map
import com.d102.wye.domain.model.News
import com.d102.wye.domain.repository.NewsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepositoryImpl @Inject constructor(
    private val newsApiService: NewsApiService
) : BaseRepository(), NewsRepository {

    /** 뉴스 목록 API를 호출하고 응답 DTO를 도메인 모델로 변환한다. */
    override suspend fun getNewsList(category: String?): BaseResult<List<News>> {
        return safeApiCall {
            newsApiService.getNewsList(category = category)
        }.map { it.toDomain() }
    }
}
