package com.whatsyouretf.userservice.domain.alert.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 알림 발송 아웃박스 (Transactional Outbox 패턴).
 * <p>
 * 알림 저장과 <b>같은 트랜잭션</b>에서 발송 요청을 {@link OutboxStatus#PENDING} 상태로 적재하고,
 * 별도 릴레이({@link com.whatsyouretf.userservice.domain.alert.service.NotificationOutboxRelay})가
 * 커밋 이후 폴링하여 FCM으로 발송한다. 발송 실패 시 재시도하며 최대치 초과 시 격리한다.
 * <p>
 * 이로써 {@code @Async} 직발송의 "커밋 전 발송 가능성"과 "실패 시 유실" 문제를 제거한다.
 */
@Entity
@Table(name = "notification_outbox", indexes = {
        @Index(name = "idx_outbox_status_created", columnList = "status, created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NotificationOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 수신 사용자 */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 알림 제목 */
    @Column(nullable = false, length = 200)
    private String title;

    /** 알림 본문 */
    @Column(columnDefinition = "TEXT")
    private String body;

    /** FCM data 페이로드 (JSON 직렬화) */
    @Column(name = "data_json", columnDefinition = "TEXT")
    private String dataJson;

    /** 발송 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OutboxStatus status = OutboxStatus.PENDING;

    /** 재시도 횟수 */
    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private int retryCount = 0;

    /** 마지막 실패 원인 */
    @Column(name = "last_error", length = 500)
    private String lastError;

    /** 적재 시각 (알림 저장 트랜잭션과 동일) */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /** 발송 완료 시각 */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /** 발송 성공 처리 */
    public void markSent() {
        this.status = OutboxStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    /**
     * 발송 실패 처리 — 재시도 카운트를 올리고, 최대치({@code maxRetry}) 초과 시 FAILED로 격리한다.
     * 그 전까지는 PENDING을 유지해 다음 릴레이에서 재시도된다.
     */
    public void markFailed(String error, int maxRetry) {
        this.retryCount++;
        this.lastError = (error != null && error.length() > 500) ? error.substring(0, 500) : error;
        if (this.retryCount >= maxRetry) {
            this.status = OutboxStatus.FAILED;
        }
    }
}
