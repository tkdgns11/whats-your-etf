package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.EtfPrice;
import com.whatsyouretf.userservice.domain.etf.service.EtfPriceReader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class EtfPriceReaderImpl implements EtfPriceReader {
    private final EtfPriceRepository etfPriceRepository;

    @Override
    public Page<EtfPrice> readPrices(String ticker, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return etfPriceRepository.findByEtfTickerAndDateRange(ticker, startDate, endDate, pageable);
    }
}
