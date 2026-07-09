package com.whatsyouretf.userservice.domain.alert.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatsyouretf.userservice.domain.alert.entity.NotificationOutbox;
import com.whatsyouretf.userservice.domain.alert.entity.OutboxStatus;
import com.whatsyouretf.userservice.domain.alert.repository.NotificationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 알림 아웃박스 릴레이.
 * <p>
 * 커밋 완료된 {@link OutboxStatus#PENDING} 아웃박스를 주기적으로 폴링하여 FCM으로 발송한다.
 * 성공 시 SENT, 실패 시 재시도 카운트를 올리고 최대치 초과 시 FAILED로 격리한다.
 * ({@code @Transactional} + JPA 더티 체킹으로 상태가 반영된다.)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationOutboxRelay {

    private final NotificationOutboxRepository outboxRepository;
    private final FcmService fcmService;
    private final ObjectMapper objectMapper;

    /** 최대 재시도 횟수 (초과 시 FAILED로 격리) */
    private static final int MAX_RETRY = 5;

    /**
     * 한 주기에 선점·처리할 최대 건수. 선점 행 잠금을 FCM 발송(외부 I/O) 동안 잡고 있으므로,
     * 잠금 보유 시간을 짧게 유지하기 위해 배치를 작게 둔다.
     */
    private static final int BATCH_SIZE = 50;

    /**
     * PENDING 아웃박스를 배치로 발송한다. 기본 5초 주기 (프로퍼티로 조정 가능).
     * <p>
     * {@code FOR UPDATE SKIP LOCKED}로 PENDING 건을 선점 조회하므로, 릴레이를 여러 인스턴스로
     * 확장해도 같은 레코드를 두 워커가 동시에 발송하지 않는다. 잠금은 이 트랜잭션이 끝날 때까지 유지된다.
     */
    @Scheduled(fixedDelayString = "${notification.outbox.relay-interval-ms:5000}")
    @Transactional
    public void relayPending() {
        List<NotificationOutbox> batch = outboxRepository.claimPendingBatch(BATCH_SIZE);
        if (batch.isEmpty()) {
            return;
        }

        int sent = 0, retry = 0, failed = 0;
        for (NotificationOutbox outbox : batch) {
            try {
                boolean ok = fcmService.sendToUser(
                        outbox.getUserId(),
                        outbox.getTitle(),
                        outbox.getBody(),
                        parseData(outbox.getDataJson())
                );
                if (ok) {
                    outbox.markSent();
                    sent++;
                } else {
                    outbox.markFailed("FCM sendToUser returned false", MAX_RETRY);
                    if (outbox.getStatus() == OutboxStatus.FAILED) failed++; else retry++;
                }
            } catch (Exception e) {
                outbox.markFailed(e.getMessage(), MAX_RETRY);
                if (outbox.getStatus() == OutboxStatus.FAILED) failed++; else retry++;
                log.warn("아웃박스 발송 실패: id={}, retry={}, error={}",
                        outbox.getId(), outbox.getRetryCount(), e.getMessage());
            }
        }
        log.info("아웃박스 릴레이 완료: 성공 {}건, 재시도 {}건, 격리 {}건", sent, retry, failed);
    }

    private Map<String, String> parseData(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
