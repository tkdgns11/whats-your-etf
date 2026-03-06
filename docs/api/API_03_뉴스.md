# 뉴스 API (News)

## 기본 정보
- Base URL: `/api/v1/news`
- 인증: 일부 엔드포인트 제외하고 JWT 필요
- 크롤링 서비스: FastAPI (Python) - 10분 간격 자동 수집

---

## 엔드포인트 목록

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/` | 최신 뉴스 목록 조회 | X |
| GET | `/{newsId}` | 뉴스 상세 조회 | X |
| GET | `/search` | 뉴스 검색 | X |
| GET | `/etf/{ticker}` | ETF 관련 뉴스 조회 | X |
| GET | `/etf/{ticker}/influence` | ETF 영향력 뉴스 조회 | X |
| GET | `/etf/{ticker}/timeline` | ETF 뉴스 타임라인 조회 | X |
| GET | `/highlight` | 하이라이트 뉴스 (맵 뷰용) | X |
| POST | `/scrape` | 수동 크롤링 트리거 (관리자) | O |

---

## API 상세

### 1. 최신 뉴스 목록 조회

**Request**
```
GET /api/v1/news?limit=20&offset=0
```

| Parameter | Type | 필수 | 기본값 | 설명 |
|-----------|------|------|--------|------|
| limit | int | X | 20 | 조회 개수 (최대 100) |
| offset | int | X | 0 | 시작 위치 |
| category | string | X | - | 카테고리 필터 (금융/ETF/경제) |

**Response**
```json
{
  "success": true,
  "data": {
    "news": [
      {
        "id": 1,
        "title": "반도체 ETF, 외국인 순매수 지속",
        "source": "한국경제",
        "thumbnailUrl": "https://...",
        "category": "ETF",
        "keywords": ["반도체", "ETF", "외국인"],
        "publishedAt": "2025-01-17T09:30:00Z"
      },
      {
        "id": 2,
        "title": "금리 인하 기대감에 채권 ETF 강세",
        "source": "매일경제",
        "thumbnailUrl": null,
        "category": "금융",
        "keywords": ["금리", "채권", "ETF"],
        "publishedAt": "2025-01-17T08:45:00Z"
      }
    ],
    "totalCount": 150,
    "hasMore": true
  }
}
```

---

### 2. 뉴스 상세 조회

**Request**
```
GET /api/v1/news/1
```

**Response**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "반도체 ETF, 외국인 순매수 지속",
    "keywords": ["반도체", "ETF", "외국인", "순매수"],
    "aiSummary": [
      "외국인 투자자들이 반도체 관련 ETF를 5거래일 연속 순매수하며 강한 매수세를 보이고 있음",
      "삼성전자, SK하이닉스 등 주요 반도체 종목 비중이 높은 ETF 중심으로 자금 유입",
      "AI·데이터센터 수요 증가 기대감이 반도체 섹터 투자 심리를 뒷받침하는 것으로 분석"
    ],
    "content": "외국인이 반도체 관련 ETF를 5거래일 연속 순매수하며 시장의 관심을 모으고 있다...(본문 전체)",
    "source": "한국경제",
    "sourceUrl": "https://news.google.com/...",
    "thumbnailUrl": "https://...",
    "category": "ETF",
    "publishedAt": "2025-01-17T09:30:00Z",
    "viewCount": 126,
    "relatedEtfs": [
      {
        "id": 1,
        "ticker": "091160",
        "name": "KODEX 반도체",
        "changeRate": 1.24,
        "influenceScore": 0.85,
        "influenceType": "POSITIVE"
      },
      {
        "id": 2,
        "ticker": "091170",
        "name": "KODEX 반도체레버리지",
        "changeRate": 2.48,
        "influenceScore": 0.78,
        "influenceType": "POSITIVE"
      }
    ]
  }
}
```

**Error Response**
```json
{
  "success": false,
  "error": {
    "code": "NEWS_NOT_FOUND",
    "message": "뉴스를 찾을 수 없습니다."
  }
}
```

---

### 3. 뉴스 검색

**Request**
```
GET /api/v1/news/search?keyword=반도체&limit=20
```

| Parameter | Type | 필수 | 설명 |
|-----------|------|------|------|
| keyword | string | O | 검색 키워드 |
| limit | int | X | 조회 개수 (기본 20) |
| offset | int | X | 시작 위치 (기본 0) |

**Response**
```json
{
  "success": true,
  "data": {
    "news": [
      {
        "id": 1,
        "title": "반도체 ETF, 외국인 순매수 지속",
        "keywords": ["반도체", "ETF", "외국인"],
        "source": "한국경제",
        "publishedAt": "2025-01-17T09:30:00Z"
      }
    ],
    "totalCount": 25,
    "keyword": "반도체"
  }
}
```

---

### 4. ETF 관련 뉴스 조회

**Request**
```
GET /api/v1/news/etf/091160?limit=10
```

