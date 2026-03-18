package com.whatsyouretf.userservice.domain.etf.repository.mock;

import com.whatsyouretf.userservice.domain.etf.dto.EtfCurrentInfo;
import com.whatsyouretf.userservice.domain.etf.repository.EtfCache;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class EtfCacheImpl implements EtfCache {
    private final Map<String, EtfCurrentInfo> cache = Map.ofEntries(

            Map.entry("069500", EtfCurrentInfo.update("069500", "KODEX 200", BigDecimal.valueOf(83000L), BigDecimal.valueOf(82000L), 17369770L, BigDecimal.valueOf(83993L))),
            Map.entry("102780", EtfCurrentInfo.update("102780", "KODEX 삼성그룹", BigDecimal.valueOf(18410L), BigDecimal.valueOf(17000L), 963401L, BigDecimal.valueOf(18364L))),
            Map.entry("091160", EtfCurrentInfo.update("091160", "KODEX 반도체", BigDecimal.valueOf(98400L), BigDecimal.valueOf(99300L), 4766324L, BigDecimal.valueOf(98939L))),

            Map.entry("133690", EtfCurrentInfo.update("133690", "TIGER 나스닥100", BigDecimal.valueOf(95000L), BigDecimal.valueOf(94500L), 1250000L, BigDecimal.valueOf(95100L))),
            Map.entry("360750", EtfCurrentInfo.update("360750", "TIGER 미국테크TOP10", BigDecimal.valueOf(14200L), BigDecimal.valueOf(14150L), 850000L, BigDecimal.valueOf(14220L))),
            Map.entry("122630", EtfCurrentInfo.update("122630", "KODEX 레버리지", BigDecimal.valueOf(21000L), BigDecimal.valueOf(20500L), 5500000L, BigDecimal.valueOf(20950L))),
            Map.entry("252670", EtfCurrentInfo.update("252670", "KODEX 200선물인버스2X", BigDecimal.valueOf(2500L), BigDecimal.valueOf(2550L), 12000000L, BigDecimal.valueOf(2510L))),
            Map.entry("305720", EtfCurrentInfo.update("305720", "TIGER 미국S&P500", BigDecimal.valueOf(31000L), BigDecimal.valueOf(31500L), 940000L, BigDecimal.valueOf(31100L))),
            Map.entry("305080", EtfCurrentInfo.update("305080", "TIGER 미국나스닥100", BigDecimal.valueOf(11500L), BigDecimal.valueOf(11400L), 2100000L, BigDecimal.valueOf(11520L))),
            Map.entry("379800", EtfCurrentInfo.update("379800", "KODEX 미국배당성장", BigDecimal.valueOf(15300L), BigDecimal.valueOf(15200L), 680000L, BigDecimal.valueOf(15350L))),
            Map.entry("153130", EtfCurrentInfo.update("153130", "KODEX 단기채권", BigDecimal.valueOf(105000L), BigDecimal.valueOf(105000L), 150000L, BigDecimal.valueOf(105010L))),
            Map.entry("273130", EtfCurrentInfo.update("273130", "KODEX 종합채권", BigDecimal.valueOf(102000L), BigDecimal.valueOf(101900L), 80000L, BigDecimal.valueOf(102050L))),
            Map.entry("314255", EtfCurrentInfo.update("314255", "KODEX ESG종합", BigDecimal.valueOf(88000L), BigDecimal.valueOf(87000L), 450000L, BigDecimal.valueOf(88100L))),
            Map.entry("305540", EtfCurrentInfo.update("305540", "TIGER 2차전지테마", BigDecimal.valueOf(42000L), BigDecimal.valueOf(43000L), 1150000L, BigDecimal.valueOf(42150L))),
            Map.entry("371460", EtfCurrentInfo.update("371460", "KODEX 2차전지산업", BigDecimal.valueOf(8500L), BigDecimal.valueOf(8400L), 3200000L, BigDecimal.valueOf(8520L))),
            Map.entry("069600", EtfCurrentInfo.update("069600", "KODEX 200TR", BigDecimal.valueOf(35000L), BigDecimal.valueOf(34800L), 120000L, BigDecimal.valueOf(35050L))),
            Map.entry("229200", EtfCurrentInfo.update("229200", "KODEX 코스닥150", BigDecimal.valueOf(15000L), BigDecimal.valueOf(15000L), 2300000L, BigDecimal.valueOf(15050L))),
            Map.entry("233740", EtfCurrentInfo.update("233740", "KODEX 코스닥150레버리지", BigDecimal.valueOf(9000L), BigDecimal.valueOf(8800L), 4500000L, BigDecimal.valueOf(9020L))),
            Map.entry("251340", EtfCurrentInfo.update("251340", "KODEX 코스닥150선물인버스", BigDecimal.valueOf(4500L), BigDecimal.valueOf(4600L), 3800000L, BigDecimal.valueOf(4510L))),
            Map.entry("102110", EtfCurrentInfo.update("102110", "TIGER 200", BigDecimal.valueOf(36000L), BigDecimal.valueOf(35500L), 850000L, BigDecimal.valueOf(36100L))),
            Map.entry("244580", EtfCurrentInfo.update("244580", "KODEX 바이오", BigDecimal.valueOf(11000L), BigDecimal.valueOf(10800L), 950000L, BigDecimal.valueOf(11050L))),
            Map.entry("261220", EtfCurrentInfo.update("261220", "TIGER 미국나스닥", BigDecimal.valueOf(16000L), BigDecimal.valueOf(15500L), 1400000L, BigDecimal.valueOf(16050L))),
            Map.entry("132030", EtfCurrentInfo.update("132030", "KODEX 골드선물", BigDecimal.valueOf(14500L), BigDecimal.valueOf(14600L), 750000L, BigDecimal.valueOf(14480L))),
            Map.entry("381170", EtfCurrentInfo.update("381170", "KODEX K-메타버스", BigDecimal.valueOf(22000L), BigDecimal.valueOf(21500L), 1800000L, BigDecimal.valueOf(22100L))),
            Map.entry("381180", EtfCurrentInfo.update("381180", "KODEX 게임산업", BigDecimal.valueOf(19500L), BigDecimal.valueOf(19000L), 1600000L, BigDecimal.valueOf(19550L))),
            Map.entry("418120", EtfCurrentInfo.update("418120", "TIGER AI반도체", BigDecimal.valueOf(21000L), BigDecimal.valueOf(20800L), 820000L, BigDecimal.valueOf(21100L))),
            Map.entry("091170", EtfCurrentInfo.update("091170", "KODEX 은행", BigDecimal.valueOf(7500L), BigDecimal.valueOf(7400L), 1500000L, BigDecimal.valueOf(7520L))),
            Map.entry("117680", EtfCurrentInfo.update("117680", "KODEX 보험", BigDecimal.valueOf(13000L), BigDecimal.valueOf(12900L), 420000L, BigDecimal.valueOf(13050L))),
            Map.entry("102970", EtfCurrentInfo.update("102970", "KODEX 증권", BigDecimal.valueOf(6800L), BigDecimal.valueOf(6700L), 910000L, BigDecimal.valueOf(6820L))),
            Map.entry("117700", EtfCurrentInfo.update("117700", "KODEX 건설", BigDecimal.valueOf(4200L), BigDecimal.valueOf(4300L), 2100000L, BigDecimal.valueOf(4210L))),
            Map.entry("102960", EtfCurrentInfo.update("102960", "KODEX 철강", BigDecimal.valueOf(5500L), BigDecimal.valueOf(5600L), 1800000L, BigDecimal.valueOf(5520L))),
            Map.entry("226490", EtfCurrentInfo.update("226490", "KODEX IT", BigDecimal.valueOf(28000L), BigDecimal.valueOf(27800L), 750000L, BigDecimal.valueOf(28050L))),
            Map.entry("226980", EtfCurrentInfo.update("226980", "KODEX 헬스케어", BigDecimal.valueOf(12500L), BigDecimal.valueOf(12400L), 310000L, BigDecimal.valueOf(12550L))),
            Map.entry("266410", EtfCurrentInfo.update("266410", "KODEX 2차전지", BigDecimal.valueOf(9800L), BigDecimal.valueOf(9700L), 620000L, BigDecimal.valueOf(9820L))),
            Map.entry("278530", EtfCurrentInfo.update("278530", "KODEX 자동차", BigDecimal.valueOf(39000L), BigDecimal.valueOf(38500L), 410000L, BigDecimal.valueOf(39100L))),
            Map.entry("280920", EtfCurrentInfo.update("280920", "KODEX 경기소비재", BigDecimal.valueOf(8200L), BigDecimal.valueOf(8100L), 1200000L, BigDecimal.valueOf(8220L))),
            Map.entry("280933", EtfCurrentInfo.update("280933", "KODEX 필수소비재", BigDecimal.valueOf(6500L), BigDecimal.valueOf(6600L), 2500000L, BigDecimal.valueOf(6510L))),
            Map.entry("280940", EtfCurrentInfo.update("280940", "KODEX 에너지", BigDecimal.valueOf(17500L), BigDecimal.valueOf(17800L), 1900000L, BigDecimal.valueOf(17550L))),
            Map.entry("433330", EtfCurrentInfo.update("433330", "TIGER 우주산업", BigDecimal.valueOf(5200L), BigDecimal.valueOf(5100L), 1500000L, BigDecimal.valueOf(5220L))),
            Map.entry("387270", EtfCurrentInfo.update("387270", "KODEX 친환경", BigDecimal.valueOf(11200L), BigDecimal.valueOf(11100L), 380000L, BigDecimal.valueOf(11250L))),
            Map.entry("292150", EtfCurrentInfo.update("292150", "KODEX 배당성장", BigDecimal.valueOf(14800L), BigDecimal.valueOf(14700L), 720000L, BigDecimal.valueOf(14850L))),
            Map.entry("394670", EtfCurrentInfo.update("394670", "TIGER 클라우드컴퓨팅", BigDecimal.valueOf(9500L), BigDecimal.valueOf(9800L), 3100000L, BigDecimal.valueOf(9520L))),
            Map.entry("161510", EtfCurrentInfo.update("161510", "KODEX 고배당", BigDecimal.valueOf(13400L), BigDecimal.valueOf(13300L), 540000L, BigDecimal.valueOf(13450L))),
            Map.entry("280930", EtfCurrentInfo.update("280930", "KODEX 경기민감", BigDecimal.valueOf(7800L), BigDecimal.valueOf(7900L), 1400000L, BigDecimal.valueOf(7820L))),
            Map.entry("314250", EtfCurrentInfo.update("314250", "KODEX 혁신기술", BigDecimal.valueOf(25000L), BigDecimal.valueOf(24500L), 2200000L, BigDecimal.valueOf(25100L)))
    );

    @Override
    public EtfCurrentInfo findByTicker(String ticker) {
        return cache.get(ticker);
    }

    @Override
    public List<EtfCurrentInfo> getTopTenEtfsAndSortedByVolume() {
        return cache.values().stream().sorted(Comparator.comparing(EtfCurrentInfo::volume).reversed())
                .limit(10)
                .toList();
    }
}
