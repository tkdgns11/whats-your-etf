package com.whatsyouretf.userservice.domain.auth.service.impl;

import com.whatsyouretf.userservice.common.exception.CustomException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.common.util.JwtTokenUtil;
import com.whatsyouretf.userservice.domain.auth.dto.AuthResponse;
import com.whatsyouretf.userservice.domain.auth.service.AuthService;
import com.whatsyouretf.userservice.domain.user.entity.RefreshToken;
import com.whatsyouretf.userservice.domain.user.entity.User;
import com.whatsyouretf.userservice.domain.user.entity.UserSocialAccount;
import com.whatsyouretf.userservice.domain.user.entity.UserSocialAccount.SocialProvider;
import com.whatsyouretf.userservice.domain.user.repository.RefreshTokenRepository;
import com.whatsyouretf.userservice.domain.user.repository.UserRepository;
import com.whatsyouretf.userservice.domain.user.repository.UserSocialAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserSocialAccountRepository socialAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenUtil jwtTokenUtil;

    @Value("${jwt.access-token-validity}")
    private Long accessTokenValidity;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Transactional
    public AuthResponse processKakaoLogin(String accessToken) {
        // 1. 카카오에서 사용자 정보 조회
        Map<String, Object> userInfo = getKakaoUserInfo(accessToken);

        // 2. 사용자 찾기 또는 생성
        AtomicBoolean isNewUser = new AtomicBoolean(false);
        User user = findOrCreateUser(userInfo, SocialProvider.KAKAO, isNewUser);

        // 3. 로그인 처리
        user.updateLastLogin();

        // 4. JWT 토큰 발급
        return generateAuthResponse(user, isNewUser.get(), "KAKAO");
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshTokenValue) {
        // 1. Refresh Token 유효성 검증
        if (!jwtTokenUtil.validateToken(refreshTokenValue) || !jwtTokenUtil.isRefreshToken(refreshTokenValue)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // 2. DB에서 Refresh Token 조회
        RefreshToken refreshToken = refreshTokenRepository
                .findValidToken(refreshTokenValue, LocalDateTime.now())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

        // 3. 기존 토큰 폐기
        refreshToken.revoke();

        // 4. 새 Access Token만 발급
        User user = refreshToken.getUser();
        String newAccessToken = jwtTokenUtil.createAccessToken(user.getId());

        return AuthResponse.ofRefresh(newAccessToken, accessTokenValidity / 1000);
    }

    @Override
    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("User logged out: {}", userId);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getKakaoUserInfo(String accessToken) {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    userInfoUrl, HttpMethod.GET, request, Map.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to get Kakao user info: {}", e.getMessage());
            throw new CustomException(ErrorCode.OAUTH_FAILED);
        }
    }

    @SuppressWarnings("unchecked")
    private User findOrCreateUser(Map<String, Object> userInfo, SocialProvider provider, AtomicBoolean isNewUser) {
        String providerUserId = String.valueOf(userInfo.get("id"));

        // 1. 기존 소셜 계정으로 찾기
        return socialAccountRepository
                .findByProviderAndProviderUserIdWithUser(provider, providerUserId)
                .map(UserSocialAccount::getUser)
                .orElseGet(() -> {
                    isNewUser.set(true);

                    // 2. 사용자 정보 추출
                    Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
                    Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

                    String email = (String) kakaoAccount.get("email");
                    String nickname = (String) profile.get("nickname");
                    String profileImage = (String) profile.get("profile_image_url");

                    // 3. 이메일로 기존 사용자 찾기
                    User user = email != null
                            ? userRepository.findByEmail(email).orElse(null)
                            : null;

                    // 4. 신규 사용자 생성
                    if (user == null) {
                        user = User.builder()
                                .email(email != null ? email : providerUserId + "@kakao.user")
                                .nickname(nickname != null ? nickname : "user_" + providerUserId.substring(0, 8))
                                .profileImage(profileImage)
                                .build();
                        user = userRepository.save(user);
                    }

                    // 5. 소셜 계정 연동
                    UserSocialAccount socialAccount = UserSocialAccount.builder()
                            .provider(provider)
                            .providerUserId(providerUserId)
                            .email(email)
                            .isPrimary(true)
                            .build();
                    user.addSocialAccount(socialAccount);
                    socialAccountRepository.save(socialAccount);

                    return user;
                });
    }

    private AuthResponse generateAuthResponse(User user, boolean isNewUser, String provider) {
        String accessToken = jwtTokenUtil.createAccessToken(user.getId());
        String refreshTokenValue = jwtTokenUtil.createRefreshToken(user.getId());

        // Refresh Token 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiresAt(jwtTokenUtil.getExpirationFromToken(refreshTokenValue)
                        .toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime())
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.of(
                accessToken,
                refreshTokenValue,
                accessTokenValidity / 1000,  // 밀리초 -> 초
                isNewUser,
                user,
                provider
        );
    }
}
