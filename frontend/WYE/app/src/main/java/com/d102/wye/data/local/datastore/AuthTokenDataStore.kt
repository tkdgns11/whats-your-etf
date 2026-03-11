package com.d102.wye.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.d102.wye.core.app.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * JWT 토큰 및 로그인 상태를 DataStore에 저장/조회하는 클래스
 *
 * AuthTokenInterceptor, TokenRefreshInterceptor, AuthRepositoryImpl에서 주입받아 사용
 *
 * Flow를 반환하는 이유:
 * DataStore는 기본적으로 Flow 기반이라 값이 바뀌면 자동으로 새 값을 emit함
 * → 로그인 상태 변화를 ViewModel에서 실시간으로 감지 가능
 */
@Singleton
class AuthTokenDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    // ─────────────────────────────────────────
    // Keys (Constants에서 문자열 관리)
    // ─────────────────────────────────────────

    private val accessTokenKey = stringPreferencesKey(Constants.KEY_ACCESS_TOKEN)
    private val refreshTokenKey = stringPreferencesKey(Constants.KEY_REFRESH_TOKEN)
    private val authProviderKey = stringPreferencesKey(Constants.KEY_AUTH_PROVIDER)
    private val userNicknameKey = stringPreferencesKey(Constants.KEY_USER_NICKNAME)
    private val kakaoUserIdKey = stringPreferencesKey(Constants.KEY_KAKAO_USER_ID)
    private val isLoggedInKey = booleanPreferencesKey(Constants.KEY_IS_LOGGED_IN)

    // ─────────────────────────────────────────
    // Read (Flow)
    // ─────────────────────────────────────────

    /** Access Token Flow — AuthTokenInterceptor에서 runBlocking으로 수집 */
    val accessToken: Flow<String?> = dataStore.data.map { it[accessTokenKey] }

    /** Refresh Token Flow — TokenRefreshInterceptor에서 runBlocking으로 수집 */
    val refreshToken: Flow<String?> = dataStore.data.map { it[refreshTokenKey] }

    /** 로그인 상태 Flow — MainActivity/AuthViewModel에서 startDestination 분기에 사용 */
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { it[isLoggedInKey] ?: false }

    /** 현재 로그인 제공자 Flow — 이메일/카카오 구분이 필요할 때 사용 */
    val authProvider: Flow<String?> = dataStore.data.map { it[authProviderKey] }

    /** 현재 사용자 닉네임 Flow — 임시 소셜 로그인 상태 표시에 사용 */
    val userNickname: Flow<String?> = dataStore.data.map { it[userNicknameKey] }

    // ─────────────────────────────────────────
    // Write
    // ─────────────────────────────────────────

    /**
     * 로그인 성공 시 토큰 저장
     * AuthRepositoryImpl.login() 성공 콜백에서 호출
     */
    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[accessTokenKey] = accessToken
            preferences[refreshTokenKey] = refreshToken
            preferences[authProviderKey] = "server"
            preferences.remove(userNicknameKey)
            preferences.remove(kakaoUserIdKey)
            preferences[isLoggedInKey] = true
        }
    }

    /**
     * 카카오 SDK 로그인 성공 후 서버 없이 임시 세션을 저장한다.
     * TODO: 서버 연동 시 카카오 사용자 정보 저장 대신 서버 JWT 저장으로 교체
     */
    suspend fun saveKakaoSession(userId: Long, nickname: String?) {
        dataStore.edit { preferences ->
            preferences.remove(accessTokenKey)
            preferences.remove(refreshTokenKey)
            preferences[authProviderKey] = "kakao"
            preferences[kakaoUserIdKey] = userId.toString()
            if (nickname.isNullOrBlank()) {
                preferences.remove(userNicknameKey)
            } else {
                preferences[userNicknameKey] = nickname
            }
            preferences[isLoggedInKey] = true
        }
    }

    /**
     * 토큰 재발급 시 Access Token만 갱신
     * TokenRefreshInterceptor에서 호출
     */
    suspend fun updateAccessToken(accessToken: String) {
        dataStore.edit { preferences ->
            preferences[accessTokenKey] = accessToken
        }
    }

    /**
     * 토큰 재발급 시 양쪽 모두 갱신
     * TokenRefreshInterceptor에서 호출
     */
    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[accessTokenKey] = accessToken
            preferences[refreshTokenKey] = refreshToken
        }
    }

    /**
     * 로그아웃 / 토큰 만료 시 전체 삭제
     * AuthRepositoryImpl.logout() 또는 TokenRefreshInterceptor 재발급 실패 시 호출
     */
    suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(accessTokenKey)
            preferences.remove(refreshTokenKey)
            preferences.remove(authProviderKey)
            preferences.remove(userNicknameKey)
            preferences.remove(kakaoUserIdKey)
            preferences[isLoggedInKey] = false
        }
    }
}
