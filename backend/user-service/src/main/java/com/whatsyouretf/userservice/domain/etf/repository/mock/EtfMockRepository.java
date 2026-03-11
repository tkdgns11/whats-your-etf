package com.whatsyouretf.userservice.domain.etf.repository.mock;

import com.whatsyouretf.userservice.domain.etf.dto.RiskType;
import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class EtfMockRepository {
    private final Map<String, Etf> db = Map.of(
        "069500", new Etf(1L,
                    "069500",
                    "KODEX 200",
                    "시장 대표",
                    null,
                    "래롬",
                    Boolean.FALSE,
                    Boolean.FALSE,
                    Boolean.FALSE,
                    BigDecimal.valueOf(2.5),
                    BigDecimal.valueOf(1241251255L),
                    12045210521050L,
                    BigDecimal.valueOf(2.5),
                    "3개월",
                    RiskType.STABLE,
                    LocalDate.of(2024,12,21),
                    null,
                    Boolean.TRUE,
                    LocalDateTime.now(),
                    LocalDateTime.now()),
        "102780", new Etf(2L,
                    "102780",
                    "KODEX 삼성그룹",
                    "시장 대표",
                    null,
                    "래롬",
                    Boolean.FALSE,
                    Boolean.FALSE,
                    Boolean.FALSE,
                    BigDecimal.valueOf(2.5),
                    BigDecimal.valueOf(1241251255L),
                    12045210521050L,
                    BigDecimal.valueOf(2.5),
                    "3개월",
                    RiskType.STABLE,
                    LocalDate.of(2024,12,21),
                    null,
                    Boolean.TRUE,
                    LocalDateTime.now(),
                    LocalDateTime.now()),
        "091160", new Etf(3L,
                    "102780",
                    "KODEX 반도체",
                    "테마형",
                    "반도체",
                    "래롬",
                    Boolean.FALSE,
                    Boolean.FALSE,
                    Boolean.FALSE,
                    BigDecimal.valueOf(2.5),
                    BigDecimal.valueOf(1241251255L),
                    12045210521050L,
                    BigDecimal.valueOf(2.5),
                    "3개월",
                    RiskType.STABLE,
                    LocalDate.of(2024,12,21),
                    null,
                    Boolean.TRUE,
                    LocalDateTime.now(),
                    LocalDateTime.now()));

    public Etf findByTicker(String ticker) {
        return db.get(ticker);
    }
}

