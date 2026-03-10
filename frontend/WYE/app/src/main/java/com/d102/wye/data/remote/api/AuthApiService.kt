package com.d102.wye.data.remote.api

import com.d102.wye.data.remote.dto.request.FcmTokenRequest
import com.d102.wye.data.remote.dto.request.LoginRequest
import com.d102.wye.data.remote.dto.response.BaseResponse
import com.d102.wye.data.remote.dto.response.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 인증 관련 Retrofit API 인터페이스
 *
 * 위치: data/remote/api/
 * 역할: 서버와의 HTTP 통신 명세 선언
 *       실제 구현은 Retrofit이 런타임에 생성
 *
 * 엔드포인트 경로는 서버 API 확정 후 수정
 * Response<BaseResponse<T>> 구조:
 *   - Response<>          : HTTP 레벨 (상태코드, 헤더)
 *   - BaseResponse<T>     : 서버 공통 응답 래퍼 (data, message, code)
 *   - T                   : 실제 데이터 타입
 */
interface AuthApiService {

    /**
     * 자체 이메일 로그인
     * POST /auth/login
     */
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<BaseResponse<TokenResponse>>


    /**
     * 로그아웃
     * POST /auth/logout
     * 서버에서 Refresh Token 무효화
     */
    @POST("auth/logout")
    suspend fun logout(): Response<BaseResponse<Unit>>

    /**
     * FCM 토큰 등록
     * POST /auth/fcm-token
     * 로그인 후 또는 토큰 갱신 시 서버에 저장
     */
    @POST("auth/fcm-token")
    suspend fun registerFcmToken(
        @Body request: FcmTokenRequest,
    ): Response<BaseResponse<Unit>>
}