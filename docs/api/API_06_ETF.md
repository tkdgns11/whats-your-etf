# ETF API

## 기본 정보
- Base URL: `/api/v1/etf`
- 인증: 일부 엔드포인트 JWT 필요

---

## 엔드포인트 목록

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/` | ETF 목록 조회 | X |
| GET | `/{etfId}` | ETF 상세 조회 | X |
| GET | `/{etfId}/prices` | ETF 가격 이력 조회 | X |
| GET | `/{etfId}/compositions` | ETF 구성종목 조회 | X |
| GET | `/search` | ETF 검색 | X |
| GET | `/clusters` | ETF 클러스터 목록 조회 | X |
| GET | `/clusters/{clusterId}` | 클러스터별 ETF 조회 | X |
| GET | `/{etfId}/sector-breakdown` | ETF 섹터 분포 조회 | X |
| GET | `/{etfId}/timeline` | ETF 뉴스 타임라인 조회 | X |
| GET | `/recommend` | ETF 추천 | O |

---

## API 상세

### 1. ETF 목록 조회

**Request**
```
GET /api/v1/etf?page=0&size=20&strategyType=INDEX&sortBy=totalAsset
```

| Parameter | Type | 필수 | 설명 |
|-----------|------|------|------|
| page | int | X | 페이지 번호 (기본 0, 최소 0) |
| size | int | X | 페이지 크기 (기본 20, 최소 1, 최대 100) |
| strategyType | string | X | 전략 유형: `MARKET` / `THEME` / `DIVIDEND` / `BOND` / `DERIVATIVE` |
| sector | string | X | 섹터 필터 (group_code, 최대 20자): IT_SEMI, IT_ELEC, BIO 등 22개 |
| riskGrade | string | X | 위험등급: `HIGH_RISK` / `MODERATE` / `STABLE` |
| sortBy | string | X | 정렬 기준: `aum` / `expenseRatio` / `name` / `changeRate` |

**Response**
```json
{
  "success": true,
  "data": {
    "etfs": [
      {
        "id": 1,
        "ticker": "069500",
        "name": "KODEX 200",
        "issuer": "삼성자산운용",
        "strategyType": "INDEX",
        "expenseRatio": 0.15,
        "totalAsset": 5000000000000,
        "listingDate": "2002-10-14",
        "latestPrice": 35420,
        "priceChangeRate": 1.25
      }
    ],
    "page": 0,
    "totalPages": 50,
    "totalElements": 1000
  }
}
```

---

### 2. ETF 상세 조회

**Request**
```
GET /api/v1/etf/1
```

**Response**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "ticker": "069500",
    "name": "KODEX 200",
    "issuer": "삼성자산운용",
    "strategyType": "INDEX",
    "expenseRatio": 0.15,
    "totalAsset": 5000000000000,
    "listingDate": "2002-10-14",
    "latestPrice": 35420,
    "priceChangeRate": 1.25,
    "description": "KOSPI 200 지수를 추종하는 ETF입니다.",
    "benchmark": "KOSPI 200",
    "dividendCycle": "분기",
    "clusterId": 1,
    "clusterName": "대형 성장주"
  }
}
```

---

### 3. ETF 가격 이력 조회

**Request**
```
GET /api/v1/etf/1/prices?startDate=2025-01-01&endDate=2025-01-31
```

| Parameter | Type | 필수 | 설명 |
|-----------|------|------|------|
| startDate | string | X | 시작일 (YYYY-MM-DD) |
| endDate | string | X | 종료일 (YYYY-MM-DD) |

**Response**
```json
{
  "success": true,
  "data": {
    "etfId": 1,
    "ticker": "069500",
    "name": "KODEX 200",
    "prices": [
      {
        "date": "2025-01-31",
        "close": 35420,
        "volume": 1500000
      },
      {
        "date": "2025-01-30",
        "close": 35000,
        "volume": 1200000
      }
    ]
  }
}
```

---

### 4. ETF 구성종목 조회

**Request**
```
GET /api/v1/etf/1/compositions?baseDate=2025-01-31
```

| Parameter | Type | 필수 | 설명 |
|-----------|------|------|------|
| baseDate | string | X | 기준일 (YYYY-MM-DD, 기본값: 최신) |

**Response**
```json
{
  "success": true,
  "data": {
    "etfId": 1,
    "ticker": "069500",
    "name": "KODEX 200",
    "baseDate": "2025-01-31",
    "compositions": [
      {
        "componentTicker": "005930",
        "componentName": "삼성전자",
        "weightPct": 25.5,
        "industry": "전자"
      },
      {
        "componentTicker": "000660",
        "componentName": "SK하이닉스",
        "weightPct": 8.2,
        "industry": "반도체"
      }
    ]
  }
}
```

