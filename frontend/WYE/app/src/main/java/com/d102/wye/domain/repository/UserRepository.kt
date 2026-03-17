package com.d102.wye.domain.repository

import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.UserProfile

interface UserRepository {

    /** 현재 로그인한 사용자의 프로필 정보를 조회한다. */
    suspend fun getMyProfile(): BaseResult<UserProfile>

    /** 변경된 필드만 포함해 내 프로필을 수정한다. */
    suspend fun updateMyProfile(
        nickname: String? = null,
        profileImage: String? = null
    ): BaseResult<UserProfile>

    /** 프로필 이미지를 업로드하고 최신 프로필을 반환한다. */
    suspend fun uploadProfileImage(imageUri: String): BaseResult<UserProfile>

    /** 프로필 이미지를 삭제하고 최신 프로필을 반환한다. */
    suspend fun deleteProfileImage(): BaseResult<UserProfile>
}
