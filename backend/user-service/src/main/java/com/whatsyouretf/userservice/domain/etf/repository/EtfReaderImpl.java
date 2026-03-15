package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.dto.EtfCurrentInfo;
import com.whatsyouretf.userservice.domain.etf.dto.EtfSummary;
import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.etf.repository.mock.EtfMockRepository;
import com.whatsyouretf.userservice.domain.etf.service.EtfQuery;
import com.whatsyouretf.userservice.domain.etf.service.EtfReader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EtfReaderImpl implements EtfReader {
    private final EtfMockRepository etfMockRepository;
    private final EtfCache etfCache;
    private final EtfRepository etfRepository;

    @Override
    public Etf read(String ticker) {
        return etfMockRepository.findByTicker(ticker);
    }

    @Override
    public EtfCurrentInfo getInfo(String ticker) {
        return etfCache.findByTicker(ticker);
    }

    @Override
    public Page<EtfSummary> readEtfList(EtfQuery query, Pageable pageable) {
        return etfMockRepository.findEtfList(pageable);
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
}
