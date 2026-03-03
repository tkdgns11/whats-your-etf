package com.whatsyouretf.userservice.domain.auth.service;

import com.whatsyouretf.userservice.common.exception.CustomException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.common.util.JwtTokenUtil;
import com.whatsyouretf.userservice.domain.auth.dto.AuthResponse;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserRepository userRepository;
    private final UserSocialAccountRepository socialAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenUtil jwtTokenUtil;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String kakaoClientSecret;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Kakao OAuth 로그인 처리
     */
    @Transactional
    public AuthResponse processKakaoCallback(String code) {
        // 1. Access Token 발급
        String kakaoAccessToken = getKakaoAccessToken(code);

        // 2. 사용자 정보 조회
        Map<String, Object> userInfo = getKakaoUserInfo(kakaoAccessToken);

        // 3. 사용자 찾기 또는 생성
        User user = findOrCreateUser(userInfo, SocialProvider.KAKAO);

        // 4. 로그인 처리
        user.updateLastLogin();

        // 5. JWT 토큰 발급
        return generateAuthResponse(user);
    }

    /**
     * Refresh Token으로 Access Token 재발급
     */
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

        // 4. 새 토큰 발급
        User user = refreshToken.getUser();
        return generateAuthResponse(user);
    }

    /**
     * 로그아웃 (Refresh Token 폐기)
     */
    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    private String getKakaoAccessToken(String code) {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("client_secret", kakaoClientSecret);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            Map<String, Object> body = response.getBody();

            if (body == null || !body.containsKey("access_token")) {
                throw new CustomException(ErrorCode.OAUTH_FAILED);
            }

            return (String) body.get("access_token");
        } catch (Exception e) {
            log.error("Failed to get Kakao access token: {}", e.getMessage());
            throw new CustomException(ErrorCode.OAUTH_FAILED);
        }
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
    private User findOrCreateUser(Map<String, Object> userInfo, SocialProvider provider) {
        String providerUserId = String.valueOf(userInfo.get("id"));

        // 1. 기존 소셜 계정으로 찾기
        return socialAccountRepository
                .findByProviderAndProviderUserIdWithUser(provider, providerUserId)
                .map(UserSocialAccount::getUser)
                .orElseGet(() -> {
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
                                .nickname(nickname)
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

    private AuthResponse generateAuthResponse(User user) {
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

        return AuthResponse.of(accessToken, refreshTokenValue, user);
    }
}