| Parameter | Type | 필수 | 설명 |
|-----------|------|------|------|
| ticker | string | O | ETF 종목 코드 |
| limit | int | X | 조회 개수 (기본 10) |

**Response**
```json
{
  "success": true,
  "data": {
    "etf": {
      "ticker": "091160",
      "name": "KODEX 반도체"
    },
    "news": [
      {
        "id": 1,
        "title": "반도체 ETF, 외국인 순매수 지속",
        "keywords": ["반도체", "ETF", "외국인"],
        "source": "한국경제",
        "publishedAt": "2025-01-17T09:30:00Z",
        "influenceScore": 0.85,
        "influenceType": "POSITIVE"
      }
    ],
    "totalCount": 15
  }
}
```

---

### 5. ETF 영향력 뉴스 조회
특정 ETF에 영향을 미치는 뉴스를 영향력 점수 순으로 조회

**Request**
```
GET /api/v1/news/etf/091160/influence?limit=5
```

**Response**
```json
{
  "success": true,
  "data": {
    "etf": {
      "ticker": "091160",
      "name": "KODEX 반도체"
    },
    "influences": [
      {
        "news": {
          "id": 1,
          "title": "삼성전자 HBM 대규모 수주 성공",
          "source": "한국경제",
          "publishedAt": "2025-01-17T09:30:00Z"
        },
        "influenceScore": 0.92,
        "influenceType": "POSITIVE",
        "analysisReason": "삼성전자가 KODEX 반도체 ETF의 35% 비중을 차지하며, HBM 수주는 매출 증가로 이어질 전망"
      },
      {
        "news": {
          "id": 5,
          "title": "미국 반도체 수출 규제 강화 검토",
          "source": "연합뉴스",
          "publishedAt": "2025-01-17T08:00:00Z"
        },
        "influenceScore": 0.78,
        "influenceType": "NEGATIVE",
        "analysisReason": "미국의 대중국 반도체 수출 규제 강화 시 국내 반도체 기업 실적에 부정적 영향 예상"
      }
    ]
  }
}
```

---

### 6. ETF 뉴스 타임라인 조회
특정 ETF에 영향을 미친 뉴스를 타임라인 형태로 조회 (실제 가격 변동 검증 포함)

**Request**
```
GET /api/v1/news/etf/091160/timeline?limit=10&verified_only=true
```

| Parameter | Type | 필수 | 기본값 | 설명 |
|-----------|------|------|--------|------|
| ticker | string | O | - | ETF 종목 코드 |
| limit | int | X | 10 | 조회 개수 (최대 50) |
| verified_only | boolean | X | false | 검증된 뉴스만 조회 |

**Response**
```json
{
  "success": true,
  "data": {
    "etf": {
      "ticker": "091160",
      "name": "KODEX 반도체"
    },
    "timeline": [
      {
        "news": {
          "id": 1,
          "title": "삼성전자 HBM 대규모 수주 성공",
          "source": "한국경제",
          "publishedAt": "2025-01-17T09:30:00Z"
        },
        "timelineTitle": "삼성전자 HBM 수주 발표",
        "timelineSummary": "글로벌 AI 기업 대상 HBM 대규모 공급 계약 체결",
        "influenceScore": 0.92,
        "influenceType": "POSITIVE",
        "actualChangeRate": 2.35,
        "isVerified": true,
        "verifiedAt": "2025-01-17T18:00:00Z"
      },
      {
        "news": {
          "id": 5,
          "title": "미국 반도체 수출 규제 강화 검토",
          "source": "연합뉴스",
          "publishedAt": "2025-01-16T08:00:00Z"
        },
        "timelineTitle": "미국 대중 반도체 규제 검토",
        "timelineSummary": "미 상무부, 추가 수출 규제 검토 중",
        "influenceScore": 0.78,
        "influenceType": "NEGATIVE",
        "actualChangeRate": -1.82,
        "isVerified": true,
        "verifiedAt": "2025-01-16T18:00:00Z"
      }
    ],
    "totalCount": 25
  }
}
```

---

### 7. 하이라이트 뉴스 (맵 뷰용)
ETF 맵에서 하이라이트할 뉴스 기반 클러스터 정보 반환

**Request**
```
GET /api/v1/news/highlight
```

**Response**
```json
{
  "success": true,
  "data": {
    "highlights": [
      {
        "news": {
          "id": 1,
          "title": "반도체 업황 회복 신호",
          "source": "한국경제",
          "publishedAt": "2025-01-17T09:30:00Z"
        },
        "affectedClusters": [
          {
            "clusterId": 3,
            "clusterName": "반도체/IT",
            "influenceType": "POSITIVE",
            "avgInfluenceScore": 0.82
          }
        ],
        "affectedEtfs": [
          {
            "ticker": "091160",
            "name": "KODEX 반도체",
            "influenceScore": 0.92
          },
          {
            "ticker": "091170",
            "name": "KODEX 반도체레버리지",
            "influenceScore": 0.88
          }
        ]
      }
    ],
    "lastUpdatedAt": "2025-01-17T10:00:00Z"
  }
}
```

