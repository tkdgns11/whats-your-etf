# 포트폴리오 API (Portfolio)

## 기본 정보
- Base URL: `/api/v1/portfolios`
- 인증: JWT 필요

---

## 엔드포인트 목록

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/` | 내 포트폴리오 목록 조회 | O |
| POST | `/` | 포트폴리오 생성 | O |
| GET | `/{portfolioId}` | 포트폴리오 상세 조회 | O |
| PUT | `/{portfolioId}` | 포트폴리오 수정 | O |
| DELETE | `/{portfolioId}` | 포트폴리오 삭제 | O |
| GET | `/{portfolioId}/performance` | 포트폴리오 수익률 조회 | O |
| POST | `/{portfolioId}/etfs` | 포트폴리오 ETF 추가 | O |
| PUT | `/{portfolioId}/etfs` | 포트폴리오 ETF 비중 수정 | O |
| DELETE | `/{portfolioId}/etfs/{etfId}` | 포트폴리오 ETF 삭제 | O |

---

## API 상세

### 1. 내 포트폴리오 목록 조회

**Request**
```
GET /api/v1/portfolios?page=0&size=10
Authorization: Bearer {accessToken}
```

| Parameter | Type | 필수 | 설명 |
|-----------|------|------|------|
| page | int | X | 페이지 번호 (기본 0) |
| size | int | X | 페이지 크기 (기본 10) |

**Response**
```json
{
  "success": true,
  "data": {
    "portfolios": [
      {
        "id": 1,
        "name": "나의 첫 포트폴리오",
        "totalInvestment": 10000000,
        "currentValue": 10500000,
        "returnRate": 5.0,
        "etfCount": 5,
        "createdAt": "2025-01-01T10:00:00Z",
        "updatedAt": "2025-01-15T14:30:00Z"
      }
    ],
    "page": 0,
    "totalPages": 1,
    "totalElements": 3
  }
}
```

---

### 2. 포트폴리오 생성

**Request**
```
POST /api/v1/portfolios
Authorization: Bearer {accessToken}
Content-Type: application/json
```
```json
{
  "name": "나의 성장 포트폴리오",
  "description": "장기 성장을 목표로 하는 포트폴리오",
  "totalInvestment": 10000000,
  "etfs": [
    {
      "etfId": 1,
      "weightPct": 40.0
    },
    {
      "etfId": 2,
      "weightPct": 30.0
    },
    {
      "etfId": 3,
      "weightPct": 30.0
    }
  ]
}
```

| Field | Type | 필수 | 설명 |
|-------|------|------|------|
| name | string | O | 포트폴리오 이름 |
| description | string | X | 포트폴리오 설명 |
| totalInvestment | number | O | 총 투자금액 (최소 10,000원, 최대 100억원) |
| etfs | array | O | ETF 구성 (최소 1개, 최대 20개) |
| etfs[].etfId | number | O | ETF ID (양수 정수) |
| etfs[].weightPct | number | O | 비중 (0.001~100.000, 소수점 3자리, 합계 100 필수) |

**name 규칙**
| 항목 | 값 |
|------|-----|
| 최소 길이 | 1자 |
| 최대 길이 | 100자 |
| 허용 문자 | 한글, 영문, 숫자, 공백, 특수문자(-_) |
| 정규식 | `^[가-힣a-zA-Z0-9\s\-_]{1,100}$` |

**description 규칙**
| 항목 | 값 |
|------|-----|
| 최대 길이 | 1000자 (DB: TEXT) |
| 허용 문자 | 모든 문자 |
| 필수 여부 | 선택 |
| 비고 | XSS 필터링 적용 |

**Response**
```json
{
  "success": true,
  "message": "포트폴리오가 생성되었습니다.",
  "data": {
    "id": 1,
    "name": "나의 성장 포트폴리오"
  }
}
```

---

### 3. 포트폴리오 상세 조회

**Request**
```
GET /api/v1/portfolios/1
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "나의 성장 포트폴리오",
    "description": "장기 성장을 목표로 하는 포트폴리오",
    "totalInvestment": 10000000,
    "currentValue": 10500000,
    "returnRate": 5.0,
    "createdAt": "2025-01-01T10:00:00Z",
    "updatedAt": "2025-01-15T14:30:00Z",
    "etfs": [
      {
        "etfId": 1,
        "ticker": "069500",
        "name": "KODEX 200",
        "weightPct": 40.0,
        "investedAmount": 4000000,
        "currentAmount": 4200000,
        "returnRate": 5.0
      },
      {
        "etfId": 2,
        "ticker": "102110",
        "name": "TIGER 200",
        "weightPct": 30.0,
        "investedAmount": 3000000,
        "currentAmount": 3150000,
        "returnRate": 5.0
      },
      {
        "etfId": 3,
        "ticker": "226490",
        "name": "KODEX 코스피",
        "weightPct": 30.0,
        "investedAmount": 3000000,
        "currentAmount": 3150000,
        "returnRate": 5.0
      }
    ]
  }
}
```

---

### 4. 포트폴리오 수정

**Request**
```
PUT /api/v1/portfolios/1
Authorization: Bearer {accessToken}
Content-Type: application/json
```
```json
{
  "name": "수정된 포트폴리오 이름",
  "description": "수정된 설명",
  "totalInvestment": 15000000
}
```

| Field | Type | 필수 | 설명 |
|-------|------|------|------|
| name | string | X | 포트폴리오 이름 (1~100자) |
| description | string | X | 포트폴리오 설명 (최대 1000자) |
| totalInvestment | number | X | 총 투자금액 (최소 10,000원, 최대 100억원) |

> 최소 1개 이상의 필드를 포함해야 합니다.

**Response**
```json
{
  "success": true,
  "message": "포트폴리오가 수정되었습니다."
}
```

---

### 5. 포트폴리오 삭제

**Request**
```
DELETE /api/v1/portfolios/1
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "success": true,
  "message": "포트폴리오가 삭제되었습니다."
}
```

---

### 6. 포트폴리오 수익률 조회

**Request**
```
GET /api/v1/portfolios/1/performance?startDate=2025-01-01&endDate=2025-01-31
Authorization: Bearer {accessToken}
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
    "portfolioId": 1,
    "name": "나의 성장 포트폴리오",
    "totalInvestment": 10000000,
    "currentValue": 10500000,
    "totalReturn": 500000,
    "totalReturnRate": 5.0,
    "dailyReturns": [
      {
        "date": "2025-01-31",
        "value": 10500000,
        "returnRate": 5.0
      },
      {
        "date": "2025-01-30",
        "value": 10450000,
        "returnRate": 4.5
      }
    ],
    "benchmarkComparison": {
      "benchmark": "KOSPI 200",
      "benchmarkReturn": 3.5,
      "alpha": 1.5
    }
  }
}
```

---

### 7. 포트폴리오 ETF 추가

> **비중 정책**: ETF 추가 후 전체 비중 합계가 정확히 100%여야 합니다.
> 클라이언트에서 기존 ETF 비중을 먼저 조정한 후 추가해야 합니다.

**Request**
```
POST /api/v1/portfolios/1/etfs
Authorization: Bearer {accessToken}
Content-Type: application/json
```
```json
{
  "etfId": 4,
  "weightPct": 20.0
}
```

| Field | Type | 필수 | 설명 |
|-------|------|------|------|
| etfId | number | O | 추가할 ETF ID |
| weightPct | number | O | 비중 (0.001~100.000, 추가 후 합계 100% 필수) |

**Response**
```json
{
  "success": true,
  "message": "ETF가 포트폴리오에 추가되었습니다."
}
```

---

### 8. 포트폴리오 ETF 비중 수정

> **비중 정책**: 전체 ETF 비중 합계가 정확히 100%여야 합니다. (100% 미만, 초과 모두 불가)

**Request**
```
PUT /api/v1/portfolios/1/etfs
Authorization: Bearer {accessToken}
Content-Type: application/json
```
```json
{
  "etfs": [
    {
      "etfId": 1,
      "weightPct": 30.0
    },
    {
      "etfId": 2,
      "weightPct": 25.0
    },
    {
      "etfId": 3,
      "weightPct": 25.0
    },
    {
      "etfId": 4,
      "weightPct": 20.0
    }
  ]
}
```

| Field | Type | 필수 | 설명 |
|-------|------|------|------|
| etfs | array | O | ETF 비중 목록 |
| etfs[].etfId | number | O | ETF ID |
| etfs[].weightPct | number | O | 비중 (0.001~100.000, 합계 100% 필수) |

**Response**
```json
{
  "success": true,
  "message": "ETF 비중이 수정되었습니다."
}
```

---

### 9. 포트폴리오 ETF 삭제

> **비중 정책**: 삭제 후 남은 ETF 비중 합계가 100%여야 합니다.
> 클라이언트에서 남은 ETF 비중을 먼저 조정한 후 삭제해야 합니다.

**Request**
```
DELETE /api/v1/portfolios/1/etfs/4
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "success": true,
  "message": "ETF가 포트폴리오에서 삭제되었습니다."
}
```

---

## 에러 코드

| 코드 | 에러명 | 설명 |
|------|--------|------|
| PORTFOLIO001 | PORTFOLIO_NOT_FOUND | 포트폴리오를 찾을 수 없음 |
| PORTFOLIO002 | PORTFOLIO_ACCESS_DENIED | 포트폴리오 접근 권한이 없음 |
| PORTFOLIO003 | DUPLICATE_PORTFOLIO_NAME | 이미 사용 중인 포트폴리오 이름 |
| PORTFOLIO004 | PORTFOLIO_ETF_LIMIT_EXCEEDED | 포트폴리오 ETF 개수 제한 초과 |
| PORTFOLIO005 | INVALID_WEIGHT_SUM | ETF 비중 합계가 100%가 아님 |
| ETF001 | ETF_NOT_FOUND | ETF를 찾을 수 없음 |
