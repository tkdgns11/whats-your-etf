package com.whatsyouretf.userservice.domain.etf.service.impl;

import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.domain.etf.dto.*;
import com.whatsyouretf.userservice.domain.etf.entity.*;
import com.whatsyouretf.userservice.domain.etf.repository.*;
import com.whatsyouretf.userservice.domain.etf.service.EtfService;
import com.whatsyouretf.userservice.domain.user.entity.UserFavoriteEtf;
import com.whatsyouretf.userservice.domain.user.repository.UserFavoriteEtfRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ETF 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EtfServiceImpl implements EtfService {

    private final EtfRepository etfRepository;
    private final EtfPriceRepository etfPriceRepository;
    private final EtfStockCompositionRepository etfStockCompositionRepository;
    private final EtfSectorClusterRepository etfSectorClusterRepository;
    private final UserFavoriteEtfRepository userFavoriteEtfRepository;

    @Override
    public EtfListResponse getEtfList(int page, int size, String strategyType,
                                       String sector, String riskGrade, String sortBy) {
        size = Math.min(size, 100);
        Sort sort = getSort(sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Etf> etfPage;
        if (strategyType != null && !strategyType.isEmpty()) {
            etfPage = etfRepository.findByStrategyTypeAndIsActiveTrue(strategyType, pageable);
        } else {
            etfPage = etfRepository.findByIsActiveTrue(pageable);
        }

        // 최신 가격 정보 조회
        List<Long> etfIds = etfPage.getContent().stream().map(Etf::getId).toList();
        Map<Long, EtfPrice> priceMap = etfPriceRepository.findLatestByEtfIds(etfIds).stream()
                .collect(Collectors.toMap(p -> p.getEtf().getId(), p -> p));

        List<EtfListResponse.EtfItem> etfItems = etfPage.getContent().stream()
                .map(etf -> {
                    EtfPrice price = priceMap.get(etf.getId());
                    return EtfListResponse.EtfItem.from(etf,
                            price != null ? price.getClose() : null,
                            price != null ? price.getChangeRate() : null);
                })
                .toList();

        return EtfListResponse.builder()
                .etfs(etfItems)
                .page(page)
                .totalPages(etfPage.getTotalPages())
                .totalElements(etfPage.getTotalElements())
                .build();
    }

    @Override
    public EtfDetailResponse getEtfDetail(Long etfId) {
        Etf etf = etfRepository.findById(etfId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ETF_NOT_FOUND));

        EtfPrice latestPrice = etfPriceRepository.findLatestByEtfId(etfId).orElse(null);

        return EtfDetailResponse.from(etf,
                latestPrice != null ? latestPrice.getClose() : null,
                latestPrice != null ? latestPrice.getChangeRate() : null);
    }

    @Override
    public EtfPriceHistoryResponse getEtfPriceHistory(Long etfId, LocalDate startDate, LocalDate endDate) {
        Etf etf = etfRepository.findById(etfId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ETF_NOT_FOUND));

        // 기본값: 최근 1개월
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        List<EtfPrice> prices = etfPriceRepository.findByEtfIdAndDateRange(etfId, startDate, endDate);

        List<EtfPriceHistoryResponse.PriceItem> priceItems = prices.stream()
                .map(EtfPriceHistoryResponse.PriceItem::from)
                .toList();

        return EtfPriceHistoryResponse.builder()
                .etfId(etf.getId())
                .ticker(etf.getStockCode())
                .name(etf.getName())
                .prices(priceItems)
                .build();
    }

    @Override
    public EtfCompositionResponse getEtfCompositions(Long etfId, LocalDate baseDate) {
        Etf etf = etfRepository.findById(etfId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ETF_NOT_FOUND));

        List<EtfStockComposition> compositions;
        if (baseDate != null) {
            compositions = etfStockCompositionRepository.findByEtfIdAndBaseDate(etfId, baseDate);
        } else {
            compositions = etfStockCompositionRepository.findLatestByEtfId(etfId);
        }

        if (compositions.isEmpty()) {
            throw new BusinessException(ErrorCode.ETF_COMPOSITION_NOT_FOUND);
        }

        LocalDate actualBaseDate = compositions.get(0).getBaseDate();

        List<EtfCompositionResponse.CompositionItem> items = compositions.stream()
                .map(EtfCompositionResponse.CompositionItem::from)
                .toList();

        return EtfCompositionResponse.builder()
                .etfId(etf.getId())
                .ticker(etf.getStockCode())
                .name(etf.getName())
                .baseDate(actualBaseDate)
                .compositions(items)
                .build();
    }

    @Override
    public EtfSearchResponse searchEtf(String keyword, String strategyType, String issuer) {
        if (keyword == null || keyword.trim().length() < 2) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<Etf> etfs = etfRepository.searchByKeyword(keyword.trim());

        // 추가 필터링
        if (strategyType != null && !strategyType.isEmpty()) {
            etfs = etfs.stream()
                    .filter(e -> strategyType.equals(e.getStrategyType()))
                    .toList();
        }
        if (issuer != null && !issuer.isEmpty()) {
            etfs = etfs.stream()
                    .filter(e -> issuer.equals(e.getAssetManager()))
                    .toList();
        }

        List<EtfSearchResponse.EtfSearchItem> items = etfs.stream()
                .map(EtfSearchResponse.EtfSearchItem::from)
                .toList();

        return EtfSearchResponse.builder()
                .etfs(items)
                .totalCount(items.size())
                .build();
    }

    @Override
    public EtfSectorClusterResponse getEtfSectorCluster(Long etfId) {
        Etf etf = etfRepository.findById(etfId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ETF_NOT_FOUND));

        List<EtfSectorCluster> clusters = etfSectorClusterRepository.findLatestByEtfId(etfId);

        if (clusters.isEmpty()) {
            // 섹터 클러스터 데이터가 없으면 빈 응답
            return EtfSectorClusterResponse.builder()
                    .etfId(etf.getId())
                    .ticker(etf.getStockCode())
                    .name(etf.getName())
                    .sectorCluster(List.of())
                    .totalSectors(0)
                    .build();
        }

        LocalDate baseDate = clusters.get(0).getBaseDate();

        List<EtfSectorClusterResponse.SectorItem> sectorItems = clusters.stream()
                .map(c -> EtfSectorClusterResponse.SectorItem.from(c, List.of())) // topStocks는 별도 쿼리 필요
                .toList();

        return EtfSectorClusterResponse.builder()
                .etfId(etf.getId())
                .ticker(etf.getStockCode())
                .name(etf.getName())
                .baseDate(baseDate)
                .sectorCluster(sectorItems)
                .totalSectors(sectorItems.size())
                .build();
    }

    @Override
    public EtfRecommendResponse getEtfRecommendations(Long userId) {
        // 사용자의 관심 ETF 조회
        List<UserFavoriteEtf> favorites = userFavoriteEtfRepository.findByUserId(userId);

        List<EtfRecommendResponse.RecommendItem> recommendations = new ArrayList<>();

        for (UserFavoriteEtf favorite : favorites) {
            Etf favoriteEtf = favorite.getEtf();
            if (favoriteEtf.getSector() != null) {
                // 같은 섹터의 유사 ETF 추천
                List<Etf> similarEtfs = etfRepository.findSimilarBySector(
                        favoriteEtf.getSector(), favoriteEtf.getId(), PageRequest.of(0, 2));

                for (Etf similarEtf : similarEtfs) {
                    recommendations.add(EtfRecommendResponse.RecommendItem.builder()
                            .etf(EtfRecommendResponse.EtfInfo.builder()
                                    .id(similarEtf.getId())
                                    .ticker(similarEtf.getStockCode())
                                    .name(similarEtf.getName())
                                    .issuer(similarEtf.getAssetManager())
                                    .strategyType(similarEtf.getStrategyType())
                                    .build())
                            .reason("관심 ETF '" + favoriteEtf.getName() + "'과 유사한 " +
                                    favoriteEtf.getSector() + " 섹터 ETF입니다.")
                            .similarity(BigDecimal.valueOf(0.85))
                            .build());
                }
            }
        }

        // 중복 제거
        List<EtfRecommendResponse.RecommendItem> uniqueRecommendations = recommendations.stream()
                .collect(Collectors.toMap(
                        r -> r.getEtf().getId(),
                        r -> r,
                        (r1, r2) -> r1
                ))
                .values()
                .stream()
                .limit(10)
                .toList();

        return EtfRecommendResponse.builder()
                .recommendations(uniqueRecommendations)
                .build();
    }

    private Sort getSort(String sortBy) {
        if (sortBy == null) {
            return Sort.by(Sort.Direction.DESC, "aum");
        }
        return switch (sortBy.toLowerCase()) {
            case "expenseratio" -> Sort.by(Sort.Direction.ASC, "expenseRatio");
            case "name" -> Sort.by(Sort.Direction.ASC, "name");
            case "changerate" -> Sort.by(Sort.Direction.DESC, "volatility1y"); // 대용
            default -> Sort.by(Sort.Direction.DESC, "aum");
        };
    }
}
