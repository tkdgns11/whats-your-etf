package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.common.config.RabbitMQConfig;
import com.whatsyouretf.userservice.domain.etf.dto.EtfCurrentInfo;
import com.whatsyouretf.userservice.domain.etf.dto.EtfSummary;
import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.etf.service.EtfQuery;
import com.whatsyouretf.userservice.domain.etf.service.EtfReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EtfReaderImpl implements EtfReader {
    private final EtfCache etfCache;
    private final EtfRepository etfRepository;
    private final EtfQueryDslReader etfQueryDslReader;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public Etf read(String ticker) {
        return etfRepository.findByStockCode(ticker).orElseThrow(() -> new BusinessException(ErrorCode.ETF_NOT_FOUND));
    }

    @Override
    public EtfCurrentInfo getInfo(String ticker) {
        EtfCurrentInfo info = etfCache.findByTicker(ticker);
        if (info == null) {
            log.debug("[{}] 캐시 miss → 전체 캐시 갱신 요청", ticker);
            try {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE_NAME,
                        RabbitMQConfig.QUEUE_CACHE_ETF_SYNC,
                        Map.of("action", "sync_all")
                );
            } catch (Exception e) {
                log.warn("[{}] 캐시 갱신 MQ 발행 실패: {}", ticker, e.getMessage());
            }
        }
        return info;
    }

    @Override
    public Page<EtfSummary> readEtfList(EtfQuery query, Pageable pageable) {
        return etfQueryDslReader.readEtfList(query, pageable);
    }

    @Override
    public List<EtfSummary> readAllEtfList(EtfQuery query) {
        return etfQueryDslReader.readAllEtfList(query);
    }

    @Override
    public Map<String, Etf> getValidEtfs(List<String> tickers) {
            return etfRepository.findEtfsByStockCodeInTickers(tickers).stream()
                    .collect(Collectors.toMap(Etf::getStockCode, etf -> etf));
    }

    @Override
    public Map<String, EtfCurrentInfo> getInfosMap(Set<String> tickers) {
        return tickers.stream()
                .map(this::getInfo)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        EtfCurrentInfo::ticker,
                        Function.identity()
                ));
    }

    @Override
    public List<EtfCurrentInfo> getTopTenEtfs() {
        return etfCache.getTopTenEtfsAndSortedByVolume();
    }
}