---

### 5. ETF 검색

**Request**
```
GET /api/v1/etf/search?keyword=코덱스&strategyType=INDEX
```

| Parameter | Type | 필수 | 설명 |
|-----------|------|------|------|
| keyword | string | O | 검색어 (ETF명/티커, 2~100자) |
| strategyType | string | X | 전략 유형 필터: `MARKET` / `THEME` / `DIVIDEND` / `BOND` / `DERIVATIVE` |
| issuer | string | X | 운용사 필터 (최대 50자): KODEX, TIGER, KBSTAR 등 |

**Response**
```json
{
  "success": true,
  "data": {
    "etfs": [
      {
        "id": 1,
        "ticker": "069500",
        "name": "KODEX 200",
        "issuer": "삼성자산운용",
        "strategyType": "INDEX"
      }
    ],
    "totalCount": 15
  }
}
```

---

### 6. ETF 클러스터 목록 조회

**Request**
```
GET /api/v1/etf/clusters
```

**Response**
```json
{
  "success": true,
  "data": {
    "clusters": [
      {
        "id": 1,
        "name": "대형 성장주",
        "description": "시가총액 상위 대형주 중심의 성장주 ETF 그룹",
        "etfCount": 25,
        "avgExpenseRatio": 0.18,
        "avgReturn1Y": 12.5
      },
      {
        "id": 2,
        "name": "배당주",
        "description": "고배당 수익을 추구하는 ETF 그룹",
        "etfCount": 18,
        "avgExpenseRatio": 0.25,
        "avgReturn1Y": 8.2
      }
    ]
  }
}
```

---

### 7. 클러스터별 ETF 조회

**Request**
```
GET /api/v1/etf/clusters/1?page=0&size=20
```

| Parameter | Type | 필수 | 설명 |
|-----------|------|------|------|
| page | int | X | 페이지 번호 (기본 0) |
| size | int | X | 페이지 크기 (기본 20) |

**Response**
```json
{
  "success": true,
  "data": {
    "cluster": {
      "id": 1,
      "name": "대형 성장주",
      "description": "시가총액 상위 대형주 중심의 성장주 ETF 그룹"
    },
    "etfs": [
      {
        "id": 1,
        "ticker": "069500",
        "name": "KODEX 200",
        "issuer": "삼성자산운용",
        "expenseRatio": 0.15,
        "totalAsset": 5000000000000
      }
    ],
    "page": 0,
    "totalPages": 2,
    "totalElements": 25
  }
}
```

---

### 8. ETF 섹터 분포 조회
ETF 구성종목의 산업분류(group_code) 기반 섹터 분포

**Request**
```
GET /api/v1/etf/1/sector-breakdown
```

**Response**
```json
{
  "success": true,
  "data": {
    "etfId": 1,
    "ticker": "069500",
    "name": "KODEX 200",
    "baseDate": "2025-01-31",
    "sectorBreakdown": [
      {
        "groupCode": "IT_SEMI",
        "groupName": "반도체",
        "weightPct": 28.5,
        "stockCount": 12,
        "topStocks": ["삼성전자", "SK하이닉스"]
      },
      {
        "groupCode": "IT_ELEC",
        "groupName": "전자/IT",
        "weightPct": 15.2,
        "stockCount": 8,
        "topStocks": ["LG전자", "삼성SDI"]
      },
      {
        "groupCode": "FINANCE",
        "groupName": "금융",
        "weightPct": 12.8,
        "stockCount": 15,
        "topStocks": ["KB금융", "신한지주"]
      },
      {
        "groupCode": "AUTO",
        "groupName": "자동차/2차전지",
        "weightPct": 10.5,
        "stockCount": 6,
        "topStocks": ["현대차", "기아"]
      }
    ],
    "totalSectors": 18
  }
}
```

---

### 9. ETF 뉴스 타임라인 조회
ETF에 영향을 미친 뉴스를 타임라인 형태로 조회 (실제 가격 변동 검증 포함)

**Request**
```
GET /api/v1/etf/1/timeline?limit=10&verifiedOnly=true
```

| Parameter | Type | 필수 | 설명 |
|-----------|------|------|------|
| limit | int | X | 조회 개수 (기본 10, 최대 50) |
| verifiedOnly | boolean | X | 검증된 뉴스만 조회 (기본 false) |

