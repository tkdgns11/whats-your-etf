package com.whatsyouretf.userservice.domain.etf.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RiskType {
    CONSERVATIVE(1, "안정형"),
    STABLE(2, "안정추구형"),
    MODERATE(3, "위험중립형"),
    ACTIVE(4,"적극투자형"),
    AGGRESSIVE(5, "공격투자형");

    private final Integer riskGrade;
    private final String typeName;
}
