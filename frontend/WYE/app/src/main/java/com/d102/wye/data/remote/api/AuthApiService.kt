package com.d102.wye.data.remote.api

import com.d102.wye.data.remote.dto.request.FcmTokenRequest
import com.d102.wye.data.remote.dto.request.KakaoLoginRequest
import com.d102.wye.data.remote.dto.request.LoginRequest
import com.d102.wye.data.remote.dto.response.BaseResponse
import com.d102.wye.data.remote.dto.response.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 인증 관련 Retrofit API 인터페이스
 */
interface AuthApiService {

    /**
     * 이메일 로그인
     */
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<BaseResponse<TokenResponse>>

    /**
     * 카카오 로그인
     */
    @POST("auth/oauth/kakao")
    suspend fun loginWithKakao(
        @Body request: KakaoLoginRequest
    ): Response<BaseResponse<TokenResponse>>


    /**
     * 로그아웃
     * 서버에서 Refresh Token 무효화
     */
    @POST("auth/logout")
    suspend fun logout(): Response<BaseResponse<Unit>>

    /**
     * FCM 토큰 등록
     * 로그인 후 또는 토큰 갱신 시 서버에 저장
     */
    @POST("auth/fcm/token")
    suspend fun registerFcmToken(
        @Body request: FcmTokenRequest,
    ): Response<BaseResponse<Unit>>
}
