package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.dto.EtfCurrentInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Redis 기반 ETF 현재 정보 캐시 구현
 * data-service에서 주기적으로 업데이트하는 캐시 데이터를 조회합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EtfCacheRedisImpl implements EtfCache {
    private final RedisTemplate<String, String> redisTemplate;

    private static final String HASH_PREFIX = "EtfCurrentInfo:";
    private static final String SET_KEY = "EtfCurrentInfo";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public EtfCurrentInfo findByTicker(String ticker) {
        try {
            String key = HASH_PREFIX + ticker;
            var hashOps = redisTemplate.opsForHash();

            // Redis에서 Hash 데이터 모두 조회
            var data = hashOps.entries(key);

            if (data == null || data.isEmpty()) {
                log.debug("[{}] Redis에 캐시 데이터 없음", ticker);
                return null;
            }

            // Hash 필드값들을 EtfCurrentInfo로 변환
            String id = (String) data.get("id");
            String currentPriceStr = (String) data.get("currentPrice");
            String dailyFluctuationStr = (String) data.get("dailyFluctuation");
            String navStr = (String) data.get("nav");
            String volumeStr = (String) data.get("volume");
            String updatedAtStr = (String) data.get("updatedAt");

            if (id == null || currentPriceStr == null) {
                log.warn("[{}] 필수 필드 부족", ticker);
                return null;
            }

            EtfCurrentInfo info = EtfCurrentInfo.of(
                id,
                id,  // name은 ID와 동일 (선택사항)
                new BigDecimal(currentPriceStr),
                new BigDecimal(dailyFluctuationStr != null ? dailyFluctuationStr : "0"),
                Long.parseLong(volumeStr != null ? volumeStr : "0"),
                new BigDecimal(navStr != null ? navStr : "0")
            );

            // updatedAt 설정
            if (updatedAtStr != null) {
                try {
                    LocalDateTime updatedAt = LocalDateTime.parse(updatedAtStr, DATE_TIME_FORMATTER);
                    info.setUpdatedAt(updatedAt);
                } catch (Exception e) {
                    log.debug("[{}] updatedAt 파싱 실패: {}", ticker, updatedAtStr);
                }
            }

            return info;
        } catch (Exception e) {
            log.error("[{}] Redis 조회 실패: {}", ticker, e.getMessage());
            return null;
        }
    }

    @Override
    public List<EtfCurrentInfo> getTopTenEtfsAndSortedByVolume() {
        try {
            // EtfCurrentInfo set에서 모든 ticker 조회
            Set<String> tickers = redisTemplate.opsForSet().members(SET_KEY);

            if (tickers == null || tickers.isEmpty()) {
                log.debug("Redis에 ETF 캐시가 없음");
                return List.of();
            }

            // 각 ticker별로 EtfCurrentInfo 조회
            return tickers.stream()
                .map(this::findByTicker)
                .filter(etf -> etf != null && etf.volume() > 0)  // null 체크 및 거래량 있는 것만
                .sorted(Comparator.comparing(EtfCurrentInfo::volume).reversed())
                .limit(10)
                .toList();

        } catch (Exception e) {
            log.error("Top 10 ETF 조회 실패: {}", e.getMessage());
            return List.of();
        }
    }
}
