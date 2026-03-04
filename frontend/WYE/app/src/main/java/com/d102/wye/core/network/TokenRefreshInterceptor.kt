package com.d102.wye.core.network

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.d102.wye.core.app.Constants
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

/**
 * Access Token 만료 시 Refresh Token으로 재발급을 시도하는 Interceptor
 *
 * 동작 순서:
 * 1. 요청 실행
 * 2. 응답이 401이면 Refresh Token으로 새 Access Token 요청
 * 3. 재발급 성공 → DataStore에 저장 후 원래 요청 재시도
 * 4. 재발급 실패 (Refresh Token도 만료) → 로그아웃 처리
 *
 * AuthTokenInterceptor와의 역할 분리:
 * - AuthTokenInterceptor → 모든 요청에 Access Token 헤더 추가
 * - TokenRefreshInterceptor → 401 발생 시 토큰 재발급 + 재시도
 */
class TokenRefreshInterceptor @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val response = chain.proceed(originalRequest)

        // 401이 아니면 그대로 반환
        if (response.code != 401) return response

        Timber.w("Access token expired (401), attempting token refresh")

        // Refresh Token 읽기
        val refreshToken = runBlocking {
            dataStore.data.first()[stringPreferencesKey(Constants.KEY_REFRESH_TOKEN)]
        }

        // Refresh Token 없으면 로그아웃 처리
        if (refreshToken.isNullOrEmpty()) {
            Timber.e("No refresh token found, clearing auth data")
            runBlocking { clearAuthData() }
            return response
        }

        // Refresh Token으로 새 Access Token 요청
        val newTokenPair = runBlocking { refreshAccessToken(refreshToken) }

        // 재발급 실패 시 로그아웃 처리
        if (newTokenPair == null) {
            Timber.e("Token refresh failed, clearing auth data")
            runBlocking { clearAuthData() }
            return response
        }

        // 새 토큰 DataStore에 저장
        runBlocking {
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey(Constants.KEY_ACCESS_TOKEN)] = newTokenPair.first
                preferences[stringPreferencesKey(Constants.KEY_REFRESH_TOKEN)] = newTokenPair.second
            }
        }

        Timber.d("Token refreshed successfully, retrying original request")

        // 원래 요청을 새 Access Token으로 재시도
        response.close()
        val retryRequest = originalRequest.newBuilder()
            .removeHeader("Authorization")
            .addHeader("Authorization", "Bearer ${newTokenPair.first}")
            .build()

        return chain.proceed(retryRequest)
    }

    /**
     * Refresh Token으로 새 Access Token 발급 요청
     * OkHttpClient를 직접 생성해서 호출 (인터셉터 순환 방지)
     *
     * @return Pair(newAccessToken, newRefreshToken) or null (재발급 실패)
     */
    private fun refreshAccessToken(refreshToken: String): Pair<String, String>? {
        return try {
            // 인터셉터가 붙지 않은 순수 OkHttpClient 사용 (무한 루프 방지)
            val client = OkHttpClient()

            val body = JSONObject()
                .put("refreshToken", refreshToken)
                .toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("${Constants.BASE_URL}/auth/refresh")
                .post(body)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                Timber.w("Token refresh request failed with code: ${response.code}")
                return null
            }

            val responseBody = response.body?.string() ?: return null
            val json = JSONObject(responseBody)

            // 서버 응답 구조에 맞게 파싱 경로 수정
            val data = json.optJSONObject("data") ?: return null
            val newAccessToken = data.optString("accessToken").takeIf { it.isNotEmpty() } ?: return null
            val newRefreshToken = data.optString("refreshToken").takeIf { it.isNotEmpty() } ?: return null

            Pair(newAccessToken, newRefreshToken)
        } catch (e: Exception) {
            Timber.e(e, "Exception during token refresh")
            null
        }
    }

    /**
     * 인증 데이터 전체 삭제 (로그아웃 처리)
     */
    private suspend fun clearAuthData() {
        dataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey(Constants.KEY_ACCESS_TOKEN))
            preferences.remove(stringPreferencesKey(Constants.KEY_REFRESH_TOKEN))
            preferences[booleanPreferencesKey(Constants.KEY_IS_LOGGED_IN)] = false
        }
        Timber.d("Auth data cleared due to refresh token expiration")
    }
}