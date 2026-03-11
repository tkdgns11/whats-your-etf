package com.whatsyouretf.userservice.domain.etf.service;

import com.whatsyouretf.userservice.domain.etf.entity.EtfPrice;
import org.springframework.data.domain.Page;

import java.awt.print.Pageable;
import java.time.LocalDate;

public interface EtfPriceReader {
    Page<EtfPrice> readPrices(Long etfId, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
