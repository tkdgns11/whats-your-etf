package com.whatsyouretf.userservice.domain.etf.repository.mock;

import com.whatsyouretf.userservice.domain.etf.dto.EtfCurrentInfo;
import com.whatsyouretf.userservice.domain.etf.repository.EtfCache;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class EtfCacheImpl implements EtfCache {
    private final Map<String, EtfCurrentInfo> db = Map.of(
        "069500", new EtfCurrentInfo("069500",
                                    BigDecimal.valueOf(83825L),
                                    BigDecimal.valueOf(82000L),
                                    17369770L,
                                    BigDecimal.valueOf(83993L)),
        "102780", new EtfCurrentInfo("102780",
                                    BigDecimal.valueOf(18410),
                                    BigDecimal.valueOf(17000L),
                                    963401L,
                                    BigDecimal.valueOf(18364L)),
        "091160", new EtfCurrentInfo("091160",
                                    BigDecimal.valueOf(98400L),
                                    BigDecimal.valueOf(99300L),
                                   4766324L,
                                    BigDecimal.valueOf(98939L)));

    @Override
    public EtfCurrentInfo findByTicker(String ticker) {
        return db.get(ticker);
    }
}
