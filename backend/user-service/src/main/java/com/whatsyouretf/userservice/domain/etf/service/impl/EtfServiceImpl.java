package com.whatsyouretf.userservice.domain.etf.service.impl;

import com.whatsyouretf.userservice.domain.etf.dto.*;
import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.etf.entity.EtfPrice;
import com.whatsyouretf.userservice.domain.etf.entity.EtfSectorAiHistory;
import com.whatsyouretf.userservice.domain.etf.entity.EtfSectorCluster;
import com.whatsyouretf.userservice.domain.etf.repository.EtfSectorAiHistoryRepository;
import com.whatsyouretf.userservice.domain.etf.repository.EtfSectorClusterRepository;
import com.whatsyouretf.userservice.domain.etf.repository.EtfStockClusterMappingRepository;
import com.whatsyouretf.userservice.domain.etf.repository.EtfStockCompositionRepository;
import com.whatsyouretf.userservice.domain.etf.service.EtfPriceReader;
import com.whatsyouretf.userservice.domain.etf.service.EtfQuery;
import com.whatsyouretf.userservice.domain.etf.service.EtfReader;
import com.whatsyouretf.userservice.domain.etf.service.EtfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final EtfStockClusterMappingRepository clusterMappingRepository;

    private static final int MAX_INFLUENTIAL_STOCKS = 5;
    private static final int MAX_SECTOR_STOCKS = 5;

    // 클러스터 타입 상수
    private static final String CLUSTER_TYPE_GROUP = "GROUP_CODE";
    private static final String CLUSTER_TYPE_SUB_SECTOR = "SUB_SECTOR";

    // 테마형 ETF 전략 타입
    private static final String STRATEGY_THEME = "테마형";



    /**
     * ETF 클러스터 데이터 조회 (영문명 + 섹터 + 영향력 종목)
     */
    @Override
    public EtfClusterResponse getClusterData(String ticker) {
        // ETF 조회 (영문명)
        Etf etf = etfReader.read(ticker);

        // 테마형 여부 판단
        boolean isThemeEtf = isThemeEtf(etf);
        String clusterType = isThemeEtf ? CLUSTER_TYPE_SUB_SECTOR : CLUSTER_TYPE_GROUP;

        // 섹터 클러스터 조회
        List<EtfSectorResponse> sectors = getSectorClusters(ticker, clusterType, isThemeEtf);

        // 영향력 종목 조회
        List<EtfInfluentialStockResponse> influentialStocks = getInfluentialStocks(etf.getId());

        return EtfClusterResponse.builder()
                .englishName(etf.getEnglishName())
                .sectors(sectors)
                .influentialStocks(influentialStocks)
                .build();
    }

    /**
     * 테마형 ETF 여부 판단
     */
    private boolean isThemeEtf(Etf etf) {
        return STRATEGY_THEME.equalsIgnoreCase(etf.getStrategyType());
    }

    @Override
    public Map<String, Etf> getEtfListInTickers(List<String> list) {
        return etfReader.getValidEtfs(list);
    }

    @Override
    public Map<String, EtfCurrentInfo> getEtfCurrentInfoMap(Set<String> tickers) {
        return etfReader.getInfosMap(tickers);
    }

    /**
     * 섹터 클러스터 조회 (버블 차트용)
     *
     * @param ticker ETF 티커
     * @param clusterType 클러스터 타입 (GROUP_CODE / SUB_SECTOR)
     * @param isThemeEtf 테마형 ETF 여부
     */
    private List<EtfSectorResponse> getSectorClusters(String ticker, String clusterType, boolean isThemeEtf) {
        // 섹터 클러스터 조회
        List<EtfSectorCluster> clusters = sectorClusterRepository.findLatestByEtfTickerAndClusterType(ticker, clusterType);

        if (clusters.isEmpty()) {
            return List.of();
        }

        // AI 분석 조회 (그룹코드별)
        Map<String, String> aiAnalysisMap = sectorAiHistoryRepository.findLatestAllByEtfTicker(ticker).stream()
            .collect(Collectors.toMap(
                EtfSectorAiHistory::getGroupCode,
                EtfSectorAiHistory::getAiAnalysis,
                (a, b) -> a // 중복 시 첫번째 사용
            ));

        // 섹터별 종목 조회하여 응답 생성
        return clusters.stream()
            .map(cluster -> {
                // 해당 섹터 종목들 조회 (테마형: sector_code로, 시장형: group_code로)
                List<EtfSectorStockResponse> stocks = isThemeEtf
                    ? getSectorStocksBySectorCode(ticker, cluster.getIndustryCode())
                    : getSectorStocksByGroupCode(ticker, cluster.getGroupCode());

                // 섹터명 결정: 테마형은 subSector, 시장형은 groupName
                String sectorName = isThemeEtf
                    ? (cluster.getSubSector() != null ? cluster.getSubSector() : cluster.getIndustryName())
                    : (cluster.getGroupName() != null ? cluster.getGroupName() : cluster.getIndustryName());

                return EtfSectorResponse.builder()
                    .name(sectorName)
                    .percentage(cluster.getWeightPct())
                    .stocks(stocks)
                    .aiAnalysis(aiAnalysisMap.get(cluster.getGroupCode()))
                    .build();
            })
            .toList();
    }

    /**
     * 섹터별 종목 목록 조회 - 세분류(sector_code)로 조회 (테마형 ETF용)
     * etf_stock_cluster_mapping 테이블 활용
     */
    private List<EtfSectorStockResponse> getSectorStocksBySectorCode(String ticker, String sectorCode) {
        if (sectorCode == null) {
            return List.of();
        }

        return clusterMappingRepository.findByEtfTickerAndSectorCode(ticker, sectorCode).stream()
                .limit(MAX_SECTOR_STOCKS)
                .map(mapping -> {
                    var comp = mapping.getComposition();
                    var stock = comp.getStock();
                    var company = stock.getCompany();

                    return EtfSectorStockResponse.builder()
                            .ticker(stock.getTicker())
                            .name(company != null ? company.getCompanyName() : null)
                            .percentage(comp.getWeightPct())
                            .build();
                })
                .toList();
    }

    /**
     * 섹터별 종목 목록 조회 - 그룹코드로 조회 (시장형 ETF용)
     * etf_stock_cluster_mapping 테이블 + industry_classification 조인
     */
    private List<EtfSectorStockResponse> getSectorStocksByGroupCode(String ticker, String groupCode) {
        if (groupCode == null) {
            return List.of();
        }

        return clusterMappingRepository.findByEtfTickerAndGroupCode(ticker, groupCode).stream()
                .limit(MAX_SECTOR_STOCKS)
                .map(mapping -> {
                    var comp = mapping.getComposition();
                    var stock = comp.getStock();
                    var company = stock.getCompany();

                    return EtfSectorStockResponse.builder()
                            .ticker(stock.getTicker())
                            .name(company != null ? company.getCompanyName() : null)
                            .percentage(comp.getWeightPct())
                            .build();
                })
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
