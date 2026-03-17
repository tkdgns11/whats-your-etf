package com.d102.wye.data.repository

import com.d102.wye.core.app.Constants
import com.d102.wye.data.remote.dto.response.BaseResponse
import com.d102.wye.domain.common.ApiError
import com.d102.wye.domain.common.BaseResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * 모든 RepositoryImpl의 공통 베이스
 *
 * safeApiCall vs directApiCall:
 * - safeApiCall    : 서버가 BaseResponse<T> 래퍼로 응답할 때 사용
 * - directApiCall  : BaseResponse 없이 T를 바로 응답할 때 사용 (파일 다운로드 등)
 */
abstract class BaseRepository {

    private fun resolveServerCode(serverCode: String?, fallbackCode: Int): Int {
        return serverCode?.toIntOrNull() ?: fallbackCode
    }

    /** HTTP 에러 응답의 BaseResponse message/code를 읽어 인증 에러 메시지를 그대로 노출한다. */
    private fun parseErrorResponse(response: Response<*>): ApiError {
        val fallbackCode = response.code()
        val fallbackMessage = when (fallbackCode) {
            401, 403 -> Constants.ERROR_SESSION_EXPIRED
            else -> "서버 오류: ${response.message()}"
        }

        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody.isNullOrBlank()) {
                ApiError(
                    message = fallbackMessage,
                    code = fallbackCode,
                    type = ApiError.getErrorType(fallbackCode)
                )
            } else {
                val responseType = object : TypeToken<BaseResponse<Any>>() {}.type
                val parsedBody: BaseResponse<Any> = Gson().fromJson(errorBody, responseType)
                val resolvedCode = resolveServerCode(
                    serverCode = parsedBody.code,
                    fallbackCode = fallbackCode
                )
                ApiError(
                    message = parsedBody.message ?: fallbackMessage,
                    code = resolvedCode,
                    type = ApiError.getErrorType(resolvedCode)
                )
            }
        } catch (e: Exception) {
            Timber.w(e, "Failed to parse error body")
            ApiError(
                message = fallbackMessage,
                code = fallbackCode,
                type = ApiError.getErrorType(fallbackCode)
            )
        }
    }

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
                        val resolvedCode = resolveServerCode(
                            serverCode = body.code,
                            fallbackCode = response.code()
                        )
                        BaseResult.Error(
                            ApiError(
                                message = body.message ?: "알 수 없는 오류",
                                code = resolvedCode,
                                type = ApiError.getErrorType(resolvedCode)
                            )
                        )
                    }
                }
            } else {
                // HTTP 4xx, 5xx
                BaseResult.Error(parseErrorResponse(response))
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

    /** data 없이 성공 여부만 내려오는 API 응답을 공통 처리한다. */
    protected suspend fun safeApiCallWithoutData(
        onSuccess: (suspend () -> Unit)? = null,
        apiCall: suspend () -> Response<BaseResponse<Unit>>
    ): BaseResult<Unit> {
        return try {
            val response = apiCall()

            if (response.isSuccessful) {
                val body = response.body()

                when {
                    // data가 없는 API도 본문 success/code를 확인해 비즈니스 에러를 놓치지 않는다.
                    body == null -> {
                        BaseResult.Error(
                            ApiError(
                                message = "응답 데이터가 없습니다",
                                code = response.code(),
                                type = ApiError.ErrorType.UNKNOWN
                            )
                        )
                    }

                    body.success == false -> {
                        val resolvedCode = resolveServerCode(
                            serverCode = body.code,
                            fallbackCode = response.code()
                        )
                        BaseResult.Error(
                            ApiError(
                                message = body.message ?: "알 수 없는 오류",
                                code = resolvedCode,
                                type = ApiError.getErrorType(resolvedCode)
                            )
                        )
                    }

                    body.code != null && body.code != "OK" -> {
                        val resolvedCode = resolveServerCode(
                            serverCode = body.code,
                            fallbackCode = response.code()
                        )
                        BaseResult.Error(
                            ApiError(
                                message = body.message ?: "알 수 없는 오류",
                                code = resolvedCode,
                                type = ApiError.getErrorType(resolvedCode)
                            )
                        )
                    }

                    else -> {
                        onSuccess?.invoke()
                        BaseResult.Success(Unit)
                    }
                }
            } else {
                BaseResult.Error(parseErrorResponse(response))
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
