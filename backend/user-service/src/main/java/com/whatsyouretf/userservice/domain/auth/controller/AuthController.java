package com.whatsyouretf.userservice.domain.auth.controller;

import com.whatsyouretf.userservice.common.auth.CustomUserDetails;
import com.whatsyouretf.userservice.common.response.ApiResponse;
import com.whatsyouretf.userservice.domain.auth.dto.*;
import com.whatsyouretf.userservice.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 인증 관련 API Controller
 * <p>
 * 담당 기능:
 * - 카카오 OAuth 로그인
 * - 이메일 회원가입/로그인
 * - 토큰 관리 (갱신, 로그아웃)
 * - 비밀번호 재설정
 */
@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ========== OAuth ==========

    /**
     * 카카오 모바일 로그인
     * <p>
     * Android/iOS 카카오 SDK에서 발급받은 access_token으로 로그인합니다.
     * 신규 회원은 자동으로 계정이 생성됩니다.
     *
     * @param request 카카오 access_token
     * @return JWT 토큰 및 사용자 정보
     */
    @Operation(summary = "카카오 로그인", description = "Android/iOS SDK에서 받은 access_token으로 로그인합니다.")
    @PostMapping("/oauth/kakao")
    public ResponseEntity<ApiResponse<AuthResponse>> kakaoLogin(
            @Valid @RequestBody KakaoMobileLoginRequest request
    ) {
        AuthResponse response = authService.processKakaoLogin(request.getAccessToken());
        return ResponseEntity.ok(ApiResponse.success("로그인 성공", response));
    }

    // ========== 이메일 회원가입 ==========

    /**
     * 이메일 회원가입
     * <p>
     * 이메일 인증을 통한 회원가입을 요청합니다.
     * 인증 이메일이 발송되며, 인증 완료 후 계정이 생성됩니다.
     *
     * @param request 이메일, 비밀번호, 닉네임
     */
    @Operation(summary = "회원가입", description = "이메일 인증을 통한 회원가입을 요청합니다.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Map<String, String>>> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        authService.signup(request);
        return ResponseEntity.ok(ApiResponse.success(
                "인증 이메일이 발송되었습니다. 이메일을 확인해주세요.",
                Map.of("email", request.getEmail())
        ));
    }

    /**
     * 이메일 인증 확인
     * <p>
     * 이메일로 발송된 6자리 인증 코드를 확인합니다.
     * 인증 성공 시 계정이 생성되고 JWT 토큰이 발급됩니다.
     *
     * @param request 이메일, 인증 코드
     * @return JWT 토큰 및 사용자 정보
     */
    @Operation(summary = "이메일 인증 확인", description = "이메일로 발송된 인증 코드를 확인합니다.")
    @PostMapping("/signup/verify")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyEmail(
            @Valid @RequestBody EmailVerifyRequest request
    ) {
        AuthResponse response = authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다.", response));
    }

    /**
     * 인증 이메일 재발송
     * <p>
     * 인증 코드가 만료되었거나 받지 못한 경우 재발송을 요청합니다.
     *
     * @param request 이메일
     */
    @Operation(summary = "인증 이메일 재발송", description = "인증 이메일을 재발송합니다.")
    @PostMapping("/signup/resend")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @Valid @RequestBody EmailResendRequest request
    ) {
        authService.resendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("인증 이메일이 재발송되었습니다."));
    }

    // ========== 이메일 로그인 ==========

    /**
     * 이메일 + 비밀번호 로그인
     *
     * @param request 이메일, 비밀번호
     * @return JWT 토큰 및 사용자 정보
     */
    @Operation(summary = "이메일 로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("로그인 성공", response));
    }

    // ========== 토큰 ==========

    /**
     * Access Token 갱신
     * <p>
     * Refresh Token으로 새로운 Access Token을 발급받습니다.
     *
     * @param request Refresh Token
     * @return 새로운 Access Token
     */
    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 Access Token을 재발급합니다.")
    @PostMapping("/token/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request
    ) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("토큰 갱신 성공", response));
    }

    /**
     * 로그아웃
     * <p>
     * Refresh Token을 폐기하여 로그아웃 처리합니다.
     */
    @Operation(summary = "로그아웃", description = "로그아웃 처리 (Refresh Token 폐기)")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authService.logout(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("로그아웃되었습니다."));
    }

    // ========== 비밀번호 재설정 ==========

    /**
     * 비밀번호 재설정 요청
     * <p>
     * 비밀번호 재설정 링크/코드를 이메일로 발송합니다.
     * 보안상 존재하지 않는 이메일이어도 동일한 응답을 반환합니다.
     *
     * @param request 이메일
     */
    @Operation(summary = "비밀번호 재설정 요청", description = "비밀번호 재설정 이메일을 발송합니다.")
    @PostMapping("/password/reset/request")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request
    ) {
        authService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("비밀번호 재설정 이메일이 발송되었습니다."));
    }

    /**
     * 비밀번호 재설정 토큰 검증
     * <p>
     * 이메일로 발송된 재설정 코드의 유효성을 확인합니다.
     *
     * @param request 이메일, 토큰
     * @return 토큰 유효 여부
     */
    @Operation(summary = "비밀번호 재설정 토큰 검증", description = "재설정 토큰의 유효성을 확인합니다.")
    @PostMapping("/password/reset/verify")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> verifyResetToken(
            @Valid @RequestBody PasswordResetVerifyRequest request
    ) {
        boolean valid = authService.verifyPasswordResetToken(request.getEmail(), request.getToken());
        return ResponseEntity.ok(ApiResponse.success(Map.of("valid", valid)));
    }

    /**
     * 비밀번호 재설정
     * <p>
     * 인증된 토큰으로 새 비밀번호를 설정합니다.
     * 완료 후 새 비밀번호로 재로그인이 필요합니다.
     *
     * @param request 이메일, 토큰, 새 비밀번호
     */
    @Operation(summary = "비밀번호 재설정", description = "새 비밀번호를 설정합니다.")
    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody PasswordResetConfirmRequest request
    ) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 재설정되었습니다. 새 비밀번호로 로그인해주세요."));
    }
}
