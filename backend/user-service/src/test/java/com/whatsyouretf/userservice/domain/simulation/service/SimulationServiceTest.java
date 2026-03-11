package com.whatsyouretf.userservice.domain.simulation.service;

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
import com.whatsyouretf.userservice.domain.simulation.entity.RebalancingCycle;
import com.whatsyouretf.userservice.domain.simulation.entity.Simulation;
import com.whatsyouretf.userservice.domain.simulation.entity.SimulationEtfPerformance;
import com.whatsyouretf.userservice.domain.simulation.entity.SimulationMonthlyReturn;
import com.whatsyouretf.userservice.domain.simulation.repository.SimulationEtfPerformanceRepository;
import com.whatsyouretf.userservice.domain.simulation.repository.SimulationMonthlyReturnRepository;
import com.whatsyouretf.userservice.domain.simulation.repository.SimulationRepository;
import com.whatsyouretf.userservice.domain.simulation.service.impl.SimulationServiceImpl;
import com.whatsyouretf.userservice.domain.user.entity.User;
import com.whatsyouretf.userservice.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * SimulationService 단위 테스트
 * <p>
 * 테스트 범위:
 * - 시뮬레이션 목록 조회
 * - 시뮬레이션 결과 저장
 * - 시뮬레이션 상세 조회
 * - 시뮬레이션 삭제
 * - 포트폴리오 비교
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SimulationService 단위 테스트")
class SimulationServiceTest {

    @InjectMocks
    private SimulationServiceImpl simulationService;

    @Mock
    private SimulationRepository simulationRepository;

    @Mock
    private SimulationMonthlyReturnRepository monthlyReturnRepository;

    @Mock
    private SimulationEtfPerformanceRepository etfPerformanceRepository;

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EtfRepository etfRepository;

    @Mock
    private EtfPriceRepository etfPriceRepository;

    // 테스트 데이터
    private User testUser;
    private Etf testEtf;
    private EtfPrice testEtfPrice;
    private Portfolio testPortfolio;
    private Simulation testSimulation;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("테스트유저")
                .isActive(true)
                .build();

        // 테스트 ETF 생성
        testEtf = Etf.builder()
                .id(100L)
                .stockCode("069500")
                .name("KODEX 200")
                .category("국내주식형")
                .assetManager("삼성자산운용")
                .isActive(true)
                .build();

        // 테스트 ETF 시세 생성
        testEtfPrice = EtfPrice.builder()
                .id(1L)
                .etf(testEtf)
                .tradeDate(LocalDate.now())
                .close(BigDecimal.valueOf(35000))
                .changeRate(BigDecimal.valueOf(5.0))
                .build();

        // 테스트 포트폴리오 ETF 생성
        PortfolioEtf portfolioEtf = PortfolioEtf.builder()
                .id(1L)
                .etf(testEtf)
                .weightPct(BigDecimal.valueOf(100))
                .build();

        // 테스트 포트폴리오 생성
        testPortfolio = Portfolio.builder()
                .id(1L)
                .user(testUser)
                .name("나의 성장 포트폴리오")
                .description("성장주 중심")
                .investAmount(BigDecimal.valueOf(10000000))
                .portfolioEtfs(new ArrayList<>(List.of(portfolioEtf)))
                .build();

        // portfolioEtf에 portfolio 설정
        portfolioEtf = PortfolioEtf.builder()
                .id(1L)
                .portfolio(testPortfolio)
                .etf(testEtf)
                .weightPct(BigDecimal.valueOf(100))
                .build();
        testPortfolio.getPortfolioEtfs().clear();
        testPortfolio.getPortfolioEtfs().add(portfolioEtf);

