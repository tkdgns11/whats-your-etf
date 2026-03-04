package com.d102.wye.core.network

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.d102.wye.core.app.Constants
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject

/**
 * JWT Token을 요청 헤더에 추가하는 Interceptor
 */
class AuthTokenInterceptor @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : Interceptor {

    companion object {
        // 토큰이 필요 없는 URL 패턴
        // 예시: 로그인, 회원가입 ...
        private val PUBLIC_URLS = listOf(
            "/api/example",
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        // 공개 URL이면 토큰 추가하지 않음
        if (PUBLIC_URLS.any { path.contains(it) }) {
            Timber.d("Public URL, skipping token: $path")
            return chain.proceed(request)
        }

        // 토큰 읽기
        val token = runBlocking {
            dataStore.data.first()[stringPreferencesKey(Constants.KEY_ACCESS_TOKEN)]
        }

        // 토큰이 있으면 추가
        val newRequest = if (!token.isNullOrEmpty()) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            Timber.w("No token found for protected URL: $path")
            request
        }

        if (!token.isNullOrEmpty()) {
            Timber.d("Added JWT token to request: $path")
        }

        val response = chain.proceed(newRequest)

        // 401 에러 (토큰 만료) 처리
        if (response.code == 401) {
            Timber.w("Access token expired (401)")

            runBlocking {
                // 로컬 데이터 삭제
                clearAuthData()
            }
        }

        return response
    }

    /**
     * 인증 데이터 삭제
     */
    private suspend fun clearAuthData() {
        dataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey(Constants.KEY_ACCESS_TOKEN))
            preferences.remove(stringPreferencesKey(Constants.KEY_REFRESH_TOKEN))
            preferences.remove(longPreferencesKey(Constants.KEY_USER_ID))
            preferences[booleanPreferencesKey(Constants.KEY_IS_LOGGED_IN)] = false
        }
        Timber.d("Auth data cleared due to token expiration")
    }
}