package com.whatsyouretf.userservice.domain.alert.dto;

import com.whatsyouretf.userservice.domain.alert.entity.AlertCategory;
import com.whatsyouretf.userservice.domain.alert.entity.UserNotificationSetting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 알림 설정 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSettingsResponse {

    /** 알림 설정 목록 */
    private List<SettingItem> settings;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SettingItem {
        /** 알림 유형 코드 */
        private String alertTypeCode;

        /** 알림 유형명 */
        private String alertTypeName;

        /** 카테고리 */
        private AlertCategory category;

        /** 활성화 여부 */
        private Boolean isEnabled;

        /**
         * Entity -> DTO 변환
         */
        public static SettingItem from(UserNotificationSetting setting) {
            return SettingItem.builder()
                    .alertTypeCode(setting.getAlertType().getCode())
                    .alertTypeName(setting.getAlertType().getName())
                    .category(setting.getAlertType().getCategory())
                    .isEnabled(setting.getIsEnabled())
                    .build();
        }
    }
}
