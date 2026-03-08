package com.whatsyouretf.userservice.domain.portfolio.service.impl;

import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.etf.entity.EtfPrice;
import com.whatsyouretf.userservice.domain.etf.repository.EtfPriceRepository;
import com.whatsyouretf.userservice.domain.etf.repository.EtfRepository;
import com.whatsyouretf.userservice.domain.portfolio.dto.*;
import com.whatsyouretf.userservice.domain.portfolio.entity.Portfolio;
import com.whatsyouretf.userservice.domain.portfolio.entity.PortfolioEtf;
import com.whatsyouretf.userservice.domain.portfolio.repository.PortfolioEtfRepository;
import com.whatsyouretf.userservice.domain.portfolio.repository.PortfolioRepository;
import com.whatsyouretf.userservice.domain.portfolio.service.PortfolioService;
import com.whatsyouretf.userservice.domain.user.entity.User;
import com.whatsyouretf.userservice.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 포트폴리오 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioEtfRepository portfolioEtfRepository;
    private final UserRepository userRepository;
    private final EtfRepository etfRepository;
    private final EtfPriceRepository etfPriceRepository;

    private static final int MAX_PORTFOLIOS = 10;
    private static final int MAX_ETF_COUNT = 20;

    @Override
    public PortfolioListResponse getPortfolios(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        Page<Portfolio> portfolioPage = portfolioRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<PortfolioListResponse.PortfolioSummary> summaries = portfolioPage.getContent().stream()
                .map(p -> {
                    BigDecimal currentValue = calculateCurrentValue(p);
                    return PortfolioListResponse.PortfolioSummary.from(p, currentValue);
                })
                .toList();

        return PortfolioListResponse.builder()
                .portfolios(summaries)
                .page(page)
                .totalPages(portfolioPage.getTotalPages())
                .totalElements(portfolioPage.getTotalElements())
                .build();
    }

    @Override
    @Transactional
    public PortfolioCreateResponse createPortfolio(Long userId, PortfolioCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 포트폴리오 개수 제한 체크
        if (portfolioRepository.countByUserId(userId) >= MAX_PORTFOLIOS) {
            throw new BusinessException(ErrorCode.PORTFOLIO_ETF_LIMIT_EXCEEDED);
        }

        // 이름 중복 체크
        if (portfolioRepository.existsByUserIdAndName(userId, request.getName())) {
            throw new BusinessException(ErrorCode.DUPLICATE_PORTFOLIO_NAME);
        }

        // 비중 합계 검증
        validateWeightSum(request.getEtfs().stream()
                .map(PortfolioCreateRequest.EtfItem::getWeightPct)
                .toList());

        // 포트폴리오 생성
        Portfolio portfolio = Portfolio.builder()
                .user(user)
                .name(request.getName())
                .description(request.getDescription())
                .investAmount(request.getTotalInvestment())
                .build();

        portfolioRepository.save(portfolio);

        // ETF 구성 추가
        for (PortfolioCreateRequest.EtfItem etfItem : request.getEtfs()) {
            Etf etf = etfRepository.findById(etfItem.getEtfId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ETF_NOT_FOUND));

            PortfolioEtf portfolioEtf = PortfolioEtf.builder()
                    .portfolio(portfolio)
                    .etf(etf)
                    .weightPct(etfItem.getWeightPct())
                    .build();

            portfolioEtfRepository.save(portfolioEtf);
        }

        return PortfolioCreateResponse.builder()
                .id(portfolio.getId())
                .name(portfolio.getName())
                .build();
    }

    @Override
    public PortfolioDetailResponse getPortfolioDetail(Long userId, Long portfolioId) {
        Portfolio portfolio = getPortfolioWithAccess(userId, portfolioId);

        BigDecimal currentValue = calculateCurrentValue(portfolio);
        List<PortfolioDetailResponse.PortfolioEtfItem> etfItems = buildEtfItems(portfolio);

        return PortfolioDetailResponse.from(portfolio, currentValue, etfItems);
    }

    @Override
    @Transactional
    public void updatePortfolio(Long userId, Long portfolioId, PortfolioUpdateRequest request) {
        Portfolio portfolio = getPortfolioWithAccess(userId, portfolioId);

        // 이름 변경 시 중복 체크
        if (request.getName() != null && !request.getName().equals(portfolio.getName())) {
            if (portfolioRepository.existsByUserIdAndName(userId, request.getName())) {
                throw new BusinessException(ErrorCode.DUPLICATE_PORTFOLIO_NAME);
            }
        }

        portfolio.update(request.getName(), request.getDescription(), request.getTotalInvestment());
    }

    @Override
    @Transactional
    public void deletePortfolio(Long userId, Long portfolioId) {
        Portfolio portfolio = getPortfolioWithAccess(userId, portfolioId);
        portfolioRepository.delete(portfolio);
    }

    @Override
    public PortfolioPerformanceResponse getPortfolioPerformance(Long userId, Long portfolioId,
                                                                  LocalDate startDate, LocalDate endDate) {
        Portfolio portfolio = getPortfolioWithAccess(userId, portfolioId);

        BigDecimal currentValue = calculateCurrentValue(portfolio);
        BigDecimal totalReturn = currentValue.subtract(portfolio.getInvestAmount());
        BigDecimal totalReturnRate = BigDecimal.ZERO;

        if (portfolio.getInvestAmount().compareTo(BigDecimal.ZERO) > 0) {
            totalReturnRate = totalReturn
                    .divide(portfolio.getInvestAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // 일별 수익률은 실제 가격 데이터 기반으로 계산 필요 (현재는 빈 리스트)
        List<PortfolioPerformanceResponse.DailyReturn> dailyReturns = new ArrayList<>();

        return PortfolioPerformanceResponse.builder()
                .portfolioId(portfolio.getId())
                .name(portfolio.getName())
                .totalInvestment(portfolio.getInvestAmount())
                .currentValue(currentValue)
                .totalReturn(totalReturn)
                .totalReturnRate(totalReturnRate)
                .dailyReturns(dailyReturns)
                .benchmarkComparison(PortfolioPerformanceResponse.BenchmarkComparison.builder()
                        .benchmark("KOSPI 200")
                        .benchmarkReturn(BigDecimal.ZERO)
                        .alpha(totalReturnRate)
                        .build())
                .build();
    }

    @Override
    @Transactional
    public void addEtfToPortfolio(Long userId, Long portfolioId, PortfolioEtfAddRequest request) {
        Portfolio portfolio = getPortfolioWithAccess(userId, portfolioId);

        // ETF 개수 제한 체크
        if (portfolio.getPortfolioEtfs().size() >= MAX_ETF_COUNT) {
            throw new BusinessException(ErrorCode.PORTFOLIO_ETF_LIMIT_EXCEEDED);
        }

        // ETF 존재 확인
        Etf etf = etfRepository.findById(request.getEtfId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ETF_NOT_FOUND));

        // 중복 ETF 체크
        if (portfolioEtfRepository.findByPortfolioIdAndEtfId(portfolioId, request.getEtfId()).isPresent()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        PortfolioEtf portfolioEtf = PortfolioEtf.builder()
                .portfolio(portfolio)
                .etf(etf)
                .weightPct(request.getWeightPct())
                .build();

        portfolioEtfRepository.save(portfolioEtf);
    }

    @Override
    @Transactional
    public void updateEtfWeights(Long userId, Long portfolioId, PortfolioEtfUpdateRequest request) {
        Portfolio portfolio = getPortfolioWithAccess(userId, portfolioId);

        // 비중 합계 검증
        validateWeightSum(request.getEtfs().stream()
                .map(PortfolioEtfUpdateRequest.EtfWeight::getWeightPct)
                .toList());

        // 각 ETF 비중 업데이트
        for (PortfolioEtfUpdateRequest.EtfWeight etfWeight : request.getEtfs()) {
            PortfolioEtf portfolioEtf = portfolioEtfRepository.findByPortfolioIdAndEtfId(portfolioId, etfWeight.getEtfId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ETF_NOT_FOUND));
            portfolioEtf.updateWeight(etfWeight.getWeightPct());
        }
    }

    @Override
    @Transactional
    public void removeEtfFromPortfolio(Long userId, Long portfolioId, Long etfId) {
        Portfolio portfolio = getPortfolioWithAccess(userId, portfolioId);

        PortfolioEtf portfolioEtf = portfolioEtfRepository.findByPortfolioIdAndEtfId(portfolioId, etfId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ETF_NOT_FOUND));

        portfolio.removeEtf(portfolioEtf);
        portfolioEtfRepository.delete(portfolioEtf);
    }

    private Portfolio getPortfolioWithAccess(Long userId, Long portfolioId) {
        return portfolioRepository.findByIdAndUserId(portfolioId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_NOT_FOUND));
    }

    private void validateWeightSum(List<BigDecimal> weights) {
        BigDecimal sum = weights.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sum.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new BusinessException(ErrorCode.INVALID_WEIGHT_SUM);
        }
    }

    private BigDecimal calculateCurrentValue(Portfolio portfolio) {
        if (portfolio.getPortfolioEtfs().isEmpty() || portfolio.getInvestAmount() == null) {
            return BigDecimal.ZERO;
        }

        List<Long> etfIds = portfolio.getPortfolioEtfs().stream()
                .map(pe -> pe.getEtf().getId())
                .toList();

        Map<Long, EtfPrice> priceMap = etfPriceRepository.findLatestByEtfIds(etfIds).stream()
                .collect(Collectors.toMap(p -> p.getEtf().getId(), p -> p));

        BigDecimal totalValue = BigDecimal.ZERO;
        for (PortfolioEtf pe : portfolio.getPortfolioEtfs()) {
            BigDecimal invested = portfolio.getInvestAmount()
                    .multiply(pe.getWeightPct())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            EtfPrice price = priceMap.get(pe.getEtf().getId());
            if (price != null && price.getChangeRate() != null) {
                // 수익률 적용
                BigDecimal returnRate = price.getChangeRate().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                BigDecimal current = invested.multiply(BigDecimal.ONE.add(returnRate));
                totalValue = totalValue.add(current);
            } else {
                totalValue = totalValue.add(invested);
            }
        }

        return totalValue;
    }

    private List<PortfolioDetailResponse.PortfolioEtfItem> buildEtfItems(Portfolio portfolio) {
        List<Long> etfIds = portfolio.getPortfolioEtfs().stream()
                .map(pe -> pe.getEtf().getId())
                .toList();

        Map<Long, EtfPrice> priceMap = etfPriceRepository.findLatestByEtfIds(etfIds).stream()
                .collect(Collectors.toMap(p -> p.getEtf().getId(), p -> p));

        return portfolio.getPortfolioEtfs().stream()
                .map(pe -> {
                    BigDecimal invested = portfolio.getInvestAmount()
                            .multiply(pe.getWeightPct())
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                    BigDecimal current = invested;
                    BigDecimal returnRate = BigDecimal.ZERO;

                    EtfPrice price = priceMap.get(pe.getEtf().getId());
                    if (price != null && price.getChangeRate() != null) {
                        returnRate = price.getChangeRate();
                        current = invested.multiply(BigDecimal.ONE.add(
                                returnRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)));
                    }

                    return PortfolioDetailResponse.PortfolioEtfItem.builder()
                            .etfId(pe.getEtf().getId())
                            .ticker(pe.getEtf().getStockCode())
                            .name(pe.getEtf().getName())
                            .weightPct(pe.getWeightPct())
                            .investedAmount(invested)
                            .currentAmount(current)
                            .returnRate(returnRate)
                            .build();
                })
                .toList();
    }
}
