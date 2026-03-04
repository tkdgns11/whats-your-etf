package com.d102.wye.domain.common

sealed class BaseResult<out T> {
    data class Success<T>(val data: T) : BaseResult<T>()
    data class Error(val error: ApiError) : BaseResult<Nothing>()
}
