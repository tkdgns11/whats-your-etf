package com.whatsyouretf.userservice.domain.company.service;

import com.whatsyouretf.userservice.domain.company.repository.StockInfo;

public interface StockCache {
    StockInfo get(String ticker);
}
