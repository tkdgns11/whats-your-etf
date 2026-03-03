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
| page | int | X | 페이지 번호 (기본 0) |
| size | int | X | 페이지 크기 (기본 20) |
| strategyType | string | X | 전략 유형 (INDEX/ACTIVE/DERIVATIVE) |
| sortBy | string | X | 정렬 기준 (totalAsset/expenseRatio/name) |

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
| keyword | string | O | 검색어 (ETF명, 티커) |
| strategyType | string | X | 전략 유형 필터 |
| issuer | string | X | 운용사 필터 |

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

### 8. ETF 추천
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

## 에러 코드

| 코드 | 에러명 | 설명 |
|------|--------|------|
| ETF001 | ETF_NOT_FOUND | ETF를 찾을 수 없음 |
| ETF002 | ETF_PRICE_NOT_FOUND | ETF 가격 정보를 찾을 수 없음 |
| ETF003 | ETF_COMPOSITION_NOT_FOUND | ETF 구성종목 정보를 찾을 수 없음 |
