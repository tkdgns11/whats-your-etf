package com.whatsyouretf.userservice.domain.etf.service.impl;

import com.whatsyouretf.userservice.domain.company.repository.StockInfo;
import com.whatsyouretf.userservice.domain.company.service.StockCache;
import com.whatsyouretf.userservice.domain.etf.dto.*;
import com.whatsyouretf.userservice.domain.etf.entity.*;
import com.whatsyouretf.userservice.domain.etf.repository.*;
import com.whatsyouretf.userservice.domain.etf.repository.SectorStockProjection;
import com.whatsyouretf.userservice.domain.etf.service.*;
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
    private final StockCache stockCache;
    private final EtfDividendRepository etfDividendRepository;
    private final EtfOtherCompositionRepository otherCompositionRepository;

    private static final int MAX_INFLUENTIAL_STOCKS = 5;
    private static final int MAX_SECTOR_STOCKS = 5;

    // 클러스터 타입 상수
    private static final String CLUSTER_TYPE_GROUP = "GROUP_CODE";
    private static final String CLUSTER_TYPE_SUB_SECTOR = "SUB_SECTOR";

    // 테마형 ETF 전략 타입
    private static final String STRATEGY_THEME = "테마형";
    private final EtfStockCompositionRepository etfStockCompositionRepository;


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

        // AI 분석 조회 (모든 그룹코드 - 주식 섹터 + 비주식 자산)
        Map<String, String> aiAnalysisMap = sectorAiHistoryRepository.findLatestAllByEtfTicker(ticker).stream()
            .collect(Collectors.toMap(
                EtfSectorAiHistory::getGroupCode,
                EtfSectorAiHistory::getAiAnalysis,
                (a, b) -> a // 중복 시 첫번째 사용
            ));

        // 섹터 클러스터 조회 (주식 섹터)
        List<EtfSectorResponse> stockSectors = getSectorClusters(ticker, clusterType, isThemeEtf, aiAnalysisMap);

        // 비주식 구성종목을 섹터로 변환 (선물, 채권 등)
        List<EtfSectorResponse> otherSectors = getOtherCompositionsAsSectors(etf.getId(), aiAnalysisMap);

        // 주식 섹터 + 비주식 섹터 합치기
        List<EtfSectorResponse> allSectors = new java.util.ArrayList<>(stockSectors);
        allSectors.addAll(otherSectors);

        // 영향력 종목 조회
        List<EtfInfluentialStockResponse> influentialStocks = getInfluentialStocks(etf.getId());

        return EtfClusterResponse.builder()
                .englishName(etf.getEnglishName())
                .sectors(allSectors)
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

    @Override
    public List<EtfStockComposition> getEtfsIncludingStock(String ticker) {
        return etfStockCompositionRepository.getEtfStockCompositionByStockTicker(ticker);
    }

    @Override
    public List<EtfCurrentInfo> getTopTenList() {
        return etfReader.getTopTenEtfs();
    }

    @Override
    public List<EtfDividendsData> getEtfDividends(String ticker) {
        return etfDividendRepository.getDividends(ticker);
    }

    /**
     * 섹터 클러스터 조회 (버블 차트용)
     *
     * @param ticker ETF 티커
     * @param clusterType 클러스터 타입 (GROUP_CODE / SUB_SECTOR)
     * @param isThemeEtf 테마형 ETF 여부
     * @param aiAnalysisMap AI 분석 맵 (그룹코드 -> 분석 텍스트)
     */
    private List<EtfSectorResponse> getSectorClusters(String ticker, String clusterType, boolean isThemeEtf, Map<String, String> aiAnalysisMap) {
        // 섹터 클러스터 조회
        List<EtfSectorCluster> clusters = sectorClusterRepository.findLatestByEtfTickerAndClusterType(ticker, clusterType);

        if (clusters.isEmpty()) {
            return List.of();
        }

        Map<String, List<EtfSectorStockResponse>> stocksByCode = isThemeEtf
            ? loadAllStocksBySectorCode(ticker)
            : loadAllStocksByGroupCode(ticker);

        // 섹터별 응답 생성
        return clusters.stream()
            .map(cluster -> {
                String code = isThemeEtf ? cluster.getIndustryCode() : cluster.getGroupCode();
                List<EtfSectorStockResponse> stocks = stocksByCode.getOrDefault(code, List.of());

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
     * 테마형 ETF: 섹터코드별 상위 5개 종목 조회
     */
    private Map<String, List<EtfSectorStockResponse>> loadAllStocksBySectorCode(String ticker) {
        return clusterMappingRepository.findTopStocksBySectorCode(ticker, MAX_SECTOR_STOCKS).stream()
            .collect(Collectors.groupingBy(
                SectorStockProjection::getSectorCode,
                Collectors.mapping(this::toSectorStockResponse, Collectors.toList())
            ));
    }

    /**
     * 시장형 ETF: 그룹코드별 상위 5개 종목 조회
     */
    private Map<String, List<EtfSectorStockResponse>> loadAllStocksByGroupCode(String ticker) {
        return clusterMappingRepository.findTopStocksByGroupCode(ticker, MAX_SECTOR_STOCKS).stream()
            .collect(Collectors.groupingBy(
                SectorStockProjection::getGroupCode,
                Collectors.mapping(this::toSectorStockResponse, Collectors.toList())
            ));
    }

    /**
     * SectorStockProjection -> EtfSectorStockResponse 변환
     */
    private EtfSectorStockResponse toSectorStockResponse(SectorStockProjection projection) {
        return EtfSectorStockResponse.builder()
            .ticker(projection.getStockTicker())
            .name(projection.getCompanyName())
            .percentage(projection.getWeightPct())
            .build();
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
     * 영향력 종목 조회 (비중 × 현재가 × |등락률| 기준 상위 N개)
     */
    private List<EtfInfluentialStockResponse> getInfluentialStocks(Long etfId) {
        return stockCompositionRepository.findLatestByEtfId(etfId).stream()
                .map(comp -> {
                    var stock = comp.getStock();
                    var company = stock.getCompany();
                    StockInfo stockInfo = stockCache.get(stock.getTicker(), stock.getDescription());

                    BigDecimal changeRate = (stockInfo != null && stockInfo.dailyReturn() != null)
                            ? stockInfo.dailyReturn() : BigDecimal.ZERO;
                    BigDecimal currentPrice = (stockInfo != null && stockInfo.currentPrice() != null)
                            ? stockInfo.currentPrice() : BigDecimal.ZERO;

                    // 영향력 = 비중 × 현재가 × |등락률|
                    BigDecimal influence = comp.getWeightPct()
                            .multiply(currentPrice)
                            .multiply(changeRate.abs());

                    return new InfluentialStockTemp(
                            stock.getTicker(),
                            company != null ? company.getCompanyName() : null,
                            comp.getWeightPct(),
                            currentPrice.longValue() > 0 ? currentPrice.longValue() : null,
                            changeRate,
                            influence
                    );
                })
                .sorted((a, b) -> b.influence().compareTo(a.influence()))
                .limit(MAX_INFLUENTIAL_STOCKS)
                .map(temp -> EtfInfluentialStockResponse.builder()
                        .ticker(temp.ticker())
                        .name(temp.name())
                        .weight(temp.weight())
                        .currentPrice(temp.currentPrice())
                        .changeRate(temp.changeRate())
                        .build())
                .toList();
    }

    private record InfluentialStockTemp(
            String ticker,
            String name,
            BigDecimal weight,
            Long currentPrice,
            BigDecimal changeRate,
            BigDecimal influence
    ) {}

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

    /**
     * 비주식 구성종목을 asset_type별로 묶어서 섹터 형태로 변환
     * 예: FUTURES 3개 → "선물" 1개 섹터 (비중 합산, 개별 항목은 stocks에 포함)
     *
     * @param etfId ETF ID
     * @param aiAnalysisMap AI 분석 맵 (asset_type -> 분석 텍스트)
     */
    private List<EtfSectorResponse> getOtherCompositionsAsSectors(Long etfId, Map<String, String> aiAnalysisMap) {
        // asset_type별 한글명 매핑
        Map<String, String> assetTypeNames = Map.of(
                "FUTURES", "선물",
                "ETF", "ETF",
                "BOND", "채권",
                "CASH", "현금",
                "PREFERRED_STOCK", "우선주"
        );

        // asset_type별로 그룹핑
        Map<String, List<EtfOtherComposition>> groupedByType = otherCompositionRepository.findByEtfId(etfId).stream()
                .collect(Collectors.groupingBy(EtfOtherComposition::getAssetType));

        return groupedByType.entrySet().stream()
                .map(entry -> {
                    String assetType = entry.getKey();
                    List<EtfOtherComposition> compositions = entry.getValue();

                    // 비중 합산
                    BigDecimal totalWeight = compositions.stream()
                            .map(EtfOtherComposition::getWeight)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    // 개별 항목을 stocks로 변환
                    List<EtfSectorStockResponse> stocks = compositions.stream()
                            .map(comp -> EtfSectorStockResponse.builder()
                                    .ticker(comp.getIdentifierValue())
                                    .name(comp.getAssetName())
                                    .percentage(comp.getWeight())
                                    .build())
                            .toList();

                    return EtfSectorResponse.builder()
                            .name(assetTypeNames.getOrDefault(assetType, assetType))
                            .percentage(totalWeight)
                            .stocks(stocks)
                            .aiAnalysis(aiAnalysisMap.get(assetType))
                            .assetType(assetType)
                            .build();
                })
                .toList();
    }
}