**Response**
```json
{
  "success": true,
  "data": {
    "etfId": 1,
    "ticker": "069500",
    "name": "KODEX 200",
    "timeline": [
      {
        "newsId": 123,
        "title": "연준 기준금리 동결 발표",
        "source": "한국경제",
        "publishedAt": "2025-01-17T09:30:00Z",
        "timelineTitle": "연준 금리 동결",
        "timelineSummary": "시장 예상치 부합, 기술주 중심 반등세",
        "influenceType": "POSITIVE",
        "influenceScore": 0.75,
        "actualChangeRate": 1.82,
        "isVerified": true,
        "verifiedAt": "2025-01-17T18:00:00Z"
      },
      {
        "newsId": 118,
        "title": "미중 반도체 갈등 고조",
        "source": "연합뉴스",
        "publishedAt": "2025-01-16T14:00:00Z",
        "timelineTitle": "미중 반도체 갈등",
        "timelineSummary": "수출 규제 강화 우려로 IT주 하락",
        "influenceType": "NEGATIVE",
        "influenceScore": 0.68,
        "actualChangeRate": -1.25,
        "isVerified": true,
        "verifiedAt": "2025-01-16T18:00:00Z"
      }
    ],
    "totalCount": 45
  }
}
```

---

### 10. ETF 추천
사용자의 관심 ETF 및 보유 ETF 기반 맞춤 추천

**Request**
```
GET /api/v1/etf/recommend
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "success": true,
  "data": {
    "recommendations": [
      {
        "etf": {
          "id": 5,
          "ticker": "102110",
          "name": "TIGER 200",
          "issuer": "미래에셋자산운용",
          "strategyType": "INDEX"
        },
        "reason": "관심 ETF 'KODEX 200'과 유사한 지수 추종 ETF입니다.",
        "similarity": 0.95
      }
    ]
  }
}
```

---

## 산업분류 코드 (group_code)

ETF 구성종목의 산업분류에 사용되는 22개 group_code:

| group_code | group_name | 대표 키워드 |
|------------|------------|-------------|
| IT_SEMI | 반도체 | 반도체, HBM, 파운드리, 메모리 |
| IT_ELEC | 전자/IT | 디스플레이, OLED, 가전, 스마트폰 |
| IT_SW | 소프트웨어 | AI, 클라우드, 게임, 플랫폼 |
| BIO | 바이오/의약 | 바이오, 제약, 신약, 의료기기 |
| AUTO | 자동차/2차전지 | 전기차, 배터리, 양극재, 음극재 |
| CHEM | 화학/소재 | 석유화학, 정밀화학, 화장품 |
| STEEL | 철강/금속 | 철강, 비철금속, 희토류, 리튬 |
| ENERGY | 에너지 | 태양광, 풍력, 원전, 수소 |
| FINANCE | 금융 | 은행, 증권, 카드, 금융지주 |
| INSURANCE | 보험 | 생명보험, 손해보험 |
| CONSTRUCT | 건설/부동산 | 건설, 부동산, 리츠, 인프라 |
| RETAIL | 유통/소매 | 백화점, 이커머스, 편의점 |
| FOOD | 식품/음료 | 식품, 음료, HMR |
| TELECOM | 통신/미디어 | 통신, 5G, 엔터테인먼트, OTT |
| CONSUMER | 소비재 | 패션, 여행, 항공, 레저 |
| TRANSPORT | 운송 | 해운, 물류, 택배, 항공화물 |
| MACHINERY | 기계/산업재 | 기계, 로봇, 자동화, 휴머노이드 |
| SHIPBUILD | 조선 | 조선, LNG선, 해양플랜트 |
| DEFENSE | 방산/우주항공 | 방산, 위성, 우주항공 |
| AGRI | 농업/어업 | 농업, 어업, 축산, 비료 |
| MINING | 광업/원자재 | 금, 원유, 천연가스, 우라늄 |
| OTHER | 기타 | 매핑 불가 시 |

---

## 에러 코드

| 코드 | 에러명 | 설명 |
|------|--------|------|
| ETF001 | ETF_NOT_FOUND | ETF를 찾을 수 없음 |
| ETF002 | ETF_PRICE_NOT_FOUND | ETF 가격 정보를 찾을 수 없음 |
| ETF003 | ETF_COMPOSITION_NOT_FOUND | ETF 구성종목 정보를 찾을 수 없음 |
| ETF004 | SECTOR_BREAKDOWN_NOT_FOUND | ETF 섹터 분포 정보를 찾을 수 없음 |
