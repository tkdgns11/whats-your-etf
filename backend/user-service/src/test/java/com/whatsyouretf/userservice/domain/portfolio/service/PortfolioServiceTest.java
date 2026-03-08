package com.whatsyouretf.userservice.domain.portfolio.service;

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
import com.whatsyouretf.userservice.domain.portfolio.service.impl.PortfolioServiceImpl;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * PortfolioService 단위 테스트
 * <p>
 * 테스트 범위:
 * - 포트폴리오 CRUD (조회, 생성, 수정, 삭제)
 * - 포트폴리오 ETF 관리 (추가, 비중 수정, 삭제)
 * - 포트폴리오 성과 조회
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PortfolioService 단위 테스트")
class PortfolioServiceTest {

    @InjectMocks
    private PortfolioServiceImpl portfolioService;

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private PortfolioEtfRepository portfolioEtfRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EtfRepository etfRepository;

    @Mock
    private EtfPriceRepository etfPriceRepository;

    // 테스트 데이터
    private User testUser;
    private Etf testEtf1;
    private Etf testEtf2;
    private Portfolio testPortfolio;
    private PortfolioEtf testPortfolioEtf;
    private EtfPrice testEtfPrice;

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
        testEtf1 = Etf.builder()
                .id(100L)
                .stockCode("069500")
                .name("KODEX 200")
                .category("국내주식형")
                .assetManager("삼성자산운용")
                .isActive(true)
                .build();

        testEtf2 = Etf.builder()
                .id(101L)
                .stockCode("102110")
                .name("TIGER 200")
                .category("국내주식형")
                .assetManager("미래에셋자산운용")
                .isActive(true)
                .build();

        // 테스트 포트폴리오 생성
        testPortfolio = Portfolio.builder()
                .id(1L)
                .user(testUser)
                .name("나의 성장 포트폴리오")
                .description("성장주 중심")
                .investAmount(BigDecimal.valueOf(10000000))
                .portfolioEtfs(new ArrayList<>())
                .build();

        // 테스트 포트폴리오 ETF 생성
        testPortfolioEtf = PortfolioEtf.builder()
                .id(1L)
                .portfolio(testPortfolio)
                .etf(testEtf1)
                .weightPct(BigDecimal.valueOf(100))
                .build();
        testPortfolio.getPortfolioEtfs().add(testPortfolioEtf);

