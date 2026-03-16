package com.d102.wye.data.repository

import com.d102.wye.data.mapper.toDomain
import com.d102.wye.data.remote.api.UserApiService
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.common.map
import com.d102.wye.domain.model.UserProfile
import com.d102.wye.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApiService: UserApiService
) : BaseRepository(), UserRepository {

    /** 마이페이지에서 사용할 내 프로필 정보를 서버에서 조회한다. */
    override suspend fun getMyProfile(): BaseResult<UserProfile> {
        return safeApiCall {
            userApiService.getMyProfile()
        }.map { it.toDomain() }
    }
}
