# AI 피드백 API (Portfolio AI Feedback)

## 기본 정보
- Base URL: `/api/v1/ai`
- 인증: JWT 필요
- LLM: GPT-4 / Gemini / Claude (설정에 따라 선택)

---

## 엔드포인트 목록

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/portfolio/review` | 포트폴리오 AI 리뷰 요청 | O |
| GET | `/portfolio/review/{reviewId}` | 리뷰 결과 조회 | O |
| GET | `/portfolio/reviews` | 내 리뷰 히스토리 | O |
| POST | `/portfolio/review/{reviewId}/rating` | 리뷰 평가 (도움됨/안됨) | O |

---

## API 상세

### 1. 포트폴리오 AI 리뷰 요청
사용자가 구성한 포트폴리오에 대해 Bull/Bear 양면 분석을 요청합니다.

**Request**
```
POST /api/v1/ai/portfolio/review
Authorization: Bearer {accessToken}
Content-Type: application/json
```
```json
{
  "portfolioSnapshotId": 123,
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
| portfolioSnapshotId | long | X | 저장된 포트폴리오 스냅샷 ID |
| portfolio | object | O | 포트폴리오 구성 정보 |
| portfolio.totalAmount | long | O | 총 투자금액 (최소 10,000원, 최대 100억원) |
| portfolio.investmentType | string | O | 투자 유형: `LUMP_SUM` (관망형) / `DCA` (적립형) |
| portfolio.etfs | array | O | ETF 목록 (최소 1개, 최대 20개) |
| portfolio.etfs[].ticker | string | O | ETF 종목 코드 (6자리 숫자) |
| portfolio.etfs[].name | string | O | ETF 이름 (최대 200자) |
| portfolio.etfs[].weight | int | O | 비중 (%, 1~100, 합계 100 필수) |

**Response**
```json
{
  "success": true,
  "data": {
    "reviewId": 456,
    "status": "COMPLETED",
    "bullReview": {
      "summary": "균형 잡힌 포트폴리오로 안정적인 수익 기대",
      "points": [
        {
          "title": "분산 투자 효과",
          "description": "대형주(KODEX 200)와 테마형(반도체)을 적절히 배분하여 리스크 분산"
        },
        {
          "title": "반도체 업황 회복",
          "description": "2025년 반도체 업황 회복세가 예상되어 KODEX 반도체 ETF의 상승 여력 존재"
        },
        {
          "title": "배당 재투자",
          "description": "KBSTAR 200TR은 배당을 자동 재투자하여 복리 효과 기대"
        }
      ]
    },
    "bearReview": {
      "summary": "변동성과 섹터 집중 리스크에 주의 필요",
      "points": [
        {
          "title": "기술주 비중 과다",
          "description": "반도체 ETF 30% 비중으로 기술주 조정 시 손실 확대 가능"
        },
        {
          "title": "국내 시장 집중",
          "description": "해외 ETF 없이 국내에만 집중되어 글로벌 분산 부족"
        },
        {
          "title": "금리 리스크",
          "description": "금리 인상 시 성장주 중심 포트폴리오는 상대적 약세 예상"
        }
      ]
    },
    "overallScore": 7.2,
    "riskLevel": "MEDIUM",
    "recommendation": "현재 포트폴리오는 양호하나, 해외 ETF(미국 S&P500 등) 10~20% 추가를 권장합니다. 또한 채권형 ETF를 일부 편입하면 변동성을 줄일 수 있습니다.",
    "relatedNews": [
      {
        "newsId": 1,
        "title": "반도체 업황 회복 신호",
        "influenceType": "POSITIVE"
      },
      {
        "newsId": 5,
        "title": "미국 금리 동결 전망",
        "influenceType": "POSITIVE"
      }
    ],
    "createdAt": "2025-01-17T10:30:00Z"
  }
}
```

**Response - 처리 중**
```json
{
  "success": true,
  "data": {
    "reviewId": 456,
    "status": "PROCESSING",
    "message": "AI 분석이 진행 중입니다. 잠시 후 다시 조회해주세요.",
    "estimatedTime": 30
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
    "status": "COMPLETED",
    "bullReview": {
      "summary": "균형 잡힌 포트폴리오로 안정적인 수익 기대",
      "points": [...]
    },
    "bearReview": {
      "summary": "변동성과 섹터 집중 리스크에 주의 필요",
      "points": [...]
    },
    "overallScore": 7.2,
    "riskLevel": "MEDIUM",
    "recommendation": "...",
    "llmModel": "gpt-4",
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
| size | int | X | 페이지 크기 (기본 10) |

**Response**
```json
{
  "success": true,
  "data": {
    "reviews": [
      {
        "reviewId": 456,
        "portfolioSnapshotId": 123,
        "overallScore": 7.2,
        "riskLevel": "MEDIUM",
        "bullSummary": "균형 잡힌 포트폴리오로 안정적인 수익 기대",
        "bearSummary": "변동성과 섹터 집중 리스크에 주의 필요",
        "createdAt": "2025-01-17T10:30:00Z"
      },
      {
        "reviewId": 400,
        "portfolioSnapshotId": 100,
        "overallScore": 6.5,
        "riskLevel": "HIGH",
        "bullSummary": "고수익 잠재력 보유",
        "bearSummary": "높은 변동성에 주의 필요",
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

### 4. 리뷰 평가 (도움됨/안됨)

**Request**
```
POST /api/v1/ai/portfolio/review/456/rating
Authorization: Bearer {accessToken}
Content-Type: application/json
```
```json
{
  "rating": "HELPFUL",
  "comment": "Bull/Bear 분석이 이해하기 쉽고 유용했습니다."
}
```

| Field | Type | 필수 | 설명 |
|-------|------|------|------|
| rating | string | O | 평가: `HELPFUL` (도움됨) / `NOT_HELPFUL` (도움안됨) |
| comment | string | X | 추가 코멘트 |

**comment 규칙**
| 항목 | 값 |
|------|-----|
| 최대 길이 | 500자 |
| 허용 문자 | 한글, 영문, 숫자, 공백, 특수문자 |
| 필수 여부 | 선택 |
| 비고 | XSS 필터링 적용 |

**Response**
```json
{
  "success": true,
  "message": "평가가 등록되었습니다. 감사합니다!"
}
```

**Error Response**
```json
{
  "success": false,
  "error": {
    "code": "ALREADY_RATED",
    "message": "이미 평가한 리뷰입니다."
  }
}
```

---

## AI 분석 상세

### Bull/Bear 리뷰 생성 프로세스

1. **포트폴리오 데이터 수집**
   - ETF 구성 종목 및 비중
   - 각 ETF의 기초 지표 (PER, PBR, 배당률 등)
   - 섹터 분포

2. **시장 컨텍스트 수집**
   - 최근 관련 뉴스 (news_stock_mapping → etf_stock_composition 기반)
   - 시장 지표 (금리, 환율 등)

3. **LLM 프롬프트 구성**
   - 포트폴리오 정보
   - 시장 컨텍스트
   - Bull (긍정적) 관점 분석 요청
   - Bear (부정적) 관점 분석 요청

4. **결과 파싱 및 저장**
   - JSON 형식으로 구조화
   - 종합 점수 및 리스크 레벨 산출
   - DB 저장

### 종합 점수 기준

| 점수 | 등급 | 설명 |
|------|------|------|
| 8.0 ~ 10.0 | 우수 | 분산, 안정성, 성장성 모두 양호 |
| 6.0 ~ 7.9 | 양호 | 일부 개선 여지 있으나 전반적으로 적절 |
| 4.0 ~ 5.9 | 보통 | 리밸런싱 또는 종목 조정 권장 |
| 0.0 ~ 3.9 | 주의 | 높은 리스크 또는 불균형 포트폴리오 |

### 리스크 레벨 기준

| 레벨 | 설명 |
|------|------|
| LOW | 변동성 낮음, 안정적 배당/채권형 중심 |
| MEDIUM | 적절한 분산, 성장주와 안전자산 혼합 |
| HIGH | 테마형/레버리지 ETF 과다, 섹터 집중 |

---

## 에러 코드

| 코드 | 에러명 | 설명 |
|------|--------|------|
| AI001 | REVIEW_NOT_FOUND | 리뷰를 찾을 수 없음 |
| AI002 | REVIEW_GENERATION_FAILED | 리뷰 생성 실패 |
| AI003 | INVALID_PORTFOLIO_FOR_REVIEW | 유효하지 않은 포트폴리오 |
| AI004 | REVIEW_PROCESSING | 리뷰 분석 중 |
| AI005 | ALREADY_RATED | 이미 평가한 리뷰 |
| AI006 | AI_SERVICE_UNAVAILABLE | AI 서비스 일시 불가 |
