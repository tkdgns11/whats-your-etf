package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.dto.EtfCurrentInfo;

public interface EtfCache {
    EtfCurrentInfo findByTicker(String ticker);
}
