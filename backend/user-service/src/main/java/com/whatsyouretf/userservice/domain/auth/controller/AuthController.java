package com.whatsyouretf.userservice.domain.auth.controller;

import com.whatsyouretf.userservice.common.auth.CustomUserDetails;
import com.whatsyouretf.userservice.common.response.ApiResponse;
import com.whatsyouretf.userservice.domain.auth.dto.AuthResponse;
import com.whatsyouretf.userservice.domain.auth.dto.AuthUrl;
import com.whatsyouretf.userservice.domain.auth.dto.TokenRefreshRequest;
import com.whatsyouretf.userservice.domain.auth.service.OAuth2Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OAuth2Service oAuth2Service;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Operation(summary = "Kakao OAuth URL 조회", description = "카카오 로그인 페이지 URL을 반환합니다.")
    @GetMapping("/oauth/kakao")
    public ResponseEntity<ApiResponse<AuthUrl>> getKakaoAuthUrl() {
        String authUrl = String.format(
                "https://kauth.kakao.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code&scope=profile_nickname,profile_image,account_email",
                kakaoClientId,
                kakaoRedirectUri
        );
        return ResponseEntity.ok(ApiResponse.success(AuthUrl.of(authUrl)));
    }

    @Operation(summary = "Kakao OAuth Callback", description = "카카오 로그인 콜백을 처리합니다.")
    @GetMapping("/oauth/kakao/callback")
    public ResponseEntity<ApiResponse<AuthResponse>> kakaoCallback(@RequestParam String code) {
        AuthResponse response = oAuth2Service.processKakaoCallback(code);
        return ResponseEntity.ok(ApiResponse.success("로그인 성공", response));
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 Access Token을 재발급합니다.")
    @PostMapping("/token/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request
    ) {
        AuthResponse response = oAuth2Service.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("토큰 갱신 성공", response));
    }

    @Operation(summary = "로그아웃", description = "로그아웃 처리 (Refresh Token 폐기)")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        oAuth2Service.logout(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("로그아웃 성공"));
    }
}
