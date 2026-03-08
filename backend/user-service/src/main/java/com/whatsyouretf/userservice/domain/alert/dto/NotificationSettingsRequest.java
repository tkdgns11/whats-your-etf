package com.whatsyouretf.userservice.domain.alert.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 알림 설정 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSettingsRequest {

    /** 설정 변경할 알림 유형 목록 */
    @NotEmpty(message = "최소 1개의 설정이 필요합니다.")
    @Valid
    private List<SettingItem> settings;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SettingItem {
        /** 알림 유형 코드 */
        @NotNull(message = "알림 유형 코드는 필수입니다.")
        @Size(max = 30, message = "알림 유형 코드는 최대 30자입니다.")
        private String alertTypeCode;

        /** 활성화 여부 */
        @NotNull(message = "활성화 여부는 필수입니다.")
        private Boolean isEnabled;
    }
}
