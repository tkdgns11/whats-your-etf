package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.dto.EtfSummary;
import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * JPA 기반 ETF 저장소 실제 구현
 * DB의 ETF 데이터를 조회합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EtfRepositoryImpl {
    private final EtfJpaRepository etfJpaRepository;

    public Etf findByTicker(String ticker) {
        try {
            return etfJpaRepository.findByStockCode(ticker)
                .orElse(null);
        } catch (Exception e) {
            log.error("[{}] DB 조회 실패: {}", ticker, e.getMessage());
            return null;
        }
    }

    public Page<EtfSummary> findEtfList(Pageable pageable) {
        try {
            Page<Etf> etfPage = etfJpaRepository.findAll(pageable);

            List<EtfSummary> summaries = etfPage.getContent().stream()
                .map(etf -> new EtfSummary(
                    etf.getId(),
                    etf.getStockCode(),
                    etf.getName(),
                    false,
                    etf.getRiskType() != null ? etf.getRiskType().getTypeName() : "MODERATE"
                ))
                .sorted(Comparator.comparing(EtfSummary::etfId))
                .toList();

            return new PageImpl<>(summaries, pageable, etfPage.getTotalElements());
        } catch (Exception e) {
            log.error("ETF 목록 조회 실패: {}", e.getMessage());
            return new PageImpl<>(List.of(), pageable, 0);
        }
    }
}
