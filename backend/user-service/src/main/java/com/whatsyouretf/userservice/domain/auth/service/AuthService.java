package com.whatsyouretf.userservice.domain.auth.service;

import com.whatsyouretf.userservice.domain.auth.dto.AuthResponse;

public interface AuthService {

    /**
     * 카카오 로그인 (모바일 SDK access_token 기반)
     */
    AuthResponse processKakaoLogin(String accessToken);

    /**
     * Refresh Token으로 Access Token 재발급
     */
    AuthResponse refreshToken(String refreshToken);

    /**
     * 로그아웃 (Refresh Token 폐기)
     */
    void logout(Long userId);
}
