package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.EtfPrice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * JPA 기반 ETF 가격 이력 저장소 구현
 * DB의 ETF_PRICE 테이블에서 가격 이력을 조회합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EtfPriceRepositoryImpl {
    private final EtfPriceJpaRepository etfPriceJpaRepository;

    /**
     * ETF 가격 이력 조회
     *
     * @param ticker    종목코드
     * @param startDate 시작일 (null이면 조건 없음)
     * @param endDate   종료일 (null이면 조건 없음)
     * @param pageable  페이징
     * @return ETF 가격 이력 페이지
     */
    public Page<EtfPrice> findAll(String ticker, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        try {
            // ticker로 ETF ID 조회 후 가격 이력 조회
            return etfPriceJpaRepository.findByEtfTickerAndDateRange(ticker, startDate, endDate, pageable);
        } catch (Exception e) {
            log.error("[{}] ETF 가격 이력 조회 실패 ({} ~ {}): {}", ticker, startDate, endDate, e.getMessage());
            return Page.empty(pageable);
        }
    }
}
