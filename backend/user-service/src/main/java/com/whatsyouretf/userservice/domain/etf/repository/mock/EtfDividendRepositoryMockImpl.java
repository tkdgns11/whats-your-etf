package com.whatsyouretf.userservice.domain.etf.repository.mock;

import com.whatsyouretf.userservice.domain.etf.repository.EtfDividendRepository;
import com.whatsyouretf.userservice.domain.etf.service.EtfDividendsData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class EtfDividendRepositoryMockImpl implements EtfDividendRepository {
    @Override
    public List<EtfDividendsData> getDividends(String ticker) {
        return List.of(new EtfDividendsData(LocalDate.of(2026, 1, 23), BigDecimal.valueOf(1010231L))
        ,new EtfDividendsData(LocalDate.of(2024, 1, 23), BigDecimal.valueOf(1010231L))
        ,new EtfDividendsData(LocalDate.of(2022, 1, 23), BigDecimal.valueOf(1010231L)));
    }
}
