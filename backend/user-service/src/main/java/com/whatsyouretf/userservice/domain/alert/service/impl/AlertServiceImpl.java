package com.whatsyouretf.userservice.domain.alert.service.impl;

import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.domain.alert.dto.*;
import com.whatsyouretf.userservice.domain.alert.entity.*;
import com.whatsyouretf.userservice.domain.alert.repository.*;
import com.whatsyouretf.userservice.domain.alert.service.AlertService;
import com.whatsyouretf.userservice.domain.user.entity.User;
import com.whatsyouretf.userservice.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 알림 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertServiceImpl implements AlertService {

    private final UserAlertRepository userAlertRepository;
    private final AlertTypeRepository alertTypeRepository;
    private final UserNotificationSettingRepository notificationSettingRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;

    @Override
    public AlertListResponse getAlerts(Long userId, int page, int size, String category) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));

        Page<UserAlert> alertPage;
        if (category == null || category.equalsIgnoreCase("all")) {
            alertPage = userAlertRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        } else {
            try {
                AlertCategory alertCategory = AlertCategory.valueOf(category.toUpperCase());
                alertPage = userAlertRepository.findByUserIdAndCategory(userId, alertCategory, pageable);
            } catch (IllegalArgumentException e) {
                alertPage = userAlertRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
            }
        }

        List<AlertListResponse.AlertItem> alerts = alertPage.getContent().stream()
                .map(AlertListResponse.AlertItem::from)
                .toList();

        long unreadCount = userAlertRepository.countByUserIdAndIsReadFalse(userId);

        return AlertListResponse.builder()
                .alerts(alerts)
                .page(page)
                .totalPages(alertPage.getTotalPages())
                .totalElements(alertPage.getTotalElements())
                .unreadCount(unreadCount)
                .build();
    }

    @Override
    public UnreadCountResponse getUnreadCount(Long userId) {
        long count = userAlertRepository.countByUserIdAndIsReadFalse(userId);
        return UnreadCountResponse.builder().unreadCount(count).build();
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long alertId) {
        UserAlert alert = userAlertRepository.findByIdAndUserId(alertId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ALERT_NOT_FOUND));
        alert.markAsRead();
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        return userAlertRepository.markAllAsRead(userId);
    }

    @Override
    @Transactional
    public void deleteAlert(Long userId, Long alertId) {
        UserAlert alert = userAlertRepository.findByIdAndUserId(alertId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ALERT_NOT_FOUND));
        userAlertRepository.delete(alert);
    }

    @Override
    @Transactional
    public int deleteAllReadAlerts(Long userId) {
        return userAlertRepository.deleteAllReadByUserId(userId);
    }

    @Override
    @Transactional
    public void registerFcmToken(Long userId, FcmTokenRequest request) {
        if (request.getDeviceType() == null) {
            throw new BusinessException(ErrorCode.FCM_TOKEN_INVALID);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 기존 토큰 조회 (같은 기기 유형)
        FcmToken existingToken = fcmTokenRepository.findByUserIdAndDeviceType(userId, request.getDeviceType())
                .orElse(null);

        if (existingToken != null) {
            // 기존 토큰 업데이트
            existingToken.updateToken(request.getToken());
        } else {
            // 새 토큰 등록
            FcmToken newToken = FcmToken.builder()
                    .user(user)
                    .token(request.getToken())
                    .deviceType(request.getDeviceType())
                    .build();
            fcmTokenRepository.save(newToken);
        }
    }

    @Override
    @Transactional
    public void deleteFcmToken(Long userId, String token) {
        fcmTokenRepository.deleteByUserIdAndToken(userId, token);
    }

    @Override
    public AlertTypeListResponse getAlertTypes() {
        List<AlertType> alertTypes = alertTypeRepository.findByIsActiveTrueOrderByDisplayOrderAsc();

        List<AlertTypeListResponse.AlertTypeItem> items = alertTypes.stream()
                .map(AlertTypeListResponse.AlertTypeItem::from)
                .toList();

        return AlertTypeListResponse.builder()
                .alertTypes(items)
                .build();
    }

    @Override
    public NotificationSettingsResponse getNotificationSettings(Long userId) {
        List<UserNotificationSetting> settings = notificationSettingRepository.findByUserIdWithAlertType(userId);

        // 설정이 없으면 기본값으로 모든 알림 유형 반환
        if (settings.isEmpty()) {
            List<AlertType> alertTypes = alertTypeRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
            List<NotificationSettingsResponse.SettingItem> items = alertTypes.stream()
                    .map(at -> NotificationSettingsResponse.SettingItem.builder()
                            .alertTypeCode(at.getCode())
                            .alertTypeName(at.getName())
                            .category(at.getCategory())
                            .isEnabled(true) // 기본값: 활성화
                            .build())
                    .toList();

            return NotificationSettingsResponse.builder()
                    .settings(items)
                    .build();
        }

        List<NotificationSettingsResponse.SettingItem> items = settings.stream()
                .map(NotificationSettingsResponse.SettingItem::from)
                .toList();

        return NotificationSettingsResponse.builder()
                .settings(items)
                .build();
    }

    @Override
    @Transactional
    public int updateNotificationSettings(Long userId, NotificationSettingsRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        int updatedCount = 0;

        for (NotificationSettingsRequest.SettingItem item : request.getSettings()) {
            AlertType alertType = alertTypeRepository.findById(item.getAlertTypeCode())
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

            UserNotificationSetting setting = notificationSettingRepository
                    .findByUserIdAndAlertTypeCode(userId, item.getAlertTypeCode())
                    .orElse(null);

            if (setting != null) {
                setting.setEnabled(item.getIsEnabled());
            } else {
                UserNotificationSetting newSetting = UserNotificationSetting.builder()
                        .user(user)
                        .alertType(alertType)
                        .isEnabled(item.getIsEnabled())
                        .build();
                notificationSettingRepository.save(newSetting);
            }
            updatedCount++;
        }

        return updatedCount;
    }
}
