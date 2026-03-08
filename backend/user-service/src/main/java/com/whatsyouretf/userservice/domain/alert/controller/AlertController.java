package com.whatsyouretf.userservice.domain.alert.controller;

import com.whatsyouretf.userservice.common.auth.CustomUserDetails;
import com.whatsyouretf.userservice.common.response.ApiResponse;
import com.whatsyouretf.userservice.domain.alert.dto.*;
import com.whatsyouretf.userservice.domain.alert.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 알림 API 컨트롤러
 */
@Tag(name = "Alerts", description = "알림 API")
@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    /**
     * 알림 목록 조회
     */
    @Operation(summary = "알림 목록 조회", description = "사용자의 알림 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<AlertListResponse>> getAlerts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "카테고리 필터 (all/ETF/PORTFOLIO/NEWS/SYSTEM)") @RequestParam(defaultValue = "all") String category
    ) {
        AlertListResponse response = alertService.getAlerts(userDetails.getUserId(), page, size, category);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 읽지 않은 알림 수 조회
     */
    @Operation(summary = "읽지 않은 알림 수", description = "읽지 않은 알림 개수를 조회합니다.")
    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UnreadCountResponse response = alertService.getUnreadCount(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 알림 읽음 처리
     */
    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 처리합니다.")
    @PutMapping("/{alertId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "알림 ID") @PathVariable Long alertId
    ) {
        alertService.markAsRead(userDetails.getUserId(), alertId);
        return ResponseEntity.ok(ApiResponse.success("알림을 읽음 처리했습니다."));
    }

    /**
     * 모든 알림 읽음 처리
     */
    @Operation(summary = "모든 알림 읽음 처리", description = "모든 알림을 읽음 처리합니다.")
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<UpdateCountResponse>> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        int count = alertService.markAllAsRead(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(UpdateCountResponse.updated(count)));
    }

    /**
     * 알림 삭제
     */
    @Operation(summary = "알림 삭제", description = "특정 알림을 삭제합니다.")
    @DeleteMapping("/{alertId}")
    public ResponseEntity<ApiResponse<Void>> deleteAlert(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "알림 ID") @PathVariable Long alertId
    ) {
        alertService.deleteAlert(userDetails.getUserId(), alertId);
        return ResponseEntity.ok(ApiResponse.success("알림이 삭제되었습니다."));
    }

    /**
     * 읽은 알림 전체 삭제
     */
    @Operation(summary = "읽은 알림 전체 삭제", description = "읽은 알림을 모두 삭제합니다.")
    @DeleteMapping
    public ResponseEntity<ApiResponse<UpdateCountResponse>> deleteAllReadAlerts(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        int count = alertService.deleteAllReadAlerts(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(UpdateCountResponse.deleted(count)));
    }

    /**
     * FCM 토큰 등록
     */
    @Operation(summary = "FCM 토큰 등록", description = "푸시 알림을 위한 FCM 토큰을 등록합니다.")
    @PostMapping("/fcm/token")
    public ResponseEntity<ApiResponse<Void>> registerFcmToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody FcmTokenRequest request
    ) {
        alertService.registerFcmToken(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("FCM 토큰이 등록되었습니다."));
    }

    /**
     * FCM 토큰 삭제
     */
    @Operation(summary = "FCM 토큰 삭제", description = "FCM 토큰을 삭제합니다.")
    @DeleteMapping("/fcm/token")
    public ResponseEntity<ApiResponse<Void>> deleteFcmToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody FcmTokenRequest request
    ) {
        alertService.deleteFcmToken(userDetails.getUserId(), request.getToken());
        return ResponseEntity.ok(ApiResponse.success("FCM 토큰이 삭제되었습니다."));
    }

    /**
     * 알림 유형 목록 조회
     */
    @Operation(summary = "알림 유형 목록 조회", description = "사용 가능한 알림 유형 목록을 조회합니다.")
    @GetMapping("/types")
    public ResponseEntity<ApiResponse<AlertTypeListResponse>> getAlertTypes(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AlertTypeListResponse response = alertService.getAlertTypes();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 알림 설정 조회
     */
    @Operation(summary = "알림 설정 조회", description = "사용자의 알림 설정을 조회합니다.")
    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<NotificationSettingsResponse>> getNotificationSettings(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        NotificationSettingsResponse response = alertService.getNotificationSettings(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 알림 설정 수정
     */
    @Operation(summary = "알림 설정 수정", description = "알림 설정을 수정합니다.")
    @PutMapping("/settings")
    public ResponseEntity<ApiResponse<UpdateCountResponse>> updateNotificationSettings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody NotificationSettingsRequest request
    ) {
        int count = alertService.updateNotificationSettings(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(UpdateCountResponse.updated(count)));
    }
}
