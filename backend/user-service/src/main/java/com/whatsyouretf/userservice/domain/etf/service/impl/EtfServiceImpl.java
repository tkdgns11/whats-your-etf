package com.whatsyouretf.userservice.domain.etf.service.impl;

import com.whatsyouretf.userservice.domain.etf.dto.*;
import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.etf.entity.EtfPrice;
import com.whatsyouretf.userservice.domain.etf.entity.EtfSectorAiHistory;
import com.whatsyouretf.userservice.domain.etf.entity.EtfSectorCluster;
import com.whatsyouretf.userservice.domain.etf.repository.EtfSectorAiHistoryRepository;
import com.whatsyouretf.userservice.domain.etf.repository.EtfSectorClusterRepository;
import com.whatsyouretf.userservice.domain.etf.repository.EtfStockCompositionRepository;
import com.whatsyouretf.userservice.domain.etf.service.EtfPriceReader;
import com.whatsyouretf.userservice.domain.etf.service.EtfQuery;
import com.whatsyouretf.userservice.domain.etf.service.EtfReader;
import com.whatsyouretf.userservice.domain.etf.service.EtfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ETF 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EtfServiceImpl implements EtfService {

    private final EtfReader etfReader;
    private final EtfPriceReader etfPriceReader;
    private final EtfSectorClusterRepository sectorClusterRepository;
    private final EtfSectorAiHistoryRepository sectorAiHistoryRepository;
    private final EtfStockCompositionRepository stockCompositionRepository;
    private static final int MAX_INFLUENTIAL_STOCKS = 5;
    private static final int MAX_SECTOR_STOCKS = 5;



    /**
     * 섹터 클러스터 조회 (버블 차트용)
     */
    private List<EtfSectorResponse> getSectorClusters(Long etfId) {
        // 섹터 클러스터 조회
        List<EtfSectorCluster> clusters = sectorClusterRepository.findLatestByEtfId(etfId);

        if (clusters.isEmpty()) {
            return List.of();
        }

        // AI 분석 조회 (그룹코드별)
        Map<String, String> aiAnalysisMap = sectorAiHistoryRepository.findLatestAllByEtfId(etfId).stream()
                .collect(Collectors.toMap(
                        EtfSectorAiHistory::getGroupCode,
                        EtfSectorAiHistory::getAiAnalysis,
                        (a, b) -> a // 중복 시 첫번째 사용
                ));

        // 섹터별 종목 조회하여 응답 생성
        return clusters.stream()
                .map(cluster -> {
                    // 해당 섹터 종목들 조회
                    List<EtfSectorStockResponse> stocks = getSectorStocks(etfId, cluster.getGroupCode());

                    return EtfSectorResponse.builder()
                            .name(cluster.getGroupName() != null ? cluster.getGroupName() : cluster.getIndustryName())
                            .percentage(cluster.getWeightPct())
                            .stocks(stocks)
                            .aiAnalysis(aiAnalysisMap.get(cluster.getGroupCode()))
                            .build();
                })
                .toList();
    }

    /**
     * 섹터별 종목 목록 조회
     */
    private List<EtfSectorStockResponse> getSectorStocks(Long etfId, String groupCode) {
        if (groupCode == null) {
            return List.of();
        }

        return stockCompositionRepository.findByEtfIdAndGroupCode(etfId, groupCode).stream()
                .limit(MAX_SECTOR_STOCKS)
                .map(comp -> EtfSectorStockResponse.builder()
                        .ticker(comp.getStock().getTicker())
                        .name(comp.getStock().getCompany() != null
                                ? comp.getStock().getCompany().getCompanyName()
                                : null)
                        .percentage(comp.getWeightPct())
                        .build())
                .toList();
    }

    /**
     * 영향력 종목 조회 (비중 상위 N개)
     */
    private List<EtfInfluentialStockResponse> getInfluentialStocks(Long etfId) {
        return stockCompositionRepository.findTopByEtfId(etfId, MAX_INFLUENTIAL_STOCKS).stream()
                .map(comp -> {
                    var stock = comp.getStock();
                    var company = stock.getCompany();

                    return EtfInfluentialStockResponse.builder()
                            .ticker(stock.getTicker())
                            .name(company != null ? company.getCompanyName() : null)
                            .weight(comp.getWeightPct())
                            .currentPrice(stock.getClose() != null ? stock.getClose().longValue() : null)
                            .changeRate(BigDecimal.ZERO) // 종목 등락률은 별도 조회 필요
                            .build();
                })
                .toList();
    }

    @Override
    public Page<EtfPrice> getEtfHistory(String ticker, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return etfPriceReader.readPrices(ticker, startDate, endDate, pageable);
    }

    @Override
    public Etf getEtfDetail(String ticker) {
        return etfReader.read(ticker);
    }

    @Override
    public EtfCurrentInfo getEtfCurrentInfo(String ticker) {
        return etfReader.getInfo(ticker);
    }

    @Override
    public Page<EtfSummary> getEtfList(EtfQuery query, Pageable pageable) {
        return etfReader.readEtfList(query, pageable);
    }
}
