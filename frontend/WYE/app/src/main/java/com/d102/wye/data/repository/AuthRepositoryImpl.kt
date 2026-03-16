package com.d102.wye.data.repository

import com.d102.wye.data.local.datastore.AuthTokenDataStore
import com.d102.wye.data.mapper.toDomain
import com.d102.wye.data.remote.api.AuthApiService
import com.d102.wye.data.remote.dto.request.FcmTokenRequest
import com.d102.wye.data.remote.dto.request.KakaoLoginRequest
import com.d102.wye.data.remote.dto.request.LoginRequest
import com.d102.wye.data.remote.dto.request.SignupRequest
import com.d102.wye.data.remote.dto.request.SignupResendRequest
import com.d102.wye.data.remote.dto.request.SignupVerifyRequest
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

    /** 회원가입 요청을 보내고 성공 여부만 반환한다. */
    override suspend fun signup(
        email: String,
        password: String,
        passwordConfirm: String,
        nickname: String
    ): BaseResult<Unit> {
        return when (
            val result = safeApiCall {
                authApiService.signup(
                    SignupRequest(
                        email = email,
                        password = password,
                        passwordConfirm = passwordConfirm,
                        nickname = nickname
                    )
                )
            }
        ) {
            is BaseResult.Success -> BaseResult.Success(Unit)
            is BaseResult.Error -> result
        }
    }

    /** 인증 코드를 검증하고 회원가입 완료에 필요한 토큰만 반환한다. */
    override suspend fun verifySignup(email: String, token: String): BaseResult<TokenPair> {
        return when (
            val result = safeApiCall {
                authApiService.verifySignup(
                    SignupVerifyRequest(
                        email = email,
                        token = token
                    )
                )
            }
        ) {
            is BaseResult.Success -> {
                val tokenPair = result.data.toDomain()
                BaseResult.Success(tokenPair)
            }
            is BaseResult.Error -> result
        }
    }

    /** 같은 이메일로 회원가입 인증 메일을 다시 발송한다. */
    override suspend fun resendSignupCode(email: String): BaseResult<Unit> {
        return safeApiCallWithoutData {
            authApiService.resendSignupCode(SignupResendRequest(email = email))
        }
    }

    override suspend fun saveAuthTokens(tokenPair: TokenPair) {
        authTokenDataStore.saveTokens(
            accessToken = tokenPair.accessToken,
            refreshToken = tokenPair.refreshToken
        )
    }

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
        return safeApiCallWithoutData(
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
        return safeApiCallWithoutData {
            authApiService.registerFcmToken(
                FcmTokenRequest(
                    token = token,
                    deviceType = "ANDROID"
                )
            )
        }
    }

    // ─────────────────────────────────────────
    // 토큰 상태 Flow (DataStore에서 직접 위임)
    // ─────────────────────────────────────────

    override val isLoggedIn: Flow<Boolean> = authTokenDataStore.isLoggedIn

    override val accessToken: Flow<String?> = authTokenDataStore.accessToken
}
