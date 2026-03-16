package com.d102.wye.domain.repository

import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.TokenPair
import kotlinx.coroutines.flow.Flow

/**
 * 인증 관련 Repository 인터페이스
 */
interface AuthRepository {

    /** 이메일 회원가입을 요청한다. */
    suspend fun signup(
        email: String,
        password: String,
        passwordConfirm: String,
        nickname: String
    ): BaseResult<Unit>

    /** 이메일 인증 코드를 확인하고 발급된 JWT를 반환한다. */
    suspend fun verifySignup(email: String, token: String): BaseResult<TokenPair>

    /** 회원가입 인증 메일을 재발송한다. */
    suspend fun resendSignupCode(email: String): BaseResult<Unit>

    /** 발급된 토큰을 로컬에 저장해 로그인 상태를 확정한다. */
    suspend fun saveAuthTokens(tokenPair: TokenPair)

    /** 로그인 */
    suspend fun login(email: String, password: String): BaseResult<TokenPair>

    /** 카카오 SDK access token으로 서버 로그인 후 JWT를 발급받는다. */
    suspend fun loginWithKakao(accessToken: String): BaseResult<TokenPair>

    /** 로그아웃 (토큰 삭제) */
    suspend fun logout(): BaseResult<Unit>

    /** FCM 토큰 서버 등록 */
    suspend fun registerFcmToken(token: String): BaseResult<Unit>

    /** 로그인 상태 Flow — MainActivity startDestination 분기에 사용 */
    val isLoggedIn: Flow<Boolean>

    /** Access Token Flow — 필요한 경우 직접 참조 */
    val accessToken: Flow<String?>
}
