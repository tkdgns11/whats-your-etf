# ETF 클러스터 API

> 화면: `EtfDetailScreen.kt`, `ClusterTab.kt`, `SectorBottomSheet.kt`

---

## 코드값 정의

### 위험등급 (riskLevel)

ETF의 투자 위험도를 1~5 단계로 나타냅니다. 금융투자협회 기준에 따라 분류됩니다.

| riskLevel | 표시 텍스트 | 색상 | 설명 | 투자 성향 |
|-----------|------------|------|------|----------|
| `1` | 안정형 | Blue (#2196F3) | 원금 손실 가능성 매우 낮음 | 예금 대안, 안정 추구 |
| `2` | 안정추구형 | Teal (#009688) | 원금 손실 가능성 낮음 | 채권형, 안정 수익 추구 |
| `3` | 위험중립형 | Yellow (#FFC107) | 원금 손실 가능성 보통 | 혼합형, 균형 투자 |
| `4` | 적극투자형 | Orange (#FF9800) | 원금 손실 가능성 높음 | 주식형, 성장 추구 |
| `5` | 공격투자형 | Red (#F44336) | 원금 손실 가능성 매우 높음 | 레버리지/인버스, 고수익 추구 |

### 변동성 (volatility)

1년 기준 ETF 가격 변동 정도를 나타냅니다.

| volatility | 설명 | 기준 (연간 표준편차) |
|------------|------|---------------------|
| `매우 낮음` | 가격 변동이 거의 없음 | < 5% |
| `낮음` | 가격 변동이 적음 | 5% ~ 10% |
| `보통` | 일반적인 변동 수준 | 10% ~ 20% |
| `높음` | 가격 변동이 큼 | 20% ~ 30% |
| `매우 높음` | 가격 변동이 매우 큼 | > 30% |

### 섹터명 & 아이콘 매핑

ETF 구성종목의 산업 섹터를 분류합니다. 클러스터 버블 차트에서 사용됩니다.

| 섹터명 | 영문명 | Material Icon | 설명 |
|--------|--------|---------------|------|
| 반도체 | Semiconductor | `Memory` | 메모리, 시스템반도체, 장비 |
| 금융 | Finance | `AccountBalance` | 은행, 증권, 보험, 카드 |
| 헬스케어/바이오 | Healthcare | `LocalHospital` | 제약, 바이오, 의료기기 |
| 에너지 | Energy | `Bolt` | 정유, 가스, 신재생에너지 |
| IT/테크 | IT/Tech | `Computer` | 소프트웨어, 인터넷, 플랫폼 |
| 소비재 | Consumer | `ShoppingCart` | 유통, 식품, 의류, 화장품 |
| 산업재 | Industrial | `Factory` | 기계, 조선, 방산, 건설장비 |
| 통신 | Telecom | `CellTower` | 통신사, 미디어, 방송 |
| 유틸리티 | Utility | `WaterDrop` | 전력, 가스, 수도 |
| 부동산 | Real Estate | `Home` | 리츠, 건설, 부동산 개발 |
| 자동차 | Automobile | `DirectionsCar` | 완성차, 부품, 전기차 |
| 화학/소재 | Chemical | `Science` | 정밀화학, 철강, 비철금속 |
| 기타 | Others | `Category` | 분류되지 않은 기타 섹터 |

### 비주식 자산 유형 (assetType)

레버리지/인버스 ETF 등 주식 외 자산을 보유하는 ETF의 구성종목 유형입니다.

| assetType | 설명 | 예시 |
|-----------|------|------|
| `FUTURES` | 선물 | KOSPI200 선물, 코스닥150 선물 |
| `ETF` | 다른 ETF 편입 | KODEX 인버스, TIGER 200 |
| `BOND` | 채권/RP | 채권/RP |
| `CASH` | 현금성 자산 | 현금성 자산 |
| `PREFERRED_STOCK` | 우선주 | 삼성전자우, 현대차2우B |

### 에러 코드

| 코드 | HTTP 상태 | 메시지 | 설명 | 대응 방법 |
|------|----------|--------|------|----------|
| `ETF001` | 404 | ETF를 찾을 수 없습니다 | 존재하지 않는 ticker | 올바른 티커 확인 |
| `ETF002` | 404 | 시세 정보가 없습니다 | ETF 가격 데이터 없음 | 장 마감 후 조회 또는 새로고침 |

---

## GET /api/v1/etfs/{ticker}/clusters

ETF 클러스터 조회 (섹터 클러스터 + 영향력 종목)

> ETF 기본 정보는 `GET /api/v1/etfs/{ticker}`에서 조회

### Request

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| ticker | String | Y | ETF 티커 (예: "091160") |

### Response

```json
{
  "success": true,
  "message": "Success",
  "code": "OK",
  "data": {
    "englishName": "KOSPI 200 Index Tracking Fund",
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
    ],
    "otherCompositions": [
      {
        "assetType": "FUTURES",
        "assetName": "KOSPI200 선물",
        "weight": 91.537
      },
      {
        "assetType": "ETF",
        "assetName": "KODEX 인버스",
        "weight": 8.463
      }
    ]
  },
  "timestamp": "2025-03-10T14:30:00"
}
```

### 화면 매핑

#### 클러스터 버블 차트
| API 필드 | 화면 표시 |
|----------|----------|
| sectors[].name | 버블 내 섹터명 |
| sectors[].percentage | 버블 내 비중 % |
| sectors[].name | 아이콘 매핑 (반도체→Memory, 금융→AccountBalance 등) |

버블 크기: `percentage` 값에 비례

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

#### 비주식 구성종목 섹션 (레버리지/인버스 ETF용)
| API 필드 | 화면 표시 |
|----------|----------|
| otherCompositions[].assetType | 자산 유형 (FUTURES, ETF, BOND 등) |
| otherCompositions[].assetName | 자산명 (예: KOSPI200 선물) |
| otherCompositions[].weight | 비중 % |

> **참고**: 일반 ETF는 `otherCompositions`가 빈 배열입니다. 레버리지/인버스 ETF 등 선물, 채권, 다른 ETF를 보유하는 상품에서만 데이터가 반환됩니다.

---

## 백엔드 구현 상태

- [x] `EtfController.java` 구현 완료
- [x] `EtfService.java` 구현 완료
- [x] `EtfClusterResponse.java` DTO 구현 완료

### 구현된 조회 로직

```
etf (기본 정보 - englishName)
    ↓
etf_sector_cluster (섹터 클러스터 - 버블 차트)
    ↓
etf_sector_ai_history (섹터 AI 분석)
    ↓
etf_stock_composition → stock → company_info (구성 종목)
    ↓
etf_other_composition (비주식 구성종목 - 선물, 채권, ETF 등)
```

- 섹터별 상위 5개 종목 반환
- 영향력 종목 상위 5개 반환
- 비주식 구성종목 (선물, 채권, 현금, 우선주, 다른 ETF) 반환
- 위험등급: AGGRESSIVE → 5, STABLE → 1 매핑
- 변동성: 1년 변동률 기준 문자열 변환

## 안드로이드 구현 상태

- [x] `EtfApiService.kt` 존재
- [x] `EtfDetailResponse.kt` DTO 존재
- [ ] API 경로 수정 필요 → `/api/v1/etfs/{ticker}/clusters`
