package com.whatsyouretf.userservice.domain.user.controller;

import com.whatsyouretf.userservice.common.auth.CustomUserDetails;
import com.whatsyouretf.userservice.common.response.ApiResponse;
import com.whatsyouretf.userservice.domain.user.dto.*;
import com.whatsyouretf.userservice.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 관련 API 컨트롤러
 * <p>
 * 사용자 정보 조회/수정, 관심 ETF, 보유 ETF(마이데이터) 기능을 제공합니다.
 */
@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    // ==================== 사용자 정보 ====================

    /**
     * 내 정보 조회
     *
     * @param userDetails 인증된 사용자 정보
     * @return 사용자 정보
     */
    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserResponse response = userService.getMyInfo(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프로필 수정
     *
     * @param userDetails 인증된 사용자 정보
     * @param request     수정할 프로필 정보
     * @return 수정된 사용자 정보
     */
    @Operation(summary = "프로필 수정", description = "사용자 프로필을 수정합니다.")
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        UserResponse response = userService.updateProfile(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("프로필 수정 성공", response));
    }

    /**
     * 닉네임 중복 체크
     *
     * @param nickname 확인할 닉네임
     * @return true = 사용 가능, false = 중복
     */
    @Operation(summary = "닉네임 중복 체크", description = "닉네임 사용 가능 여부를 확인합니다.")
    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResponse<Boolean>> checkNickname(@RequestParam String nickname) {
        boolean isDuplicate = userService.checkNicknameDuplicate(nickname);
        return ResponseEntity.ok(ApiResponse.success(!isDuplicate));
    }

    /**
     * 특정 사용자 조회
     *
     * @param userId 사용자 ID
     * @return 사용자 공개 정보
     */
    @Operation(summary = "사용자 조회", description = "특정 사용자의 공개 정보를 조회합니다.")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long userId) {
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 회원 탈퇴
     *
     * @param userDetails 인증된 사용자 정보
     * @return 성공 응답
     */
    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 진행합니다.")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deactivateAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userService.deactivateUser(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("회원 탈퇴 완료"));
    }

    // ==================== 관심 ETF ====================

    /**
     * 관심 ETF 목록 조회
     *
     * @param userDetails 인증된 사용자 정보
     * @return 관심 ETF 목록 (최신 시세 포함)
     */
    @Operation(summary = "관심 ETF 목록 조회", description = "로그인한 사용자의 관심 ETF 목록을 조회합니다.")
    @GetMapping("/me/favorites")
    public ResponseEntity<ApiResponse<FavoriteEtfListResponse>> getFavoriteEtfs(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        FavoriteEtfListResponse response = userService.getFavoriteEtfs(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 관심 ETF 추가
     *
     * @param userDetails 인증된 사용자 정보
     * @param etfId       ETF ID
     * @return 성공 응답
     */
    @Operation(summary = "관심 ETF 추가", description = "ETF를 관심 목록에 추가합니다.")
    @PostMapping("/me/favorites/{etfId}")
    public ResponseEntity<ApiResponse<Void>> addFavoriteEtf(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "ETF ID") @PathVariable Long etfId
    ) {
        userService.addFavoriteEtf(userDetails.getUserId(), etfId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("관심 ETF 추가 완료"));
    }

    /**
     * 관심 ETF 삭제
     *
     * @param userDetails 인증된 사용자 정보
     * @param etfId       ETF ID
     * @return 성공 응답
     */
    @Operation(summary = "관심 ETF 삭제", description = "ETF를 관심 목록에서 삭제합니다.")
    @DeleteMapping("/me/favorites/{etfId}")
    public ResponseEntity<ApiResponse<Void>> removeFavoriteEtf(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "ETF ID") @PathVariable Long etfId
    ) {
        userService.removeFavoriteEtf(userDetails.getUserId(), etfId);
        return ResponseEntity.ok(ApiResponse.success("관심 ETF 삭제 완료"));
    }

    /**
     * 관심 ETF 여부 확인
     *
     * @param userDetails 인증된 사용자 정보
     * @param etfId       ETF ID
     * @return true = 관심 등록됨
     */
    @Operation(summary = "관심 ETF 여부 확인", description = "특정 ETF가 관심 목록에 있는지 확인합니다.")
    @GetMapping("/me/favorites/{etfId}/check")
    public ResponseEntity<ApiResponse<Boolean>> checkFavoriteEtf(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "ETF ID") @PathVariable Long etfId
    ) {
        boolean isFavorite = userService.isFavoriteEtf(userDetails.getUserId(), etfId);
        return ResponseEntity.ok(ApiResponse.success(isFavorite));
    }

    // ==================== 보유 ETF (마이데이터) ====================

    /**
     * 보유 ETF 목록 조회
     *
     * @param userDetails 인증된 사용자 정보
     * @return 보유 ETF 목록 (평가금액, 손익 포함)
     */
    @Operation(summary = "보유 ETF 목록 조회", description = "로그인한 사용자의 보유 ETF 목록을 조회합니다.")
    @GetMapping("/me/holdings")
    public ResponseEntity<ApiResponse<HoldingEtfListResponse>> getHoldingEtfs(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        HoldingEtfListResponse response = userService.getHoldingEtfs(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 마이데이터 동기화
     * <p>
     * 현재는 Mock 데이터로 동작합니다.
     *
     * @param userDetails 인증된 사용자 정보
     * @return 동기화된 보유 ETF 목록
     */
    @Operation(summary = "마이데이터 동기화", description = "마이데이터에서 보유 ETF 정보를 동기화합니다. (Mock)")
    @PostMapping("/me/holdings/sync")
    public ResponseEntity<ApiResponse<HoldingEtfListResponse>> syncMyData(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        HoldingEtfListResponse response = userService.syncMyData(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("마이데이터 동기화 완료", response));
    }
}
