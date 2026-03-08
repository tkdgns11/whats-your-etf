package com.whatsyouretf.userservice.domain.alert.service;

import com.whatsyouretf.userservice.domain.alert.dto.*;
import com.whatsyouretf.userservice.domain.alert.entity.AlertCategory;

/**
 * 알림 서비스 인터페이스
 */
public interface AlertService {

    /**
     * 알림 목록 조회
     */
    AlertListResponse getAlerts(Long userId, int page, int size, String category);

    /**
     * 읽지 않은 알림 수 조회
     */
    UnreadCountResponse getUnreadCount(Long userId);

    /**
     * 알림 읽음 처리
     */
    void markAsRead(Long userId, Long alertId);

    /**
     * 모든 알림 읽음 처리
     */
    int markAllAsRead(Long userId);

    /**
     * 알림 삭제
     */
    void deleteAlert(Long userId, Long alertId);

    /**
     * 읽은 알림 전체 삭제
     */
    int deleteAllReadAlerts(Long userId);

    /**
     * FCM 토큰 등록
     */
    void registerFcmToken(Long userId, FcmTokenRequest request);

    /**
     * FCM 토큰 삭제
     */
    void deleteFcmToken(Long userId, String token);

    /**
     * 알림 유형 목록 조회
     */
    AlertTypeListResponse getAlertTypes();

    /**
     * 알림 설정 조회
     */
    NotificationSettingsResponse getNotificationSettings(Long userId);

    /**
     * 알림 설정 수정
     */
    int updateNotificationSettings(Long userId, NotificationSettingsRequest request);
}
