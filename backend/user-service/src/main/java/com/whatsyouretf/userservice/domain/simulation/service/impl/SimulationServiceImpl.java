package com.whatsyouretf.userservice.domain.simulation.service.impl;

import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.etf.entity.EtfPrice;
import com.whatsyouretf.userservice.domain.etf.repository.EtfPriceRepository;
import com.whatsyouretf.userservice.domain.etf.repository.EtfRepository;
import com.whatsyouretf.userservice.domain.portfolio.entity.Portfolio;
import com.whatsyouretf.userservice.domain.portfolio.entity.PortfolioEtf;
import com.whatsyouretf.userservice.domain.portfolio.repository.PortfolioRepository;
import com.whatsyouretf.userservice.domain.simulation.dto.*;
import com.whatsyouretf.userservice.domain.simulation.entity.*;
import com.whatsyouretf.userservice.domain.simulation.repository.SimulationEtfPerformanceRepository;
import com.whatsyouretf.userservice.domain.simulation.repository.SimulationMonthlyReturnRepository;
import com.whatsyouretf.userservice.domain.simulation.repository.SimulationRepository;
import com.whatsyouretf.userservice.domain.simulation.service.SimulationService;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * 시뮬레이션 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SimulationServiceImpl implements SimulationService {

    private final SimulationRepository simulationRepository;
    private final SimulationMonthlyReturnRepository monthlyReturnRepository;
    private final SimulationEtfPerformanceRepository etfPerformanceRepository;
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final EtfRepository etfRepository;
    private final EtfPriceRepository etfPriceRepository;

    private static final int MAX_SIMULATIONS = 50;

    @Override
    public SimulationListResponse getSimulations(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        Page<Simulation> simulationPage = simulationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<SimulationListResponse.SimulationSummary> summaries = simulationPage.getContent().stream()
                .map(SimulationListResponse.SimulationSummary::from)
                .toList();

        return SimulationListResponse.builder()
                .simulations(summaries)
                .page(page)
                .totalPages(simulationPage.getTotalPages())
                .totalElements(simulationPage.getTotalElements())
                .build();
    }

    @Override
    @Transactional
    public SimulationSaveResponse saveSimulation(Long userId, SimulationSaveRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Portfolio portfolio = portfolioRepository.findByIdAndUserId(request.getPortfolioId(), userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_NOT_FOUND));

        // 시뮬레이션 개수 제한 체크
        if (simulationRepository.countByUserId(userId) >= MAX_SIMULATIONS) {
            throw new BusinessException(ErrorCode.SIMULATION_LIMIT_EXCEEDED);
        }

        // 기간 검증
        validatePeriod(request.getStartDate(), request.getEndDate());

        SimulationSaveRequest.SimulationResult result = request.getResult();

        // 시뮬레이션 생성
        Simulation simulation = Simulation.builder()
                .user(user)
                .portfolio(portfolio)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .initialAmount(request.getInitialAmount())
                .finalAmount(result.getFinalAmount())
                .rebalancingCycle(request.getRebalancingCycle() != null ?
                        request.getRebalancingCycle() : RebalancingCycle.NONE)
                .totalReturn(result.getTotalReturn())
                .totalReturnRate(result.getTotalReturnRate())
                .annualizedReturn(result.getAnnualizedReturn())
                .maxDrawdown(result.getMaxDrawdown())
                .sharpeRatio(result.getSharpeRatio())
                .volatility(result.getVolatility())
                .build();

        simulationRepository.save(simulation);

        // 월별 수익률 저장
        for (SimulationSaveRequest.MonthlyReturn monthlyReturn : result.getMonthlyReturns()) {
            SimulationMonthlyReturn smr = SimulationMonthlyReturn.builder()
                    .simulation(simulation)
                    .month(monthlyReturn.getMonth())
                    .value(monthlyReturn.getValue())
                    .returnRate(monthlyReturn.getReturnRate())
                    .build();
            monthlyReturnRepository.save(smr);
        }

        // ETF 성과 저장
        for (SimulationSaveRequest.EtfPerformance etfPerformance : result.getEtfPerformance()) {
            Etf etf = etfRepository.findById(etfPerformance.getEtfId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ETF_NOT_FOUND));

            SimulationEtfPerformance sep = SimulationEtfPerformance.builder()
                    .simulation(simulation)
                    .etf(etf)
                    .weightPct(etfPerformance.getWeightPct())
                    .returnRate(etfPerformance.getReturnRate())
                    .contribution(etfPerformance.getContribution())
                    .build();
            etfPerformanceRepository.save(sep);
        }

        return SimulationSaveResponse.of(simulation.getId());
    }

    @Override
    public SimulationDetailResponse getSimulationDetail(Long userId, Long simulationId) {
        Simulation simulation = simulationRepository.findByIdAndUserId(simulationId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SIMULATION_NOT_FOUND));

        List<SimulationMonthlyReturn> monthlyReturns =
                monthlyReturnRepository.findBySimulationIdOrderByMonth(simulationId);
        List<SimulationEtfPerformance> etfPerformances =
                etfPerformanceRepository.findBySimulationIdWithEtf(simulationId);

        return SimulationDetailResponse.from(simulation, monthlyReturns, etfPerformances);
    }

    @Override
    @Transactional
    public void deleteSimulation(Long userId, Long simulationId) {
        Simulation simulation = simulationRepository.findByIdAndUserId(simulationId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SIMULATION_NOT_FOUND));

        simulationRepository.delete(simulation);
    }

    @Override
    public SimulationCompareResponse comparePortfolios(Long userId, SimulationCompareRequest request) {
        // 기간 검증
        validatePeriod(request.getStartDate(), request.getEndDate());

        List<SimulationCompareResponse.PortfolioComparison> comparisons = new ArrayList<>();

        for (Long portfolioId : request.getPortfolioIds()) {
            Portfolio portfolio = portfolioRepository.findByIdAndUserId(portfolioId, userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_NOT_FOUND));

            // 포트폴리오 성과 계산
            BigDecimal finalAmount = calculatePortfolioValue(
                    portfolio, request.getInitialAmount(), request.getStartDate(), request.getEndDate());
            BigDecimal totalReturnRate = calculateReturnRate(request.getInitialAmount(), finalAmount);
            BigDecimal maxDrawdown = calculateMaxDrawdown(portfolio, request.getStartDate(), request.getEndDate());
            BigDecimal sharpeRatio = calculateSharpeRatio(portfolio, request.getStartDate(), request.getEndDate());

            comparisons.add(SimulationCompareResponse.PortfolioComparison.builder()
                    .portfolioId(portfolio.getId())
                    .portfolioName(portfolio.getName())
                    .finalAmount(finalAmount)
                    .totalReturnRate(totalReturnRate)
                    .maxDrawdown(maxDrawdown)
                    .sharpeRatio(sharpeRatio)
                    .rank(0)
                    .build());
        }

        // 수익률 기준 순위 매기기
        comparisons.sort((a, b) -> b.getTotalReturnRate().compareTo(a.getTotalReturnRate()));
        List<SimulationCompareResponse.PortfolioComparison> rankedComparisons = new ArrayList<>();
        for (int i = 0; i < comparisons.size(); i++) {
            SimulationCompareResponse.PortfolioComparison original = comparisons.get(i);
            rankedComparisons.add(SimulationCompareResponse.PortfolioComparison.builder()
                    .portfolioId(original.getPortfolioId())
                    .portfolioName(original.getPortfolioName())
                    .finalAmount(original.getFinalAmount())
                    .totalReturnRate(original.getTotalReturnRate())
                    .maxDrawdown(original.getMaxDrawdown())
                    .sharpeRatio(original.getSharpeRatio())
                    .rank(i + 1)
                    .build());
        }

        return SimulationCompareResponse.builder()
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .initialAmount(request.getInitialAmount())
                .comparisons(rankedComparisons)
                .benchmark(SimulationCompareResponse.Benchmark.builder()
                        .name("KOSPI 200")
                        .returnRate(BigDecimal.valueOf(10.0))
                        .build())
                .build();
    }

    /**
     * 기간 검증
     */
    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.INVALID_SIMULATION_PERIOD);
        }
        if (endDate.isAfter(LocalDate.now())) {
            throw new BusinessException(ErrorCode.INVALID_SIMULATION_PERIOD);
        }
        if (startDate.isBefore(LocalDate.of(2010, 1, 1))) {
            throw new BusinessException(ErrorCode.INVALID_SIMULATION_PERIOD);
        }
    }

    /**
     * 포트폴리오 가치 계산
     */
    private BigDecimal calculatePortfolioValue(Portfolio portfolio, BigDecimal initialAmount,
                                                LocalDate startDate, LocalDate endDate) {
        if (portfolio.getPortfolioEtfs().isEmpty()) {
            return initialAmount;
        }

        List<Long> etfIds = portfolio.getPortfolioEtfs().stream()
                .map(pe -> pe.getEtf().getId())
                .toList();

        Map<Long, EtfPrice> priceMap = etfPriceRepository.findLatestByEtfIds(etfIds).stream()
                .collect(Collectors.toMap(p -> p.getEtf().getId(), p -> p));

        BigDecimal totalValue = BigDecimal.ZERO;
        for (PortfolioEtf pe : portfolio.getPortfolioEtfs()) {
            BigDecimal invested = initialAmount
                    .multiply(pe.getWeightPct())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            EtfPrice price = priceMap.get(pe.getEtf().getId());
            if (price != null && price.getChangeRate() != null) {
                BigDecimal returnRate = price.getChangeRate().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                BigDecimal current = invested.multiply(BigDecimal.ONE.add(returnRate));
                totalValue = totalValue.add(current);
            } else {
                totalValue = totalValue.add(invested);
            }
        }

        return totalValue;
    }

    /**
     * 수익률 계산
     */
    private BigDecimal calculateReturnRate(BigDecimal initial, BigDecimal current) {
        if (initial.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return current.subtract(initial)
                .divide(initial, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * 최대 낙폭 계산 (간소화된 버전)
     */
    private BigDecimal calculateMaxDrawdown(Portfolio portfolio, LocalDate startDate, LocalDate endDate) {
        // 실제 구현에서는 일별 가격 데이터를 기반으로 계산
        return BigDecimal.valueOf(-5.0);
    }

    /**
     * 샤프 비율 계산 (간소화된 버전)
     */
    private BigDecimal calculateSharpeRatio(Portfolio portfolio, LocalDate startDate, LocalDate endDate) {
        // 실제 구현에서는 수익률의 표준편차를 기반으로 계산
        return BigDecimal.valueOf(1.0);
    }
}