        // 테스트 시뮬레이션 생성
        testSimulation = Simulation.builder()
                .id(1L)
                .user(testUser)
                .portfolio(testPortfolio)
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 12, 31))
                .initialAmount(BigDecimal.valueOf(10000000))
                .finalAmount(BigDecimal.valueOf(11500000))
                .rebalancingCycle(RebalancingCycle.MONTHLY)
                .totalReturn(BigDecimal.valueOf(1500000))
                .totalReturnRate(BigDecimal.valueOf(15.0))
                .annualizedReturn(BigDecimal.valueOf(15.0))
                .maxDrawdown(BigDecimal.valueOf(-8.5))
                .sharpeRatio(BigDecimal.valueOf(1.2))
                .volatility(BigDecimal.valueOf(12.5))
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ========== 시뮬레이션 목록 조회 테스트 ==========

    @Nested
    @DisplayName("시뮬레이션 목록 조회 테스트")
    class GetSimulationsTest {

        @Test
        @DisplayName("시뮬레이션 목록 조회 - 정상 조회")
        void getSimulations_Success() {
            // given
            Page<Simulation> simulationPage = new PageImpl<>(List.of(testSimulation));
            given(simulationRepository.findByUserIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                    .willReturn(simulationPage);

            // when
            SimulationListResponse response = simulationService.getSimulations(1L, 0, 10);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getSimulations()).hasSize(1);
            assertThat(response.getSimulations().get(0).getPortfolioName()).isEqualTo("나의 성장 포트폴리오");
        }

        @Test
        @DisplayName("시뮬레이션 목록 조회 - 빈 목록인 경우")
        void getSimulations_Empty_ReturnsEmptyList() {
            // given
            Page<Simulation> emptyPage = Page.empty();
            given(simulationRepository.findByUserIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                    .willReturn(emptyPage);

            // when
            SimulationListResponse response = simulationService.getSimulations(1L, 0, 10);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getSimulations()).isEmpty();
            assertThat(response.getTotalElements()).isZero();
        }
    }

    // ========== 시뮬레이션 저장 테스트 ==========

    @Nested
    @DisplayName("시뮬레이션 저장 테스트")
    class SaveSimulationTest {

        @Test
        @DisplayName("시뮬레이션 저장 - 정상 저장")
        void saveSimulation_Success() {
            // given
            SimulationSaveRequest request = SimulationSaveRequest.builder()
                    .portfolioId(1L)
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2024, 12, 31))
                    .initialAmount(BigDecimal.valueOf(10000000))
                    .rebalancingCycle(RebalancingCycle.MONTHLY)
                    .result(SimulationSaveRequest.SimulationResult.builder()
                            .finalAmount(BigDecimal.valueOf(11500000))
                            .totalReturn(BigDecimal.valueOf(1500000))
                            .totalReturnRate(BigDecimal.valueOf(15.0))
                            .annualizedReturn(BigDecimal.valueOf(15.0))
                            .maxDrawdown(BigDecimal.valueOf(-8.5))
                            .sharpeRatio(BigDecimal.valueOf(1.2))
                            .volatility(BigDecimal.valueOf(12.5))
                            .monthlyReturns(List.of(
                                    SimulationSaveRequest.MonthlyReturn.builder()
                                            .month("2024-01")
                                            .value(BigDecimal.valueOf(10200000))
                                            .returnRate(BigDecimal.valueOf(2.0))
                                            .build()
                            ))
                            .etfPerformance(List.of(
                                    SimulationSaveRequest.EtfPerformance.builder()
                                            .etfId(100L)
                                            .weightPct(BigDecimal.valueOf(100))
                                            .returnRate(BigDecimal.valueOf(15.0))
                                            .contribution(BigDecimal.valueOf(15.0))
                                            .build()
                            ))
                            .build())
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(portfolioRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(testPortfolio));
            given(simulationRepository.countByUserId(1L)).willReturn(0L);
            given(etfRepository.findById(100L)).willReturn(Optional.of(testEtf));

            // when
            SimulationSaveResponse response = simulationService.saveSimulation(1L, request);

            // then
            assertThat(response).isNotNull();
            then(simulationRepository).should().save(any(Simulation.class));
            then(monthlyReturnRepository).should().save(any(SimulationMonthlyReturn.class));
            then(etfPerformanceRepository).should().save(any(SimulationEtfPerformance.class));
        }

        @Test
        @DisplayName("시뮬레이션 저장 - 사용자가 존재하지 않으면 예외 발생")
        void saveSimulation_UserNotFound_ThrowsException() {
            // given
            SimulationSaveRequest request = SimulationSaveRequest.builder()
                    .portfolioId(1L)
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2024, 12, 31))
                    .initialAmount(BigDecimal.valueOf(10000000))
                    .result(SimulationSaveRequest.SimulationResult.builder()
                            .finalAmount(BigDecimal.valueOf(11500000))
                            .totalReturn(BigDecimal.valueOf(1500000))
                            .totalReturnRate(BigDecimal.valueOf(15.0))
                            .annualizedReturn(BigDecimal.valueOf(15.0))
                            .maxDrawdown(BigDecimal.valueOf(-8.5))
                            .sharpeRatio(BigDecimal.valueOf(1.2))
                            .volatility(BigDecimal.valueOf(12.5))
                            .monthlyReturns(List.of())
                            .etfPerformance(List.of())
                            .build())
                    .build();

            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> simulationService.saveSimulation(999L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("시뮬레이션 저장 - 포트폴리오가 존재하지 않으면 예외 발생")
        void saveSimulation_PortfolioNotFound_ThrowsException() {
            // given
            SimulationSaveRequest request = SimulationSaveRequest.builder()
                    .portfolioId(999L)
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2024, 12, 31))
                    .initialAmount(BigDecimal.valueOf(10000000))
                    .result(SimulationSaveRequest.SimulationResult.builder()
                            .finalAmount(BigDecimal.valueOf(11500000))
                            .totalReturn(BigDecimal.valueOf(1500000))
                            .totalReturnRate(BigDecimal.valueOf(15.0))
                            .annualizedReturn(BigDecimal.valueOf(15.0))
                            .maxDrawdown(BigDecimal.valueOf(-8.5))
                            .sharpeRatio(BigDecimal.valueOf(1.2))
                            .volatility(BigDecimal.valueOf(12.5))
                            .monthlyReturns(List.of())
                            .etfPerformance(List.of())
                            .build())
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(portfolioRepository.findByIdAndUserId(999L, 1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> simulationService.saveSimulation(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.PORTFOLIO_NOT_FOUND);
        }

        @Test
        @DisplayName("시뮬레이션 저장 - 개수 제한 초과 시 예외 발생")
        void saveSimulation_LimitExceeded_ThrowsException() {
            // given
            SimulationSaveRequest request = SimulationSaveRequest.builder()
                    .portfolioId(1L)
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2024, 12, 31))
                    .initialAmount(BigDecimal.valueOf(10000000))
                    .result(SimulationSaveRequest.SimulationResult.builder()
                            .finalAmount(BigDecimal.valueOf(11500000))
                            .totalReturn(BigDecimal.valueOf(1500000))
                            .totalReturnRate(BigDecimal.valueOf(15.0))
                            .annualizedReturn(BigDecimal.valueOf(15.0))
                            .maxDrawdown(BigDecimal.valueOf(-8.5))
                            .sharpeRatio(BigDecimal.valueOf(1.2))
                            .volatility(BigDecimal.valueOf(12.5))
                            .monthlyReturns(List.of())
                            .etfPerformance(List.of())
                            .build())
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(portfolioRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(testPortfolio));
            given(simulationRepository.countByUserId(1L)).willReturn(50L);

            // when & then
            assertThatThrownBy(() -> simulationService.saveSimulation(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.SIMULATION_LIMIT_EXCEEDED);
        }

        @Test
        @DisplayName("시뮬레이션 저장 - 잘못된 기간이면 예외 발생")
        void saveSimulation_InvalidPeriod_ThrowsException() {
            // given
            SimulationSaveRequest request = SimulationSaveRequest.builder()
                    .portfolioId(1L)
                    .startDate(LocalDate.of(2024, 12, 31))
                    .endDate(LocalDate.of(2024, 1, 1))
                    .initialAmount(BigDecimal.valueOf(10000000))
                    .result(SimulationSaveRequest.SimulationResult.builder()
                            .finalAmount(BigDecimal.valueOf(11500000))
                            .totalReturn(BigDecimal.valueOf(1500000))
                            .totalReturnRate(BigDecimal.valueOf(15.0))
                            .annualizedReturn(BigDecimal.valueOf(15.0))
                            .maxDrawdown(BigDecimal.valueOf(-8.5))
                            .sharpeRatio(BigDecimal.valueOf(1.2))
                            .volatility(BigDecimal.valueOf(12.5))
                            .monthlyReturns(List.of())
                            .etfPerformance(List.of())
                            .build())
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(portfolioRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(testPortfolio));
            given(simulationRepository.countByUserId(1L)).willReturn(0L);

            // when & then
            assertThatThrownBy(() -> simulationService.saveSimulation(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.INVALID_SIMULATION_PERIOD);
        }
    }

    // ========== 시뮬레이션 상세 조회 테스트 ==========

    @Nested
    @DisplayName("시뮬레이션 상세 조회 테스트")
    class GetSimulationDetailTest {

        @Test
        @DisplayName("시뮬레이션 상세 조회 - 정상 조회")
        void getSimulationDetail_Success() {
            // given
            SimulationMonthlyReturn monthlyReturn = SimulationMonthlyReturn.builder()
                    .id(1L)
                    .simulation(testSimulation)
                    .month("2024-01")
                    .value(BigDecimal.valueOf(10200000))
                    .returnRate(BigDecimal.valueOf(2.0))
                    .build();

            SimulationEtfPerformance etfPerformance = SimulationEtfPerformance.builder()
                    .id(1L)
                    .simulation(testSimulation)
                    .etf(testEtf)
                    .weightPct(BigDecimal.valueOf(100))
                    .returnRate(BigDecimal.valueOf(15.0))
                    .contribution(BigDecimal.valueOf(15.0))
                    .build();

            given(simulationRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(testSimulation));
            given(monthlyReturnRepository.findBySimulationIdOrderByMonth(1L)).willReturn(List.of(monthlyReturn));
            given(etfPerformanceRepository.findBySimulationIdWithEtf(1L)).willReturn(List.of(etfPerformance));

            // when
            SimulationDetailResponse response = simulationService.getSimulationDetail(1L, 1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getPortfolioName()).isEqualTo("나의 성장 포트폴리오");
            assertThat(response.getSummary().getTotalReturnRate()).isEqualTo(BigDecimal.valueOf(15.0));
            assertThat(response.getMonthlyReturns()).hasSize(1);
            assertThat(response.getEtfPerformance()).hasSize(1);
        }

        @Test
        @DisplayName("시뮬레이션 상세 조회 - 존재하지 않으면 예외 발생")
        void getSimulationDetail_NotFound_ThrowsException() {
            // given
            given(simulationRepository.findByIdAndUserId(999L, 1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> simulationService.getSimulationDetail(1L, 999L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.SIMULATION_NOT_FOUND);
        }
    }

    // ========== 시뮬레이션 삭제 테스트 ==========

    @Nested
    @DisplayName("시뮬레이션 삭제 테스트")
    class DeleteSimulationTest {

        @Test
        @DisplayName("시뮬레이션 삭제 - 정상 삭제")
        void deleteSimulation_Success() {
            // given
            given(simulationRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(testSimulation));

            // when
            simulationService.deleteSimulation(1L, 1L);

            // then
            then(simulationRepository).should().delete(testSimulation);
        }

        @Test
        @DisplayName("시뮬레이션 삭제 - 존재하지 않으면 예외 발생")
        void deleteSimulation_NotFound_ThrowsException() {
            // given
            given(simulationRepository.findByIdAndUserId(999L, 1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> simulationService.deleteSimulation(1L, 999L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.SIMULATION_NOT_FOUND);
        }
    }

    // ========== 포트폴리오 비교 테스트 ==========

    @Nested
    @DisplayName("포트폴리오 비교 테스트")
    class ComparePortfoliosTest {

        @Test
        @DisplayName("포트폴리오 비교 - 정상 비교")
        void comparePortfolios_Success() {
            // given
            Portfolio portfolio2 = Portfolio.builder()
                    .id(2L)
                    .user(testUser)
                    .name("배당 포트폴리오")
                    .investAmount(BigDecimal.valueOf(10000000))
                    .portfolioEtfs(new ArrayList<>())
                    .build();

            SimulationCompareRequest request = SimulationCompareRequest.builder()
                    .portfolioIds(List.of(1L, 2L))
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2024, 12, 31))
                    .initialAmount(BigDecimal.valueOf(10000000))
                    .build();

            given(portfolioRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(testPortfolio));
            given(portfolioRepository.findByIdAndUserId(2L, 1L)).willReturn(Optional.of(portfolio2));
            given(etfPriceRepository.findLatestByEtfIds(anyList())).willReturn(List.of(testEtfPrice));

            // when
            SimulationCompareResponse response = simulationService.comparePortfolios(1L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getComparisons()).hasSize(2);
            assertThat(response.getBenchmark().getName()).isEqualTo("KOSPI 200");
            // 순위가 매겨져 있는지 확인
            assertThat(response.getComparisons().get(0).getRank()).isEqualTo(1);
            assertThat(response.getComparisons().get(1).getRank()).isEqualTo(2);
        }

        @Test
        @DisplayName("포트폴리오 비교 - 포트폴리오가 존재하지 않으면 예외 발생")
        void comparePortfolios_PortfolioNotFound_ThrowsException() {
            // given
            SimulationCompareRequest request = SimulationCompareRequest.builder()
                    .portfolioIds(List.of(1L, 999L))
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2024, 12, 31))
                    .initialAmount(BigDecimal.valueOf(10000000))
                    .build();

            given(portfolioRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(testPortfolio));
            given(portfolioRepository.findByIdAndUserId(999L, 1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> simulationService.comparePortfolios(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.PORTFOLIO_NOT_FOUND);
        }

        @Test
        @DisplayName("포트폴리오 비교 - 잘못된 기간이면 예외 발생")
        void comparePortfolios_InvalidPeriod_ThrowsException() {
            // given
            SimulationCompareRequest request = SimulationCompareRequest.builder()
                    .portfolioIds(List.of(1L, 2L))
                    .startDate(LocalDate.of(2024, 12, 31))
                    .endDate(LocalDate.of(2024, 1, 1))
                    .initialAmount(BigDecimal.valueOf(10000000))
                    .build();

            // when & then
            assertThatThrownBy(() -> simulationService.comparePortfolios(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.INVALID_SIMULATION_PERIOD);
        }
    }
}
