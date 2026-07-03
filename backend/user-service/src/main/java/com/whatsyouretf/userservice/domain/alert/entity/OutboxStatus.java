package com.whatsyouretf.userservice.domain.alert.entity;

/**
 * 알림 아웃박스 발송 상태.
 */
public enum OutboxStatus {
    /** 발송 대기 — 알림 저장 트랜잭션과 함께 적재됨 */
    PENDING,
    /** 발송 성공 */
    SENT,
    /** 최대 재시도 초과로 격리 — 운영 확인 대상 */
    FAILED
}
