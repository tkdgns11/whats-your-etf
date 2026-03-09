# AI 피드백 API (Portfolio AI Feedback)

## 기본 정보
- Base URL: `/api/v1/ai`
- 인증: JWT 필요
- LLM: Claude (GMS API 연동)

---

## 엔드포인트 목록

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/portfolio/review` | 포트폴리오 AI 리뷰 요청 | O |
| GET | `/portfolio/review/{reviewId}` | 리뷰 결과 조회 | O |
| GET | `/portfolio/reviews` | 내 리뷰 히스토리 | O |

---

## API 상세

### 1. 포트폴리오 AI 리뷰 요청
사용자가 구성한 포트폴리오에 대해 AI 분석을 요청합니다.

**Request**
```
POST /api/v1/ai/portfolio/review
Authorization: Bearer {accessToken}
Content-Type: application/json
```
```json
{
  "portfolio": {
    "totalAmount": 10000000,
    "investmentType": "LUMP_SUM",
    "etfs": [
      {
        "ticker": "069500",
        "name": "KODEX 200",
        "weight": 40
      },
      {
        "ticker": "091160",
        "name": "KODEX 반도체",
        "weight": 30
      },
      {
        "ticker": "148020",
        "name": "KBSTAR 200TR",
        "weight": 30
      }
    ]
  }
}
```

| Field | Type | 필수 | 설명 |
|-------|------|------|------|
| portfolio | object | O | 포트폴리오 구성 정보 |
| portfolio.totalAmount | long | O | 총 투자금액 (최소 10,000원, 최대 100억원) |
| portfolio.investmentType | string | O | 투자 유형: `LUMP_SUM` (관망형) / `DCA` (적립형) |
| portfolio.etfs | array | O | ETF 목록 (최소 1개, 최대 20개) |
| portfolio.etfs[].ticker | string | O | ETF 종목 코드 (6자리 숫자) |
| portfolio.etfs[].name | string | O | ETF 이름 (최대 200자) |
| portfolio.etfs[].weight | int | O | 비중 (%, 1~100, 합계 100 필수) |

**Response - 완료**
```json
{
  "success": true,
  "data": {
    "reviewId": 456,
    "headline": "공격적인 성장 추구!",
    "subHeadline": "기술주 중심의 고성장 전략 포트폴리오",
    "keywords": ["기술주집중", "고변동성", "성장중심"],
    "analysis": "포트폴리오는 국내 대형주와 반도체 섹터에 집중되어 있습니다. KODEX 200(40%)으로 시장 전반에 투자하면서, KODEX 반도체(30%)로 기술주 비중을 높였습니다. KBSTAR 200TR(30%)은 배당 재투자 전략으로 복리 효과를 기대할 수 있습니다. 다만 국내 시장에만 집중되어 있어 글로벌 분산이 부족하고, 반도체 업황에 따른 변동성이 높을 수 있습니다. 해외 ETF 편입을 고려해보시기 바랍니다.",
    "llmModel": "claude-sonnet-4-20250514",
    "createdAt": "2025-01-17T10:30:00Z"
  }
}
```

**Response - 처리 중 (분석 미완료)**
```json
{
  "success": true,
  "data": {
    "reviewId": 456,
    "headline": null,
    "subHeadline": null,
    "keywords": null,
    "analysis": null,
    "llmModel": null,
    "createdAt": null
  }
}
```

---

### 2. 리뷰 결과 조회

**Request**
```
GET /api/v1/ai/portfolio/review/456
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "success": true,
  "data": {
    "reviewId": 456,
    "headline": "공격적인 성장 추구!",
    "subHeadline": "기술주 중심의 고성장 전략 포트폴리오",
    "keywords": ["기술주집중", "고변동성", "성장중심"],
    "analysis": "포트폴리오는 국내 대형주와 반도체 섹터에 집중되어 있습니다...",
    "llmModel": "claude-sonnet-4-20250514",
    "createdAt": "2025-01-17T10:30:00Z"
  }
}
```

**Error Response**
```json
{
  "success": false,
  "error": {
    "code": "REVIEW_NOT_FOUND",
    "message": "리뷰를 찾을 수 없습니다."
  }
}
```

---

### 3. 내 리뷰 히스토리

**Request**
```
GET /api/v1/ai/portfolio/reviews?page=0&size=10
Authorization: Bearer {accessToken}
```

| Parameter | Type | 필수 | 설명 |
|-----------|------|------|------|
| page | int | X | 페이지 번호 (기본 0) |
| size | int | X | 페이지 크기 (기본 10, 최대 50) |

**Response**
```json
{
  "success": true,
  "data": {
    "reviews": [
      {
        "reviewId": 456,
        "headline": "공격적인 성장 추구!",
        "subHeadline": "기술주 중심의 고성장 전략 포트폴리오",
        "keywords": ["기술주집중", "고변동성", "성장중심"],
        "createdAt": "2025-01-17T10:30:00Z"
      },
      {
        "reviewId": 400,
        "headline": "안정적인 배당 전략",
        "subHeadline": "배당주 중심의 저변동성 포트폴리오",
        "keywords": ["배당형", "안정적", "저변동성"],
        "createdAt": "2025-01-15T14:00:00Z"
      }
    ],
    "page": 0,
    "totalPages": 3,
    "totalElements": 25
  }
}
```

---

## 응답 필드 설명

| 필드 | 타입 | 설명 |
|------|------|------|
| reviewId | long | 리뷰 고유 ID |
| headline | string | 포트폴리오 특성 한 문장 (15자 내외) |
| subHeadline | string | 부제목 구체적 설명 (25자 내외) |
| keywords | string[] | 포트폴리오 특성 키워드 (3~5개) |
| analysis | string | 종합 분석 결과 (200~300자) |
| llmModel | string | 사용된 LLM 모델명 |
| createdAt | datetime | 생성일시 (ISO 8601) |

---

## 에러 코드

| 코드 | 에러명 | 설명 |
|------|--------|------|
| AI001 | REVIEW_NOT_FOUND | 리뷰를 찾을 수 없음 |
| AI002 | REVIEW_GENERATION_FAILED | 리뷰 생성 실패 |
| AI003 | INVALID_WEIGHT_SUM | 비중 합계가 100%가 아님 |
| AI004 | AI_SERVICE_UNAVAILABLE | AI 서비스 일시 불가 |
| AI005 | USER_NOT_FOUND | 사용자를 찾을 수 없음 |
