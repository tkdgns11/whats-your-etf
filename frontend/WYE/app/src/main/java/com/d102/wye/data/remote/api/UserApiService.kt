package com.d102.wye.data.remote.api

import com.d102.wye.data.remote.dto.response.BaseResponse
import com.d102.wye.data.remote.dto.response.FavoriteEtfListResponse
import com.d102.wye.data.remote.dto.response.UserProfileResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Part
import retrofit2.http.Query
import com.d102.wye.data.remote.dto.request.UpdateUserProfileRequest

interface UserApiService {

    /** 현재 로그인한 사용자의 프로필 정보를 조회한다. */
    @GET("users/me")
    suspend fun getMyProfile(): Response<BaseResponse<UserProfileResponse>>

    /** 현재 로그인한 사용자의 관심 ETF 목록을 조회한다. */
    @GET("users/me/favorites/etfs")
    suspend fun getFavoriteEtfs(
        @Query("sort") sort: String = "RECENT"
    ): Response<BaseResponse<FavoriteEtfListResponse>>

    /** 특정 ETF가 관심 ETF에 등록되어 있는지 확인한다. */
    @GET("users/me/favorites/etfs/{etfId}/check")
    suspend fun checkFavoriteEtf(
        @Path("etfId") etfId: Long
    ): Response<BaseResponse<Boolean>>

    /** 변경할 필드만 전달해 내 프로필을 수정한다. */
    @PATCH("users/me")
    suspend fun updateMyProfile(
        @Body request: UpdateUserProfileRequest
    ): Response<BaseResponse<UserProfileResponse>>

    /** multipart로 프로필 이미지를 업로드한다. */
    @Multipart
    @POST("users/me/profile-image")
    suspend fun uploadProfileImage(
        @Part file: MultipartBody.Part
    ): Response<BaseResponse<String>>

    /** 서버에 저장된 프로필 이미지를 삭제한다. */
    @DELETE("users/me/profile-image")
    suspend fun deleteProfileImage(): Response<BaseResponse<Unit>>
}
