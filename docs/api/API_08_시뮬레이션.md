# 시뮬레이션 API (Simulation)

## 기본 정보
- Base URL: `/api/v1/simulations`
- 인증: JWT 필요
- **시뮬레이션 정책**: 프론트엔드에서 시뮬레이션 계산을 수행하고, 사용자가 저장을 원할 때만 API를 호출하여 결과 저장

---

## 엔드포인트 목록

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/` | 내 시뮬레이션 목록 조회 | O |
| POST | `/` | 시뮬레이션 결과 저장 | O |
| GET | `/{simulationId}` | 시뮬레이션 결과 조회 | O |
| DELETE | `/{simulationId}` | 시뮬레이션 삭제 | O |
| POST | `/compare` | 포트폴리오 비교 (저장하지 않음) | O |

---

## API 상세

### 1. 내 시뮬레이션 목록 조회

**Request**
```
GET /api/v1/simulations?page=0&size=10
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
    "simulations": [
      {
        "id": 1,
        "portfolioId": 1,
        "portfolioName": "나의 성장 포트폴리오",
        "startDate": "2024-01-01",
        "endDate": "2024-12-31",
        "initialAmount": 10000000,
        "finalAmount": 11500000,
        "totalReturn": 15.0,
        "createdAt": "2025-01-15T10:00:00Z"
      }
    ],
    "page": 0,
    "totalPages": 1,
    "totalElements": 5
  }
}
```

---

### 2. 시뮬레이션 결과 저장

프론트엔드에서 계산한 시뮬레이션 결과를 저장합니다. 사용자가 "저장" 버튼을 클릭할 때만 호출됩니다.

**Request**
```
POST /api/v1/simulations
Authorization: Bearer {accessToken}
Content-Type: application/json
```
```json
{
  "portfolioId": 1,
  "startDate": "2024-01-01",
  "endDate": "2024-12-31",
  "initialAmount": 10000000,
  "rebalancingCycle": "MONTHLY",
  "result": {
    "finalAmount": 11500000,
    "totalReturn": 1500000,
    "totalReturnRate": 15.0,
    "annualizedReturn": 15.0,
    "maxDrawdown": -8.5,
    "sharpeRatio": 1.2,
    "volatility": 12.5,
    "monthlyReturns": [
      { "month": "2024-01", "value": 10200000, "returnRate": 2.0 },
      { "month": "2024-02", "value": 10350000, "returnRate": 1.47 }
    ],
    "etfPerformance": [
      { "etfId": 1, "weightPct": 40.0, "returnRate": 12.0, "contribution": 4.8 },
      { "etfId": 2, "weightPct": 30.0, "returnRate": 18.0, "contribution": 5.4 }
    ]
  }
}
```

| Field | Type | 필수 | 설명 |
|-------|------|------|------|
| portfolioId | number | O | 포트폴리오 ID (양수 정수) |
| startDate | string | O | 시뮬레이션 시작일 (YYYY-MM-DD, 정규식: `^\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01])$`) |
| endDate | string | O | 시뮬레이션 종료일 (YYYY-MM-DD) |
| initialAmount | number | O | 초기 투자금액 (최소 10,000원, 최대 100억원) |
| rebalancingCycle | string | X | 리밸런싱 주기: `NONE` / `MONTHLY` / `QUARTERLY` / `YEARLY` (기본값: NONE) |
| result | object | O | 프론트엔드에서 계산한 시뮬레이션 결과 |

**result 객체 상세**
| Field | Type | 필수 | 설명 |
|-------|------|------|------|
| finalAmount | number | O | 최종 금액 |
| totalReturn | number | O | 총 수익금액 |
| totalReturnRate | number | O | 총 수익률 (%) |
| annualizedReturn | number | O | 연환산 수익률 (%) |
| maxDrawdown | number | O | 최대 낙폭 (%) |
| sharpeRatio | number | O | 샤프 비율 |
| volatility | number | O | 변동성 (%) |
| monthlyReturns | array | O | 월별 수익률 배열 |
| etfPerformance | array | O | ETF별 성과 배열 |

**Response**
```json
{
  "success": true,
  "message": "시뮬레이션이 저장되었습니다.",
  "data": {
    "simulationId": 1
  }
}
```

---

### 3. 시뮬레이션 결과 조회

**Request**
```
GET /api/v1/simulations/1
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "portfolioId": 1,
    "portfolioName": "나의 성장 포트폴리오",
    "startDate": "2024-01-01",
    "endDate": "2024-12-31",
    "initialAmount": 10000000,
    "finalAmount": 11500000,
    "rebalancingCycle": "MONTHLY",
    "summary": {
      "totalReturn": 1500000,
      "totalReturnRate": 15.0,
      "annualizedReturn": 15.0,
      "maxDrawdown": -8.5,
      "sharpeRatio": 1.2,
      "volatility": 12.5
    },
    "monthlyReturns": [
      {
        "month": "2024-01",
        "value": 10200000,
        "returnRate": 2.0
      },
      {
        "month": "2024-02",
        "value": 10350000,
        "returnRate": 1.47
      }
    ],
    "etfPerformance": [
      {
        "etfId": 1,
        "ticker": "069500",
        "name": "KODEX 200",
        "weightPct": 40.0,
        "returnRate": 12.0,
        "contribution": 4.8
      },
      {
        "etfId": 2,
        "ticker": "102110",
        "name": "TIGER 200",
        "weightPct": 30.0,
        "returnRate": 18.0,
        "contribution": 5.4
      }
    ],
    "benchmarkComparison": {
      "benchmark": "KOSPI 200",
      "benchmarkReturn": 10.0,
      "alpha": 5.0
    },
    "createdAt": "2025-01-15T10:00:00Z"
  }
}
```

---

### 4. 시뮬레이션 삭제

**Request**
```
DELETE /api/v1/simulations/1
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "success": true,
  "message": "시뮬레이션이 삭제되었습니다."
}
```

---

### 5. 포트폴리오 비교
여러 포트폴리오의 과거 수익률 데이터를 조회합니다. 결과는 저장되지 않습니다

**Request**
```
POST /api/v1/simulations/compare
Authorization: Bearer {accessToken}
Content-Type: application/json
```
```json
{
  "portfolioIds": [1, 2, 3],
  "startDate": "2024-01-01",
  "endDate": "2024-12-31",
  "initialAmount": 10000000
}
```

| Field | Type | 필수 | 설명 |
|-------|------|------|------|
| portfolioIds | array | O | 비교할 포트폴리오 ID 목록 (최소 2개, 최대 5개, 양수 정수) |
| startDate | string | O | 시뮬레이션 시작일 (YYYY-MM-DD, 최소 2010-01-01) |
| endDate | string | O | 시뮬레이션 종료일 (YYYY-MM-DD, startDate 이후, 오늘 이전) |
| initialAmount | number | O | 초기 투자금액 (최소 10,000원, 최대 100억원) |

**Response**
```json
{
  "success": true,
  "data": {
    "startDate": "2024-01-01",
    "endDate": "2024-12-31",
    "initialAmount": 10000000,
    "comparisons": [
      {
        "portfolioId": 1,
        "portfolioName": "나의 성장 포트폴리오",
        "finalAmount": 11500000,
        "totalReturnRate": 15.0,
        "maxDrawdown": -8.5,
        "sharpeRatio": 1.2,
        "rank": 1
      },
      {
        "portfolioId": 2,
        "portfolioName": "배당 포트폴리오",
        "finalAmount": 11200000,
        "totalReturnRate": 12.0,
        "maxDrawdown": -5.0,
        "sharpeRatio": 1.5,
        "rank": 2
      },
      {
        "portfolioId": 3,
        "portfolioName": "밸런스 포트폴리오",
        "finalAmount": 10800000,
        "totalReturnRate": 8.0,
        "maxDrawdown": -3.0,
        "sharpeRatio": 1.8,
        "rank": 3
      }
    ],
    "benchmark": {
      "name": "KOSPI 200",
      "returnRate": 10.0
    }
  }
}
```

---

## 시뮬레이션 지표 설명

| 지표 | 설명 |
|------|------|
| totalReturn | 총 수익금액 |
| totalReturnRate | 총 수익률 (%) |
| annualizedReturn | 연환산 수익률 (%) |
| maxDrawdown | 최대 낙폭 (%) - 고점 대비 최대 하락폭 |
| sharpeRatio | 샤프 비율 - 위험 대비 초과수익 |
| volatility | 변동성 (%) - 수익률의 표준편차 |
| alpha | 알파 - 벤치마크 대비 초과수익률 |

---

## 에러 코드

| 코드 | 에러명 | 설명 |
|------|--------|------|
| SIM001 | SIMULATION_NOT_FOUND | 시뮬레이션을 찾을 수 없음 |
| SIM002 | SIMULATION_FAILED | 시뮬레이션 실행 실패 |
| SIM003 | INVALID_SIMULATION_PERIOD | 잘못된 시뮬레이션 기간 |
| PORTFOLIO001 | PORTFOLIO_NOT_FOUND | 포트폴리오를 찾을 수 없음 |
