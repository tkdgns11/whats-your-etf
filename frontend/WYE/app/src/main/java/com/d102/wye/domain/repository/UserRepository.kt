package com.d102.wye.domain.repository

import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.UserProfile

interface UserRepository {

    /** 현재 로그인한 사용자의 프로필 정보를 조회한다. */
    suspend fun getMyProfile(): BaseResult<UserProfile>
}
