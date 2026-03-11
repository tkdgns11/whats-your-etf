package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.dto.EtfCurrentInfo;
import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.etf.repository.mock.EtfMockRepository;
import com.whatsyouretf.userservice.domain.etf.service.EtfReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EtfReaderImpl implements EtfReader {
    private final EtfMockRepository etfMockRepository;
    private final EtfCache etfCache;

    @Override
    public Etf read(String ticker) {
        return etfMockRepository.findByTicker(ticker);
    }

    @Override
    public EtfCurrentInfo getInfo(String ticker) {
        return etfCache.findByTicker(ticker);
    }
}
