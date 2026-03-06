package com.d102.wye.domain.repository

import com.d102.wye.data.remote.dto.response.TokenResponse
import com.d102.wye.domain.common.BaseResult
import kotlinx.coroutines.flow.Flow

/**
 * 인증 관련 Repository 인터페이스
 */
interface AuthRepository {

    /** 로그인 */
    suspend fun login(email: String, password: String): BaseResult<TokenResponse>

    /** 로그아웃 (토큰 삭제) */
    suspend fun logout(): BaseResult<Unit>

    /** 로그인 상태 Flow — MainActivity startDestination 분기에 사용 */
    val isLoggedIn: Flow<Boolean>

    /** Access Token Flow — 필요한 경우 직접 참조 */
    val accessToken: Flow<String?>
}