package com.whatsyouretf.userservice.domain.company.repository;
import com.whatsyouretf.userservice.domain.company.service.StockCache;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class MockStockCacheImpl implements StockCache {

    private final Map<String, StockInfo> stockMap = new HashMap<>();

    public MockStockCacheImpl() {
        init();
    }

    private void init() {
        stockMap.put("005930", new StockInfo(
            "005930",
            "삼성전자",
            new BigDecimal("450000000000000"),
            new BigDecimal("75000"),
            new BigDecimal("1.25"),
            "글로벌 반도체 및 전자제품을 생산하는 대한민국 대표 IT 기업으로, 메모리 반도체, 스마트폰, 디스플레이 등 다양한 사업을 영위하고 있습니다."
        ));

        stockMap.put("000660", new StockInfo(
            "000660",
            "SK하이닉스",
            new BigDecimal("150000000000000"),
            new BigDecimal("210000"),
            new BigDecimal("-0.85"),
            "DRAM과 NAND 플래시 메모리 중심의 글로벌 반도체 기업으로, AI 및 데이터센터 시장 성장의 핵심 수혜 기업입니다."
        ));

        stockMap.put("373220", new StockInfo(
            "373220",
            "LG에너지솔루션",
            new BigDecimal("120000000000000"),
            new BigDecimal("400000"),
            new BigDecimal("0.55"),
            "전기차 배터리 및 에너지 저장장치(ESS)를 생산하는 기업으로 글로벌 배터리 시장을 선도하고 있습니다."
        ));

        stockMap.put("207940", new StockInfo(
            "207940",
            "삼성바이오로직스",
            new BigDecimal("80000000000000"),
            new BigDecimal("1100000"),
            new BigDecimal("0.30"),
            "바이오 의약품 위탁생산(CMO) 기업으로 글로벌 제약사와 협업하며 바이오 산업에서 높은 성장성을 보유하고 있습니다."
        ));

        stockMap.put("005380", new StockInfo(
            "005380",
            "현대차",
            new BigDecimal("60000000000000"),
            new BigDecimal("250000"),
            new BigDecimal("-1.10"),
            "자동차 제조 및 친환경 모빌리티 사업을 영위하며 전기차, 수소차 등 미래 자동차 산업을 선도하는 기업입니다."
        ));
    }

    public StockInfo get(String ticker) {
        return stockMap.get(ticker);
    }
}