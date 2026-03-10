package com.d102.wye.core.auth

import android.app.Activity
import com.d102.wye.BuildConfig
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient

/** 카카오 SDK 로그인 결과를 UI 계층에서 다루기 쉬운 형태로 전달한다. */
data class KakaoLoginProfile(
    val userId: Long,
    val nickname: String?,
)

/** 카카오 SDK 로그인과 사용자 프로필 조회를 감싼다. */
class KakaoLoginManager {

    /** 카카오 SDK 로그인 흐름을 시작하고 성공 시 프로필까지 조회한다. */
    fun login(
        activity: Activity,
        onSuccess: (KakaoLoginProfile) -> Unit,
        onError: (String) -> Unit,
    ) {
        if (BuildConfig.KAKAO_NATIVE_APP_KEY.startsWith("TODO_")) {
            onError("카카오 네이티브 앱 키를 먼저 설정해 주세요.")
            // TODO: 실제 네이티브 앱 키를 설정한 뒤 로그인 동작을 검증
            return
        }

        val callback: (OAuthToken?, Throwable?) -> Unit = { _, error ->
            when {
                error != null -> onError(error.message ?: "카카오 로그인 중 오류가 발생했습니다.")
                else -> requestUserProfile(onSuccess = onSuccess, onError = onError)
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
            UserApiClient.instance.loginWithKakaoTalk(activity, callback = callback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(activity, callback = callback)
        }
    }

    /** 로그인 완료 후 카카오 사용자 프로필을 조회한다. */
    private fun requestUserProfile(
        onSuccess: (KakaoLoginProfile) -> Unit,
        onError: (String) -> Unit,
    ) {
        UserApiClient.instance.me { user, error ->
            val userId = user?.id
            when {
                error != null -> onError(error.message ?: "카카오 사용자 정보를 불러오지 못했습니다.")
                user == null -> onError("카카오 사용자 정보가 비어 있습니다.")
                userId == null -> onError("카카오 사용자 식별자를 불러오지 못했습니다.")
                else -> onSuccess(
                    KakaoLoginProfile(
                        userId = userId,
                        nickname = user.kakaoAccount?.profile?.nickname,
                    )
                )
            }
        }
    }
}
