package com.d102.wye.data.repository

import com.d102.wye.data.local.datastore.AuthTokenDataStore
import com.d102.wye.data.mapper.toDomain
import com.d102.wye.data.remote.api.AuthApiService
import com.d102.wye.data.remote.dto.request.FcmTokenRequest
import com.d102.wye.data.remote.dto.request.KakaoLoginRequest
import com.d102.wye.data.remote.dto.request.LoginRequest
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

    /** 카카오 SDK access token으로 서버 로그인 후 JWT를 저장한다. */
    override suspend fun loginWithKakao(accessToken: String): BaseResult<TokenPair> {
        return when (val result = safeApiCall {
            authApiService.loginWithKakao(KakaoLoginRequest(accessToken = accessToken))
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
    // FCM 토큰 등록
    // ─────────────────────────────────────────

    override suspend fun registerFcmToken(token: String): BaseResult<Unit> {
        return safeApiCall { authApiService.registerFcmToken(FcmTokenRequest(token)) }
    }

    // ─────────────────────────────────────────
    // 토큰 상태 Flow (DataStore에서 직접 위임)
    // ─────────────────────────────────────────

    override val isLoggedIn: Flow<Boolean> = authTokenDataStore.isLoggedIn

    override val accessToken: Flow<String?> = authTokenDataStore.accessToken
}
