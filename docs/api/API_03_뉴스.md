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
        "contentSummary": "외국인이 반도체 관련 ETF를 5거래일 연속 순매수하며...",
        "source": "한국경제",
        "sourceUrl": "https://news.google.com/...",
        "thumbnailUrl": "https://...",
        "category": "ETF",
        "keywords": ["반도체", "ETF", "외국인"],
        "publishedAt": "2025-01-17T09:30:00Z",
        "viewCount": 125
      },
      {
        "id": 2,
        "title": "금리 인하 기대감에 채권 ETF 강세",
        "contentSummary": "미국 연준의 금리 인하 시그널에 채권형 ETF가...",
        "source": "매일경제",
        "sourceUrl": "https://news.google.com/...",
        "thumbnailUrl": null,
        "category": "금융",
        "keywords": ["금리", "채권", "ETF"],
        "publishedAt": "2025-01-17T08:45:00Z",
        "viewCount": 89
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
    "contentSummary": "외국인이 반도체 관련 ETF를 5거래일 연속 순매수하며...",
    "source": "한국경제",
    "sourceUrl": "https://news.google.com/...",
    "thumbnailUrl": "https://...",
    "category": "ETF",
    "keywords": ["반도체", "ETF", "외국인"],
    "publishedAt": "2025-01-17T09:30:00Z",
    "viewCount": 126,
    "relatedEtfs": [
      {
        "ticker": "091160",
        "name": "KODEX 반도체",
        "influenceScore": 0.85,
        "influenceType": "POSITIVE"
      },
      {
        "ticker": "091170",
        "name": "KODEX 반도체레버리지",
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
        "contentSummary": "외국인이 반도체 관련 ETF를 5거래일 연속 순매수하며...",
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
        "contentSummary": "외국인이 반도체 관련 ETF를 5거래일 연속...",
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

### 6. 하이라이트 뉴스 (맵 뷰용)
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

### 7. 수동 크롤링 트리거 (관리자)

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

### 크롤링 키워드
```
ETF, 반도체+ETF, 2차전지+ETF, AI+ETF, 배당+ETF,
금리+인하, 금리+인상, KOSPI, KOSDAQ, 미국+증시,
나스닥, S&P500, 채권+금리, 환율+원달러, 원자재+금,
원유+가격, 인플레이션, 삼성전자, SK하이닉스, 테슬라
```

### 스케줄
- **실행 주기**: 10분 간격
- **처리량**: 키워드당 최대 5개 뉴스

### 뉴스-ETF 영향력 분석
1. 뉴스 수집 후 LLM을 통해 관련 ETF 분석
2. 영향력 점수 (0.0 ~ 1.0) 및 유형 (POSITIVE/NEGATIVE/NEUTRAL) 산출
3. 분석 근거 텍스트 저장

---

## 에러 코드

| 코드 | 설명 |
|------|------|
| NEWS_NOT_FOUND | 뉴스를 찾을 수 없음 |
| ETF_NOT_FOUND | ETF를 찾을 수 없음 |
| SCRAPE_FAILED | 크롤링 실패 |
| INVALID_KEYWORD | 유효하지 않은 검색어 |
