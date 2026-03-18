package com.d102.wye.domain.repository

import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.FavoriteEtfList
import com.d102.wye.domain.model.FavoriteEtfSort
import com.d102.wye.domain.model.UserProfile

interface UserRepository {

    /** 현재 로그인한 사용자의 프로필 정보를 조회한다. */
    suspend fun getMyProfile(): BaseResult<UserProfile>

    /** 현재 로그인한 사용자의 관심 ETF 목록을 조회한다. */
    suspend fun getFavoriteEtfs(sort: FavoriteEtfSort = FavoriteEtfSort.RECENT): BaseResult<FavoriteEtfList>

    /** 특정 ETF가 관심 ETF에 등록되어 있는지 확인한다. */
    suspend fun checkFavoriteEtf(etfId: Long): BaseResult<Boolean>

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
