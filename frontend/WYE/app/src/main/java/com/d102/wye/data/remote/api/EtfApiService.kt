package com.d102.wye.data.remote.api

import com.d102.wye.data.remote.dto.response.BaseResponse
import retrofit2.Response
import retrofit2.http.GET

interface EtfApiService {

    @GET("api/test")
    suspend fun test(): Response<BaseResponse<Unit>>

}