---

### 8. 수동 크롤링 트리거 (관리자)

**Request**
```
POST /api/v1/news/scrape
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "success": true,
  "data": {
    "scrapedCount": 45,
    "startedAt": "2025-01-17T10:00:00Z",
    "completedAt": "2025-01-17T10:02:30Z"
  },
  "message": "뉴스 크롤링이 완료되었습니다."
}
```

---

## 크롤링 시스템

### 크롤링 소스
- **Google News RSS API** (한국어)
- 산업분류(group_code) 기반 키워드 (22개 산업, 250+ 키워드)

### 뉴스 소스 관리 (news_source 테이블)

| 언론사 | 도메인 | 본문 크롤링 |
|--------|--------|-------------|
| 한국경제 | hankyung.com | ✅ 가능 |
| 서울경제 | sedaily.com | ✅ 가능 |
| 이데일리 | edaily.co.kr | ✅ 가능 |
| 뉴스1 | news1.kr | ✅ 가능 |
| 이투데이 | etoday.co.kr | ✅ 가능 |
| 비즈워치 | bizwatch.co.kr | ✅ 가능 |
| 기타 언론사 | - | ❌ RSS snippet 사용 |

- 본문 크롤링 실패 5회 연속 시 자동 비활성화
- 매일 1회 비활성 언론사 재테스트

### 스케줄
- **뉴스 수집**: 10분 간격
- **ETF 영향력 검증**: 매일 장 마감 후 (18:00)

### 2-Step 뉴스-ETF 영향력 분석

```
[Step 1: 뉴스 발행 시점 - 10분 배치]

  1. Google News RSS로 뉴스 수집
  2. 본문 크롤링 가능 언론사 → content 저장
     본문 크롤링 불가 언론사 → RSS snippet 활용
  3. LLM 분석 (뉴스 본문 또는 snippet 입력)
     → keywords: 핵심 키워드 배열 (4~6개)
     → content_summary: AI 요약 (bullets 3개)
     → impact_analysis: 회사/산업 영향 분석 (target_type, impact_score, impact_reason)
  4. 저장
     → news_article: keywords, content_summary (JSONB)
     → news_impact: 뉴스→회사/산업 1차 영향 분석 결과
  5. 관심 ETF 보유 사용자에게 알림 발송

[Step 2: 장 마감 후 - 일 1회 배치]

  1. 뉴스 발행 후 실제 ETF 가격 변동률 조회
  2. 산업 → ETF 매핑 (etf_sector_breakdown 기반)
  3. 영향도 계산 = (산업 관련도 × 0.5) + (실제 변동률 기여도 × 0.5)
  4. 타임라인 텍스트 생성 (LLM)
     - timeline_title: UI 표시용 제목 (20자)
     - timeline_summary: UI 표시용 요약 (50자)
  5. news_etf_influence 저장 (is_verified = TRUE)
```

### LLM 분석 출력 형식

```json
{
  "keywords": ["금리동결", "나스닥", "빅테크", "반도체"],
  "content_summary": {
    "bullets": [
      "미국 연방준비제도(Fed)가 기준금리를 현 수준에서 동결하기로 결정",
      "파월 의장은 인플레이션 둔화세가 뚜렷해질 때까지 신중한 접근 유지 시사",
      "금리 인상 사이클 종료 기대감에 나스닥 등 주요 기술주들이 강세"
    ]
  },
  "impact_analysis": [
    {
      "target_type": "COMPANY",
      "stock_code": "005930",
      "impact_score": 0.72,
      "impact_reason": "AI 반도체 수요 증가로 삼성전자 HBM 사업 수혜 기대"
    },
    {
      "target_type": "INDUSTRY",
      "industry_code": "032601",
      "impact_score": 0.85,
      "impact_reason": "금리 동결로 반도체 업종 투자 심리 개선"
    }
  ]
}
```

### 장점
- **과거 데이터 정확**: 실제 주가 변동 기반으로 신뢰도 높음
- **산업 매핑 재사용**: 뉴스→산업은 한번만, ETF 매핑은 자동
- **검증 가능**: is_verified로 예측 vs 실제 구분

---

## 에러 코드

| 코드 | 에러명 | 설명 |
|------|--------|------|
| NEWS001 | NEWS_NOT_FOUND | 뉴스를 찾을 수 없음 |
| NEWS002 | NEWS_SCRAPE_FAILED | 크롤링 실패 |
| NEWS003 | INVALID_DATE_RANGE | 잘못된 날짜 범위 |
| NEWS004 | INVALID_KEYWORD | 유효하지 않은 검색어 |
| ETF001 | ETF_NOT_FOUND | ETF를 찾을 수 없음 |
