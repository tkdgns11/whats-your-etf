package com.d102.wye.data.remote.api

import com.d102.wye.data.remote.dto.response.BaseResponse
import com.d102.wye.data.remote.dto.response.UserProfileResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.PATCH
import com.d102.wye.data.remote.dto.request.UpdateUserProfileRequest

interface UserApiService {

    /** 현재 로그인한 사용자의 프로필 정보를 조회한다. */
    @GET("users/me")
    suspend fun getMyProfile(): Response<BaseResponse<UserProfileResponse>>

    /** 변경할 필드만 전달해 내 프로필을 수정한다. */
    @PATCH("users/me")
    suspend fun updateMyProfile(
        @Body request: UpdateUserProfileRequest
    ): Response<BaseResponse<UserProfileResponse>>
}
