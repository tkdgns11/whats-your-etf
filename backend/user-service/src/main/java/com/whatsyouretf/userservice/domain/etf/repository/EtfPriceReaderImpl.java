package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.EtfPrice;
import com.whatsyouretf.userservice.domain.etf.service.EtfPriceReader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.awt.print.Pageable;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class EtfPriceReaderImpl implements EtfPriceReader {
//    private final EtfPriceJsonLoader etfPriceJsonLoader;

    @Override
    public Page<EtfPrice> readPrices(Long etfId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
//        List<EtfPriceInfo> etfPriceInfoList = etfPriceJsonLoader.findAll();
//        return etfPriceInfoList.stream()
//            .collect();
        return null;
    }
}
