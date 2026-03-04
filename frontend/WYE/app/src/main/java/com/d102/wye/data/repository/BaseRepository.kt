package com.d102.wye.data.repository

import com.d102.wye.data.remote.dto.response.BaseResponse
import com.d102.wye.domain.common.ApiError
import com.d102.wye.domain.common.BaseResult
import com.google.gson.Gson
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * 모든 RepositoryImpl의 공통 베이스
 *
 * 사용법:
 * class EtfRepositoryImpl @Inject constructor(...) : BaseRepository() {
 *     override suspend fun getEtfList() = safeApiCall {
 *         etfApiService.getEtfList()
 *     }
 * }
 *
 * safeApiCall vs directApiCall:
 * - safeApiCall    : 서버가 BaseResponse<T> 래퍼로 응답할 때 사용
 * - directApiCall  : BaseResponse 없이 T를 바로 응답할 때 사용 (파일 다운로드 등)
 */
abstract class BaseRepository {

    /**
     * BaseResponse<T> 래퍼로 감싸진 API 호출
     *
     * @param onSuccess 성공 시 추가 작업 (DataStore 저장, Room 캐싱 등)
     * @param apiCall   실제 Retrofit 호출 람다
     *
     * 사용 예시:
     * suspend fun login(email: String, pw: String) = safeApiCall(
     *     onSuccess = { tokenPair -> dataStore.saveTokens(tokenPair) }
     * ) {
     *     authApiService.login(LoginRequest(email, pw))
     * }
     */
    protected suspend fun <T> safeApiCall(
        onSuccess: (suspend (T) -> Unit)? = null,
        apiCall: suspend () -> Response<BaseResponse<T>>
    ): BaseResult<T> {
        return try {
            val response = apiCall()

            if (response.isSuccessful) {
                val body = response.body()

                when {
                    // 응답 body 자체가 null
                    body == null -> {
                        BaseResult.Error(
                            ApiError(
                                message = "응답 데이터가 없습니다",
                                code = response.code(),
                                type = ApiError.ErrorType.UNKNOWN
                            )
                        )
                    }

                    // data 필드가 있는 정상 응답
                    body.data != null -> {
                        onSuccess?.invoke(body.data)
                        BaseResult.Success(body.data)
                    }

                    // 200이지만 data가 null (서버 비즈니스 에러)
                    else -> {
                        BaseResult.Error(
                            ApiError(
                                message = body.message ?: "알 수 없는 오류",
                                code = body.code ?: response.code(),
                                type = ApiError.getErrorType(body.code)
                            )
                        )
                    }
                }
            } else {
                // HTTP 4xx, 5xx
                BaseResult.Error(
                    ApiError(
                        message = "서버 오류: ${response.message()}",
                        code = response.code(),
                        type = ApiError.getErrorType(response.code())
                    )
                )
            }
        } catch (e: SocketTimeoutException) {
            Timber.e(e, "Timeout error")
            BaseResult.Error(ApiError.timeoutError())
        } catch (e: IOException) {
            Timber.e(e, "Network error")
            BaseResult.Error(ApiError.networkError())
        } catch (e: Exception) {
            Timber.e(e, "Unknown error")
            BaseResult.Error(ApiError.unknownError(e.message ?: "알 수 없는 오류"))
        }
    }

    /**
     * BaseResponse 없이 T를 직접 응답하는 API 호출
     * 주로 파일 다운로드, 외부 API 연동 등에서 사용
     */
    protected suspend fun <T> directApiCall(
        onSuccess: (suspend (T) -> Unit)? = null,
        apiCall: suspend () -> T
    ): BaseResult<T> {
        return try {
            val response = apiCall()
            onSuccess?.invoke(response)
            BaseResult.Success(response)

        } catch (e: HttpException) {
            Timber.e(e, "HTTP error: ${e.code()}")
            val errorBody = e.response()?.errorBody()?.string()
            val apiError = try {
                Gson().fromJson(errorBody, ApiError::class.java)
            } catch (ex: Exception) {
                ApiError(
                    code = e.code(),
                    message = e.message() ?: "API 호출에 실패했습니다",
                    type = ApiError.getErrorType(e.code())
                )
            }
            BaseResult.Error(apiError)

        } catch (e: IOException) {
            Timber.e(e, "Network error")
            BaseResult.Error(ApiError.networkError())

        } catch (e: Exception) {
            Timber.e(e, "Unknown error")
            BaseResult.Error(ApiError.unknownError(e.message ?: "알 수 없는 오류"))
        }
    }
}