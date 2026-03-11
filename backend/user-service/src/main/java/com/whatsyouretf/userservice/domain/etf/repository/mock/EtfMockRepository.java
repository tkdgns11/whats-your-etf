package com.whatsyouretf.userservice.domain.etf.repository.mock;

import com.whatsyouretf.userservice.domain.etf.dto.EtfSummary;
import com.whatsyouretf.userservice.domain.etf.dto.RiskType;
import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.etf.entity.Fundamental;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class EtfMockRepository {
        private final Map<String, Etf> db = Map.ofEntries(
                // 기존 데이터 3개
                Map.entry("069500", new Etf(1L, "069500", "KODEX 200", "시장 대표", null, "래롬", false, false, false, BigDecimal.valueOf(2.5), BigDecimal.valueOf(1241251255L), 12045210521050L, BigDecimal.valueOf(2.5), "3개월", RiskType.STABLE, LocalDate.of(2024,12,21), null, Fundamental.calculateFundamental(22.7, 1.99), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("102780", new Etf(2L, "102780", "KODEX 삼성그룹", "시장 대표", null, "래롬", false, false, false, BigDecimal.valueOf(2.5), BigDecimal.valueOf(1241251255L), 12045210521050L, BigDecimal.valueOf(2.5), "3개월", RiskType.STABLE, LocalDate.of(2024,12,21), null, Fundamental.calculateFundamental(32.36, 1.92), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("091160", new Etf(3L, "091160", "KODEX 반도체", "테마형", "반도체", "래롬", false, false, false, BigDecimal.valueOf(2.5), BigDecimal.valueOf(1241251255L), 12045210521050L, BigDecimal.valueOf(2.5), "3개월", RiskType.STABLE, LocalDate.of(2024,12,21), null, Fundamental.calculateFundamental(37.92, 5.24), true, LocalDateTime.now(), LocalDateTime.now())),

                // 추가 데이터 42개 (국내 시총 상위 및 주요 테마 ETF 기준)
                Map.entry("133690", new Etf(4L, "133690", "TIGER 미국나스닥100", "해외주식", "나스닥", "미래에셋", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.AGGRESSIVE, LocalDate.now(), null, Fundamental.calculateFundamental(35.2, 5.1), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("360750", new Etf(5L, "360750", "TIGER 미국S&P500", "해외주식", "S&P500", "미래에셋", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.STABLE, LocalDate.now(), null, Fundamental.calculateFundamental(24.8, 4.3), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("122630", new Etf(6L, "122630", "KODEX 레버리지", "파생형", "레버리지", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.AGGRESSIVE, LocalDate.now(), null, Fundamental.calculateFundamental(15.2, 1.1), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("252670", new Etf(7L, "252670", "KODEX 200선물인버스2X", "파생형", "인버스", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.AGGRESSIVE, LocalDate.now(), null, Fundamental.calculateFundamental(0.0, 0.0), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("305720", new Etf(8L, "305720", "KODEX 2차전지산업", "테마형", "2차전지", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.STABLE, LocalDate.now(), null, Fundamental.calculateFundamental(45.5, 6.2), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("305080", new Etf(9L, "305080", "TIGER 미국배당다우존스", "해외주식", "배당", "미래에셋", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.STABLE, LocalDate.now(), null, Fundamental.calculateFundamental(18.4, 2.5), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("379800", new Etf(10L, "379800", "KODEX 미국S&P500TR", "해외주식", "S&P500", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.STABLE, LocalDate.now(), null, Fundamental.calculateFundamental(24.8, 4.3), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("153130", new Etf(11L, "153130", "KODEX 단기채권", "국내채권", "단기채", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.STABLE, LocalDate.now(), null, Fundamental.calculateFundamental(0.0, 0.0), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("273130", new Etf(12L, "273130", "KODEX 종합채권(AA-이상)액티브", "국내채권", "종합채", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.STABLE, LocalDate.now(), null, Fundamental.calculateFundamental(0.0, 0.0), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("314255", new Etf(13L, "314250", "KODEX 미국나스닥100TR", "해외주식", "나스닥", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.AGGRESSIVE, LocalDate.now(), null, Fundamental.calculateFundamental(35.2, 5.1), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("305540", new Etf(14L, "305540", "TIGER 2차전지테마", "테마형", "2차전지", "미래에셋", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.STABLE, LocalDate.now(), null, Fundamental.calculateFundamental(42.1, 5.8), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("371460", new Etf(15L, "371460", "TIGER 차이나전기차SOLACTIVE", "해외주식", "전기차", "미래에셋", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.AGGRESSIVE, LocalDate.now(), null, Fundamental.calculateFundamental(22.5, 3.1), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("069600", new Etf(16L, "069600", "KOSEF 200", "시장 대표", null, "키움", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.STABLE, LocalDate.now(), null, Fundamental.calculateFundamental(22.7, 1.99), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("229200", new Etf(17L, "229200", "KODEX 코스닥150", "시장 대표", "코스닥", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.STABLE, LocalDate.now(), null, Fundamental.calculateFundamental(38.4, 2.5), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("233740", new Etf(18L, "233740", "KODEX 코스닥150레버리지", "파생형", "레버리지", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.AGGRESSIVE, LocalDate.now(), null, Fundamental.calculateFundamental(38.4, 2.5), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("251340", new Etf(19L, "251340", "KODEX 코스닥150선물인버스", "파생형", "인버스", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.AGGRESSIVE, LocalDate.now(), null, Fundamental.calculateFundamental(0.0, 0.0), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("102110", new Etf(20L, "102110", "TIGER 200", "시장 대표", null, "미래에셋", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.STABLE, LocalDate.now(), null, Fundamental.calculateFundamental(22.7, 1.99), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("244580", new Etf(21L, "244580", "KODEX 바이오", "테마형", "바이오", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.AGGRESSIVE, LocalDate.now(), null, Fundamental.calculateFundamental(65.2, 4.8), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("261220", new Etf(22L, "261220", "KODEX WTI원유선물(H)", "원자재", "원유", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.AGGRESSIVE, LocalDate.now(), null, Fundamental.calculateFundamental(0.0, 0.0), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("132030", new Etf(23L, "132030", "KODEX 골드선물(H)", "원자재", "금", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.STABLE, LocalDate.now(), null, Fundamental.calculateFundamental(0.0, 0.0), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("381170", new Etf(24L, "381170", "TIGER 미국테크TOP10 INDXX", "해외주식", "빅테크", "미래에셋", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.AGGRESSIVE, LocalDate.now(), null, Fundamental.calculateFundamental(40.5, 8.2), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("381180", new Etf(25L, "381180", "TIGER 미국필라델피아반도체나스닥", "해외주식", "반도체", "미래에셋", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.AGGRESSIVE, LocalDate.now(), null, Fundamental.calculateFundamental(42.1, 7.5), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("418120", new Etf(26L, "418120", "KODEX 자동차", "테마형", "자동차", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.STABLE, LocalDate.now(), null, Fundamental.calculateFundamental(8.5, 0.8), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("091170", new Etf(27L, "091170", "KODEX 은행", "테마형", "금융", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.STABLE, LocalDate.now(), null, Fundamental.calculateFundamental(5.2, 0.4), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("117680", new Etf(28L, "117680", "KODEX 배당성장", "스마트베타", "배당", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.STABLE, LocalDate.now(), null, Fundamental.calculateFundamental(12.4, 1.1), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("102970", new Etf(29L, "102970", "KODEX 증권", "테마형", "금융", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.STABLE, LocalDate.now(), null, Fundamental.calculateFundamental(7.8, 0.6), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("117700", new Etf(30L, "117700", "KODEX 건설", "테마형", "건설", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.STABLE, LocalDate.now(), null, Fundamental.calculateFundamental(10.2, 0.7), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("102960", new Etf(31L, "102960", "KODEX 조선", "테마형", "조선", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.STABLE, LocalDate.now(), null, Fundamental.calculateFundamental(15.4, 1.5), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("226490", new Etf(32L, "226490", "KODEX 코스피", "시장 대표", "코스피", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.STABLE, LocalDate.now(), null, Fundamental.calculateFundamental(20.5, 1.8), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("226980", new Etf(33L, "226980", "KODEX 200 중소형", "시장 대표", "중소형", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.STABLE, LocalDate.now(), null, Fundamental.calculateFundamental(18.2, 1.5), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("266410", new Etf(34L, "266410", "KODEX 배당가치", "스마트베타", "배당", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.STABLE, LocalDate.now(), null, Fundamental.calculateFundamental(10.5, 0.9), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("278530", new Etf(35L, "278530", "KODEX 200TR", "시장 대표", "TR", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.STABLE, LocalDate.now(), null, Fundamental.calculateFundamental(22.7, 1.99), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("280920", new Etf(36L, "280920", "KODEX K-뉴딜(디지털)", "테마형", "디지털", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.STABLE, LocalDate.now(), null, Fundamental.calculateFundamental(30.1, 3.2), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("280933", new Etf(37L, "280930", "KODEX K-뉴딜(바이오)", "테마형", "바이오", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.STABLE, LocalDate.now(), null, Fundamental.calculateFundamental(55.2, 4.5), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("280940", new Etf(38L, "280940", "KODEX K-뉴딜(2차전지)", "테마형", "2차전지", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.STABLE, LocalDate.now(), null, Fundamental.calculateFundamental(40.5, 5.5), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("433330", new Etf(39L, "433330", "KODEX K-메타버스액티브", "액티브형", "메타버스", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.AGGRESSIVE, LocalDate.now(), null, Fundamental.calculateFundamental(45.2, 4.8), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("387270", new Etf(40L, "387270", "KBSTAR ESG사회책임투자", "테마형", "ESG", "KB", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.STABLE, LocalDate.now(), null, Fundamental.calculateFundamental(15.4, 1.4), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("292150", new Etf(41L, "292150", "TIGER TOP10", "테마형", "우량주", "미래에셋", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.STABLE, LocalDate.now(), null, Fundamental.calculateFundamental(25.1, 2.8), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("394670", new Etf(42L, "394670", "TIGER 글로벌리튬&2차전지SOLACTIVE", "해외주식", "2차전지", "미래에셋", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.AGGRESSIVE, LocalDate.now(), null, Fundamental.calculateFundamental(35.2, 4.1), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("161510", new Etf(43L, "161510", "ARIRANG 고배당주", "스마트베타", "배당", "한화", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.STABLE, LocalDate.now(), null, Fundamental.calculateFundamental(8.5, 0.7), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("280930", new Etf(44L, "280930", "KODEX K-뉴딜(인터넷)", "테마형", "인터넷", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.STABLE, LocalDate.now(), null, Fundamental.calculateFundamental(32.4, 3.5), true, LocalDateTime.now(), LocalDateTime.now())),
                Map.entry("314250", new Etf(45L, "314250", "KODEX 미국나스닥100레버리지(합성 H)", "파생형", "레버리지", "삼성", false, false, false, BigDecimal.valueOf(1.5), BigDecimal.valueOf(9999L), 500000000L, BigDecimal.valueOf(1.0), "3개월", RiskType.AGGRESSIVE, LocalDate.now(), null, Fundamental.calculateFundamental(35.2, 5.1), true, LocalDateTime.now(), LocalDateTime.now()))
        );

        public Etf findByTicker(String ticker) {
                return db.get(ticker);
        }

        public Page<EtfSummary> findEtfList(Pageable pageable) {
                List<EtfSummary> pagedEtfs = db.values().stream()
                        .sorted(Comparator.comparing(Etf::getId))
                        .skip(pageable.getOffset()) // OFFSET: 앞에 있는 데이터를 건너뜀
                        .limit(pageable.getPageSize()) // LIMIT: 사이즈만큼만 가져옴
                        .map(etf ->
                                new EtfSummary(etf.getId(), etf.getStockCode(), etf.getName(), Boolean.FALSE, etf.getRiskType().getTypeName()))
                        .toList();

                return new PageImpl<>(pagedEtfs, pageable, db.size());
        }
}

