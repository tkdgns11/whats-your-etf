package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.dto.EtfSummary;
import com.whatsyouretf.userservice.domain.etf.service.EtfQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EtfQueryDslReader {
    Page<EtfSummary> readEtfList(EtfQuery query, Pageable pageable);
}
