# ETF 클러스터 API

> 화면: `EtfDetailScreen.kt`, `ClusterTab.kt`, `SectorBottomSheet.kt`

---

## GET /api/v1/etf/{ticker}

ETF 상세 조회 (섹터 클러스터 + 영향력 종목 포함)

### Request

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| ticker | String | Y | ETF 티커 (예: "091160") |

### Response

```json
{
  "success": true,
  "data": {
    "ticker": "091160",
    "name": "KODEX 반도체",
    "englishName": "KODEX Semiconductor",
    "riskLevel": 4,
    "currentPrice": 42350,
    "changeAmount": 520,
    "changeRate": 1.24,
    "iNav": 42380,
    "iNavChangeAmount": 550,
    "iNavChangeRate": 1.31,
    "returnRate1M": 5.67,
    "volume": 1523000,
    "manager": "삼성자산운용",
    "volatility": "높음",
    "expenseRatio": 0.45,
    "netAsset": 1250000000000,
    "listedDate": "2006-06-27",
    "sectors": [
      {
        "name": "반도체",
        "percentage": 45.2,
        "stocks": [
          {"ticker": "005930", "name": "삼성전자", "percentage": 28.5},
          {"ticker": "000660", "name": "SK하이닉스", "percentage": 16.7}
        ],
        "aiAnalysis": "반도체 섹터는 AI 수요 증가로 인해 성장세가 지속될 것으로 예상됩니다."
      },
      {
        "name": "전자장비",
        "percentage": 22.8,
        "stocks": [
          {"ticker": "006400", "name": "삼성SDI", "percentage": 12.3},
          {"ticker": "051910", "name": "LG화학", "percentage": 10.5}
        ],
        "aiAnalysis": "전자장비 섹터는 전기차 배터리 수요와 연동되어 있습니다."
      },
      {
        "name": "소프트웨어",
        "percentage": 15.4,
        "stocks": [
          {"ticker": "035720", "name": "카카오", "percentage": 8.2},
          {"ticker": "035420", "name": "NAVER", "percentage": 7.2}
        ],
        "aiAnalysis": null
      }
    ],
    "influentialStocks": [
      {
        "ticker": "005930",
        "name": "삼성전자",
        "weight": 28.5,
        "currentPrice": 72300,
        "changeRate": 1.85
      },
      {
        "ticker": "000660",
        "name": "SK하이닉스",
        "weight": 16.7,
        "currentPrice": 178500,
        "changeRate": 2.34
      },
      {
        "ticker": "006400",
        "name": "삼성SDI",
        "weight": 12.3,
        "currentPrice": 412000,
        "changeRate": -0.72
      }
    ]
  }
}
```

### 화면 매핑

#### ETF 헤더 (ClusterTab)
| API 필드 | 화면 표시 |
|----------|----------|
| riskLevel | 위험등급 배지 (1:안정형 ~ 5:공격투자형) |
| ticker | 중앙 대형 텍스트 |
| englishName | 영문명 (작은 텍스트) |

#### 클러스터 버블 차트
| API 필드 | 화면 표시 |
|----------|----------|
| sectors[].name | 버블 내 섹터명 |
| sectors[].percentage | 버블 내 비중 % |
| sectors[].name | 아이콘 매핑 (반도체→Memory, 금융→AccountBalance 등) |

버블 크기: `percentage` 값에 비례

#### 가격/거래량 카드
| API 필드 | 화면 표시 |
|----------|----------|
| currentPrice | 현재가 (천단위 콤마) |
| volume | 거래량 (억/만 단위 변환) |

#### 영향력 종목 섹션
| API 필드 | 화면 표시 |
|----------|----------|
| influentialStocks[].name | 종목명 (아바타 첫글자) |
| influentialStocks[].weight | 비중 % |
| influentialStocks[].currentPrice | 현재가 |
| influentialStocks[].changeRate | 등락률 (초록/빨강) |

#### 섹터 바텀시트 (SectorBottomSheet)
| API 필드 | 화면 표시 |
|----------|----------|
| sectors[].name | 섹터명 |
| sectors[].percentage | 섹터 비중 |
| sectors[].stocks[].name | 종목명 |
| sectors[].stocks[].percentage | 종목 비중 |
| sectors[].aiAnalysis | AI 분석 텍스트 (있으면 표시) |

---

## 위험등급 매핑

| riskLevel | 표시 텍스트 | 색상 |
|-----------|------------|------|
| 1 | 안정형 | Blue |
| 2 | 안정추구형 | Teal |
| 3 | 위험중립형 | Yellow |
| 4 | 적극투자형 | Orange |
| 5 | 공격투자형 | Red |

---

## 섹터 아이콘 매핑

| 섹터명 | 아이콘 |
|--------|--------|
| 반도체 | Memory |
| 금융 | AccountBalance |
| 헬스케어/바이오 | LocalHospital |
| 에너지 | Bolt |
| IT/테크 | Computer |
| 소비재 | ShoppingCart |
| 산업재 | Factory |
| 통신 | CellTower |
| 유틸리티 | WaterDrop |
| 부동산 | Home |
| 기타 | Category |

---

## 백엔드 구현 상태

- [x] `EtfController.java` 구현 완료
- [x] `EtfService.java` 구현 완료
- [x] `EtfDetailResponse.java` DTO 구현 완료

### 구현된 조회 로직

```
etf (기본 정보)
    ↓
etf_prices (최신 시세)
    ↓
etf_sector_cluster (섹터 클러스터 - 버블 차트)
    ↓
etf_sector_ai_history (섹터 AI 분석)
    ↓
etf_stock_composition → stock → company_info (구성 종목)
```

- 섹터별 상위 5개 종목 반환
- 영향력 종목 상위 5개 반환
- 위험등급: HIGH_RISK → 5, STABLE → 1 매핑
- 변동성: 1년 변동률 기준 문자열 변환

## 안드로이드 구현 상태

- [x] `EtfApiService.kt` 존재
- [x] `EtfDetailResponse.kt` DTO 존재
- [ ] API 경로 수정 필요 (`api/etf/{ticker}` → `/api/v1/etf/{ticker}`)
