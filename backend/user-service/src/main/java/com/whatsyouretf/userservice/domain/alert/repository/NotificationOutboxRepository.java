package com.whatsyouretf.userservice.domain.alert.repository;

import com.whatsyouretf.userservice.domain.alert.entity.NotificationOutbox;
import com.whatsyouretf.userservice.domain.alert.entity.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

    /** 발송 대기(PENDING) 건을 오래된 순으로 배치 조회 (한 릴레이 주기에 처리할 상한) */
    List<NotificationOutbox> findTop200ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