        // 테스트 ETF 시세 생성
        testEtfPrice = EtfPrice.builder()
                .id(1L)
                .etf(testEtf1)
                .tradeDate(LocalDate.now())
                .close(BigDecimal.valueOf(35000))
                .changeRate(BigDecimal.valueOf(5.0))
                .build();
    }

    // ========== 포트폴리오 목록 조회 테스트 ==========

    @Nested
    @DisplayName("포트폴리오 목록 조회 테스트")
    class GetPortfoliosTest {

        @Test
        @DisplayName("포트폴리오 목록 조회 - 정상 조회")
        void getPortfolios_Success() {
            // given
            Page<Portfolio> portfolioPage = new PageImpl<>(List.of(testPortfolio));
            given(portfolioRepository.findByUserIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                    .willReturn(portfolioPage);
            given(etfPriceRepository.findLatestByEtfIds(anyList())).willReturn(List.of(testEtfPrice));

            // when
            PortfolioListResponse response = portfolioService.getPortfolios(1L, 0, 10);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getPortfolios()).hasSize(1);
            assertThat(response.getPortfolios().get(0).getName()).isEqualTo("나의 성장 포트폴리오");
        }

        @Test
        @DisplayName("포트폴리오 목록 조회 - 포트폴리오가 없는 경우 빈 목록 반환")
        void getPortfolios_Empty_ReturnsEmptyList() {
            // given
            Page<Portfolio> emptyPage = Page.empty();
            given(portfolioRepository.findByUserIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                    .willReturn(emptyPage);

            // when
            PortfolioListResponse response = portfolioService.getPortfolios(1L, 0, 10);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getPortfolios()).isEmpty();
            assertThat(response.getTotalElements()).isZero();
        }
    }

    // ========== 포트폴리오 생성 테스트 ==========

    @Nested
    @DisplayName("포트폴리오 생성 테스트")
    class CreatePortfolioTest {

        @Test
        @DisplayName("포트폴리오 생성 - 정상 생성")
        void createPortfolio_Success() {
            // given
            PortfolioCreateRequest request = PortfolioCreateRequest.builder()
                    .name("새 포트폴리오")
                    .description("테스트 포트폴리오")
                    .totalInvestment(BigDecimal.valueOf(5000000))
                    .etfs(List.of(
                            PortfolioCreateRequest.EtfItem.builder()
                                    .etfId(100L)
                                    .weightPct(BigDecimal.valueOf(60))
                                    .build(),
                            PortfolioCreateRequest.EtfItem.builder()
                                    .etfId(101L)
                                    .weightPct(BigDecimal.valueOf(40))
                                    .build()
                    ))
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(portfolioRepository.countByUserId(1L)).willReturn(0L);
            given(portfolioRepository.existsByUserIdAndName(1L, "새 포트폴리오")).willReturn(false);
            given(etfRepository.findById(100L)).willReturn(Optional.of(testEtf1));
            given(etfRepository.findById(101L)).willReturn(Optional.of(testEtf2));

            // when
            PortfolioCreateResponse response = portfolioService.createPortfolio(1L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getName()).isEqualTo("새 포트폴리오");
            then(portfolioRepository).should().save(any(Portfolio.class));
            then(portfolioEtfRepository).should(times(2)).save(any(PortfolioEtf.class));
        }

        @Test
        @DisplayName("포트폴리오 생성 - 사용자가 존재하지 않으면 예외 발생")
        void createPortfolio_UserNotFound_ThrowsException() {
            // given
            PortfolioCreateRequest request = PortfolioCreateRequest.builder()
                    .name("새 포트폴리오")
                    .totalInvestment(BigDecimal.valueOf(5000000))
                    .etfs(List.of())
                    .build();

            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> portfolioService.createPortfolio(999L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("포트폴리오 생성 - 포트폴리오 개수 초과 시 예외 발생")
        void createPortfolio_LimitExceeded_ThrowsException() {
            // given
            PortfolioCreateRequest request = PortfolioCreateRequest.builder()
                    .name("새 포트폴리오")
                    .totalInvestment(BigDecimal.valueOf(5000000))
                    .etfs(List.of())
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(portfolioRepository.countByUserId(1L)).willReturn(10L);

            // when & then
            assertThatThrownBy(() -> portfolioService.createPortfolio(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.PORTFOLIO_ETF_LIMIT_EXCEEDED);
        }

        @Test
        @DisplayName("포트폴리오 생성 - 이름 중복 시 예외 발생")
        void createPortfolio_DuplicateName_ThrowsException() {
            // given
            PortfolioCreateRequest request = PortfolioCreateRequest.builder()
                    .name("나의 성장 포트폴리오")
                    .totalInvestment(BigDecimal.valueOf(5000000))
                    .etfs(List.of())
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(portfolioRepository.countByUserId(1L)).willReturn(0L);
            given(portfolioRepository.existsByUserIdAndName(1L, "나의 성장 포트폴리오")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> portfolioService.createPortfolio(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.DUPLICATE_PORTFOLIO_NAME);
        }

        @Test
        @DisplayName("포트폴리오 생성 - ETF 비중 합계가 100%가 아니면 예외 발생")
        void createPortfolio_InvalidWeightSum_ThrowsException() {
            // given
            PortfolioCreateRequest request = PortfolioCreateRequest.builder()
                    .name("새 포트폴리오")
                    .totalInvestment(BigDecimal.valueOf(5000000))
                    .etfs(List.of(
                            PortfolioCreateRequest.EtfItem.builder()
                                    .etfId(100L)
                                    .weightPct(BigDecimal.valueOf(50))
                                    .build()
                    ))
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(portfolioRepository.countByUserId(1L)).willReturn(0L);
            given(portfolioRepository.existsByUserIdAndName(1L, "새 포트폴리오")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> portfolioService.createPortfolio(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.INVALID_WEIGHT_SUM);
        }
    }

    // ========== 포트폴리오 상세 조회 테스트 ==========

    @Nested
    @DisplayName("포트폴리오 상세 조회 테스트")
    class GetPortfolioDetailTest {

        @Test
        @DisplayName("포트폴리오 상세 조회 - 정상 조회")
        void getPortfolioDetail_Success() {
            // given
            given(portfolioRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(testPortfolio));
            given(etfPriceRepository.findLatestByEtfIds(List.of(100L))).willReturn(List.of(testEtfPrice));

            // when
            PortfolioDetailResponse response = portfolioService.getPortfolioDetail(1L, 1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getName()).isEqualTo("나의 성장 포트폴리오");
            assertThat(response.getTotalInvestment()).isEqualTo(BigDecimal.valueOf(10000000));
        }

        @Test
        @DisplayName("포트폴리오 상세 조회 - 존재하지 않는 포트폴리오는 예외 발생")
        void getPortfolioDetail_NotFound_ThrowsException() {
            // given
            given(portfolioRepository.findByIdAndUserId(999L, 1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> portfolioService.getPortfolioDetail(1L, 999L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.PORTFOLIO_NOT_FOUND);
        }
    }

    // ========== 포트폴리오 수정 테스트 ==========

    @Nested
    @DisplayName("포트폴리오 수정 테스트")
    class UpdatePortfolioTest {

        @Test
        @DisplayName("포트폴리오 수정 - 정상 수정")
        void updatePortfolio_Success() {
            // given
            PortfolioUpdateRequest request = PortfolioUpdateRequest.builder()
                    .name("수정된 포트폴리오")
                    .description("수정된 설명")
                    .totalInvestment(BigDecimal.valueOf(20000000))
                    .build();

            given(portfolioRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(testPortfolio));
            given(portfolioRepository.existsByUserIdAndName(1L, "수정된 포트폴리오")).willReturn(false);

            // when
            portfolioService.updatePortfolio(1L, 1L, request);

            // then
            assertThat(testPortfolio.getName()).isEqualTo("수정된 포트폴리오");
        }

        @Test
        @DisplayName("포트폴리오 수정 - 존재하지 않는 포트폴리오는 예외 발생")
        void updatePortfolio_NotFound_ThrowsException() {
            // given
            PortfolioUpdateRequest request = PortfolioUpdateRequest.builder()
                    .name("수정된 포트폴리오")
                    .build();

            given(portfolioRepository.findByIdAndUserId(999L, 1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> portfolioService.updatePortfolio(1L, 999L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.PORTFOLIO_NOT_FOUND);
        }
    }

    // ========== 포트폴리오 삭제 테스트 ==========

    @Nested
    @DisplayName("포트폴리오 삭제 테스트")
    class DeletePortfolioTest {

        @Test
        @DisplayName("포트폴리오 삭제 - 정상 삭제")
        void deletePortfolio_Success() {
            // given
            given(portfolioRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(testPortfolio));

            // when
            portfolioService.deletePortfolio(1L, 1L);

            // then
            then(portfolioRepository).should().delete(testPortfolio);
        }

        @Test
        @DisplayName("포트폴리오 삭제 - 존재하지 않는 포트폴리오는 예외 발생")
        void deletePortfolio_NotFound_ThrowsException() {
            // given
            given(portfolioRepository.findByIdAndUserId(999L, 1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> portfolioService.deletePortfolio(1L, 999L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.PORTFOLIO_NOT_FOUND);
        }
    }

    // ========== 포트폴리오 ETF 추가 테스트 ==========

    @Nested
    @DisplayName("포트폴리오 ETF 추가 테스트")
    class AddEtfToPortfolioTest {

        @Test
        @DisplayName("포트폴리오 ETF 추가 - 정상 추가")
        void addEtfToPortfolio_Success() {
            // given
            PortfolioEtfAddRequest request = PortfolioEtfAddRequest.builder()
                    .etfId(101L)
                    .weightPct(BigDecimal.valueOf(30))
                    .build();

            given(portfolioRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(testPortfolio));
            given(etfRepository.findById(101L)).willReturn(Optional.of(testEtf2));
            given(portfolioEtfRepository.findByPortfolioIdAndEtfId(1L, 101L)).willReturn(Optional.empty());

            // when
            portfolioService.addEtfToPortfolio(1L, 1L, request);

            // then
            then(portfolioEtfRepository).should().save(any(PortfolioEtf.class));
        }

        @Test
        @DisplayName("포트폴리오 ETF 추가 - ETF 개수 제한 초과 시 예외 발생")
        void addEtfToPortfolio_LimitExceeded_ThrowsException() {
            // given
            // 포트폴리오에 이미 20개의 ETF가 있는 상황 시뮬레이션
            List<PortfolioEtf> manyEtfs = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                manyEtfs.add(PortfolioEtf.builder().build());
            }
            Portfolio portfolioWithManyEtfs = Portfolio.builder()
                    .id(1L)
                    .user(testUser)
                    .name("테스트")
                    .portfolioEtfs(manyEtfs)
                    .build();

            PortfolioEtfAddRequest request = PortfolioEtfAddRequest.builder()
                    .etfId(101L)
                    .weightPct(BigDecimal.valueOf(30))
                    .build();

            given(portfolioRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(portfolioWithManyEtfs));

            // when & then
            assertThatThrownBy(() -> portfolioService.addEtfToPortfolio(1L, 1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.PORTFOLIO_ETF_LIMIT_EXCEEDED);
        }

        @Test
        @DisplayName("포트폴리오 ETF 추가 - 존재하지 않는 ETF는 예외 발생")
        void addEtfToPortfolio_EtfNotFound_ThrowsException() {
            // given
            PortfolioEtfAddRequest request = PortfolioEtfAddRequest.builder()
                    .etfId(999L)
                    .weightPct(BigDecimal.valueOf(30))
                    .build();

            given(portfolioRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(testPortfolio));
            given(etfRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> portfolioService.addEtfToPortfolio(1L, 1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.ETF_NOT_FOUND);
        }

        @Test
        @DisplayName("포트폴리오 ETF 추가 - 중복 ETF는 예외 발생")
        void addEtfToPortfolio_DuplicateEtf_ThrowsException() {
            // given
            PortfolioEtfAddRequest request = PortfolioEtfAddRequest.builder()
                    .etfId(100L)
                    .weightPct(BigDecimal.valueOf(30))
                    .build();

            given(portfolioRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(testPortfolio));
            given(etfRepository.findById(100L)).willReturn(Optional.of(testEtf1));
            given(portfolioEtfRepository.findByPortfolioIdAndEtfId(1L, 100L)).willReturn(Optional.of(testPortfolioEtf));

            // when & then
            assertThatThrownBy(() -> portfolioService.addEtfToPortfolio(1L, 1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    // ========== 포트폴리오 ETF 삭제 테스트 ==========

    @Nested
    @DisplayName("포트폴리오 ETF 삭제 테스트")
    class RemoveEtfFromPortfolioTest {

        @Test
        @DisplayName("포트폴리오 ETF 삭제 - 정상 삭제")
        void removeEtfFromPortfolio_Success() {
            // given
            given(portfolioRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(testPortfolio));
            given(portfolioEtfRepository.findByPortfolioIdAndEtfId(1L, 100L)).willReturn(Optional.of(testPortfolioEtf));

            // when
            portfolioService.removeEtfFromPortfolio(1L, 1L, 100L);

            // then
            then(portfolioEtfRepository).should().delete(testPortfolioEtf);
        }

        @Test
        @DisplayName("포트폴리오 ETF 삭제 - 존재하지 않는 ETF는 예외 발생")
        void removeEtfFromPortfolio_NotFound_ThrowsException() {
            // given
            given(portfolioRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(testPortfolio));
            given(portfolioEtfRepository.findByPortfolioIdAndEtfId(1L, 999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> portfolioService.removeEtfFromPortfolio(1L, 1L, 999L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.ETF_NOT_FOUND);
        }
    }

    // ========== 포트폴리오 수익률 조회 테스트 ==========

    @Nested
    @DisplayName("포트폴리오 수익률 조회 테스트")
    class GetPortfolioPerformanceTest {

        @Test
        @DisplayName("포트폴리오 수익률 조회 - 정상 조회")
        void getPortfolioPerformance_Success() {
            // given
            given(portfolioRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(testPortfolio));
            given(etfPriceRepository.findLatestByEtfIds(List.of(100L))).willReturn(List.of(testEtfPrice));

            // when
            PortfolioPerformanceResponse response = portfolioService.getPortfolioPerformance(
                    1L, 1L, LocalDate.now().minusMonths(1), LocalDate.now());

            // then
            assertThat(response).isNotNull();
            assertThat(response.getPortfolioId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("나의 성장 포트폴리오");
            assertThat(response.getTotalInvestment()).isEqualTo(BigDecimal.valueOf(10000000));
        }

        @Test
        @DisplayName("포트폴리오 수익률 조회 - 존재하지 않는 포트폴리오는 예외 발생")
        void getPortfolioPerformance_NotFound_ThrowsException() {
            // given
            given(portfolioRepository.findByIdAndUserId(999L, 1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> portfolioService.getPortfolioPerformance(
                    1L, 999L, LocalDate.now().minusMonths(1), LocalDate.now()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.PORTFOLIO_NOT_FOUND);
        }
    }
}
