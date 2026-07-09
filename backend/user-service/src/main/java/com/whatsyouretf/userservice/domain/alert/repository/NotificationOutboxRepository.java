package com.whatsyouretf.userservice.domain.alert.repository;

import com.whatsyouretf.userservice.domain.alert.entity.NotificationOutbox;
import com.whatsyouretf.userservice.domain.alert.entity.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

    /** 발송 대기(PENDING) 건을 오래된 순으로 배치 조회 (한 릴레이 주기에 처리할 상한) */
    List<NotificationOutbox> findTop200ByStatusOrderByCreatedAtAsc(OutboxStatus status);

    /**
     * 발송 대기(PENDING) 건을 오래된 순으로 <b>선점</b> 조회한다.
     * <p>
     * {@code FOR UPDATE SKIP LOCKED}로 다른 릴레이 워커가 이미 잠근 행은 건너뛴다.
     * 호출 트랜잭션이 끝날 때까지 행 잠금이 유지되므로, 릴레이를 <b>여러 인스턴스로
     * 확장</b>해도 같은 아웃박스 레코드를 두 워커가 동시에 처리(중복 발송)하지 않는다.
     * <p>
     * 반드시 릴레이의 {@code @Transactional} 안에서 호출할 것.
     * (단, FCM 발송 성공 후 SENT 커밋 전 장애 시 재시도로 인한 중복 가능성은 남는 at-least-once)
     */
    @Query(value = """
            SELECT *
            FROM notification_outbox
            WHERE status = 'PENDING'
            ORDER BY created_at ASC
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<NotificationOutbox> claimPendingBatch(@Param("limit") int limit);
}
