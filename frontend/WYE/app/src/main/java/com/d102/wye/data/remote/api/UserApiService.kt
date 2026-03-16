package com.d102.wye.data.remote.api

import com.d102.wye.data.remote.dto.response.BaseResponse
import com.d102.wye.data.remote.dto.response.UserProfileResponse
import retrofit2.Response
import retrofit2.http.GET

interface UserApiService {

    /** 현재 로그인한 사용자의 프로필 정보를 조회한다. */
    @GET("users/me")
    suspend fun getMyProfile(): Response<BaseResponse<UserProfileResponse>>
}
