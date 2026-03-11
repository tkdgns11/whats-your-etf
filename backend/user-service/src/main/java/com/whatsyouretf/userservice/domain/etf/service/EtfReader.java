package com.whatsyouretf.userservice.domain.etf.service;

import com.whatsyouretf.userservice.domain.etf.dto.EtfCurrentInfo;
import com.whatsyouretf.userservice.domain.etf.entity.Etf;

public interface EtfReader {
    Etf read(String ticker);

    EtfCurrentInfo getInfo(String ticker);
}
