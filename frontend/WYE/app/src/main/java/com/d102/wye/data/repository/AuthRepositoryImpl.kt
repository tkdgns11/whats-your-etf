package com.d102.wye.data.repository

import com.d102.wye.data.local.datastore.AuthTokenDataStore
import com.d102.wye.data.mapper.toDomain
import com.d102.wye.data.remote.api.AuthApiService
import com.d102.wye.data.remote.dto.request.LoginRequest
import com.d102.wye.domain.common.ApiError
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.TokenPair
import com.d102.wye.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val authTokenDataStore: AuthTokenDataStore  // 토큰 저장/조회
) : BaseRepository(), AuthRepository {

    // ─────────────────────────────────────────
    // 로그인
    // ─────────────────────────────────────────

    override suspend fun login(email: String, password: String): BaseResult<TokenPair> {
        return when (val result = safeApiCall { 
            authApiService.login(LoginRequest(email, password))
        }) {
            is BaseResult.Success -> {
                val tokenPair = result.data.toDomain()
                authTokenDataStore.saveTokens(
                    accessToken = tokenPair.accessToken,
                    refreshToken = tokenPair.refreshToken
                )
                BaseResult.Success(tokenPair)
            }
            is BaseResult.Error -> result
        }
    }

    /**
     * 카카오 SDK 로그인 결과를 서버 없이 임시 세션으로 저장한다.
     * TODO: API 연동 시 카카오 access token/code를 서버로 보내 우리 서비스 JWT를 발급받도록 교체
     */
    override suspend fun loginWithKakao(userId: Long, nickname: String?): BaseResult<Unit> {
        return try {
            authTokenDataStore.saveKakaoSession(userId = userId, nickname = nickname)
            BaseResult.Success(Unit)
        } catch (e: Exception) {
            BaseResult.Error(
                ApiError.unknownError(
                    e.message ?: "카카오 로그인 세션 저장 중 오류가 발생했습니다"
                )
            )
        }
    }


    // ─────────────────────────────────────────
    // 로그아웃
    // ─────────────────────────────────────────

    override suspend fun logout(): BaseResult<Unit> {
        return safeApiCall(
            // 서버 로그아웃 성공/실패 무관하게 로컬 토큰 삭제
            onSuccess = { authTokenDataStore.clearTokens() }
        ) {
            authApiService.logout()
        }.also {
            // 서버 오류여도 로컬은 무조건 삭제
            if (it is BaseResult.Error) {
                authTokenDataStore.clearTokens()
            }
        }
    }

    // ─────────────────────────────────────────
    // 토큰 상태 Flow (DataStore에서 직접 위임)
    // ─────────────────────────────────────────

    override val isLoggedIn: Flow<Boolean> = authTokenDataStore.isLoggedIn

    override val accessToken: Flow<String?> = authTokenDataStore.accessToken
}
