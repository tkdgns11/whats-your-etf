# 뉴스 API (News)

## 기본 정보
- Base URL: `/api/v1/news`
- 인증: 일부 엔드포인트 제외하고 JWT 필요
- 크롤링 서비스: FastAPI (Python) - 네이버 증권 종목뉴스 수집

---

## 뉴스 수집 방식

**네이버 증권 종목뉴스 기반**
- 네이버가 이미 뉴스-종목 매핑을 해놓음 (LLM 분석 불필요)
- 종목별 뉴스 크롤링 → `news_article` + `news_stock_mapping` 저장
- ETF 관련 뉴스 = ETF 구성종목(`etf_stock_composition`)의 뉴스

```
[뉴스 수집 흐름]
1. 네이버 증권 종목 페이지에서 뉴스 링크 추출
2. n.news.naver.com에서 본문 크롤링 (100% 본문 추출 가능)
3. news_article 저장 + news_stock_mapping으로 종목 연결
4. ETF 관련 뉴스 조회 시: etf_stock_composition JOIN news_stock_mapping
```

---

## 엔드포인트 목록

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/` | 최신 뉴스 목록 조회 | X |
| GET | `/{newsId}` | 뉴스 상세 조회 | X |
| GET | `/search` | 뉴스 검색 | X |
| GET | `/etf/{etfId}` | ETF 관련 뉴스 조회 | X |
| GET | `/stock/{stockCode}` | 종목 뉴스 조회 | X |

---

## API 상세

### 1. 최신 뉴스 목록 조회

**Request**
```
GET /api/v1/news?page=0&size=20
```

| Parameter | Type | 필수 | 기본값 | 설명 |
|-----------|------|------|--------|------|
| page | int | X | 0 | 페이지 번호 (0부터 시작) |
| size | int | X | 20 | 페이지 크기 (최대 100) |
| category | string | X | - | 카테고리 필터 (아래 14개 코드 참조) |

**category 코드 (14개)**

| 코드 | 이름 |
|------|------|
| NEWS_SEMI | 반도체 |
| NEWS_IT | IT/전자 |
| NEWS_BIO | 바이오/의약 |
| NEWS_AUTO | 자동차 |
| NEWS_CHEM | 화학/소재 |
| NEWS_ENERGY | 에너지 |
| NEWS_FINANCE | 금융 |
| NEWS_CONSTRUCT | 건설/부동산 |
| NEWS_CONSUMER | 소비재 |
| NEWS_TELECOM | 통신/미디어 |
| NEWS_TRANSPORT | 운송/물류 |
| NEWS_INDUSTRY | 산업재 |
| NEWS_ETC | 기타 |
| NEWS_MARKET | 시장/경제 |

**Response**
```json
{
  "success": true,
  "data": {
    "news": [
      {
        "id": 1,
        "title": "삼성전자, HBM 대규모 수주 성공",
        "source": "한국경제",
        "thumbnailUrl": "https://...",
        "category": "NEWS_SEMI",
        "publishedAt": "2025-01-17T09:30:00Z",
        "relatedStocks": [
          {"stockCode": "005930", "companyName": "삼성전자"}
        ]
      }
    ],
    "page": 0,
    "totalPages": 8,
    "totalElements": 150
  }
}
```

---

### 2. 뉴스 상세 조회

**Request**
```
GET /api/v1/news/{newsId}
```

**Response**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "삼성전자, HBM 대규모 수주 성공",
    "content": "삼성전자가 글로벌 AI 기업으로부터 HBM 대규모 수주에 성공했다...(본문 전체)",
    "source": "한국경제",
    "sourceUrl": "https://n.news.naver.com/...",
    "thumbnailUrl": "https://...",
    "category": "NEWS_SEMI",
    "publishedAt": "2025-01-17T09:30:00Z",
    "relatedStocks": [
      {
        "stockCode": "005930",
        "companyName": "삼성전자",
        "industryGroup": "IT_SEMI"
      }
    ],
    "relatedEtfs": [
      {
        "etfId": 1,
        "ticker": "091160",
        "name": "KODEX 반도체",
        "weightPct": 35.5
      },
      {
        "etfId": 2,
        "ticker": "091170",
        "name": "KODEX 반도체레버리지",
        "weightPct": 35.5
      }
    ]
  }
}
```

> `relatedEtfs`: 해당 종목을 구성종목으로 포함하는 ETF 목록 (비중 순 정렬)

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
GET /api/v1/news/search?keyword=반도체&page=0&size=20
```

| Parameter | Type | 필수 | 설명 |
|-----------|------|------|------|
| keyword | string | O | 검색 키워드 (최소 2자, 최대 50자) |
| page | int | X | 페이지 번호 (기본 0) |
| size | int | X | 페이지 크기 (기본 20) |

**Response**
```json
{
  "success": true,
  "data": {
    "news": [
      {
        "id": 1,
        "title": "삼성전자, HBM 대규모 수주 성공",
        "source": "한국경제",
        "publishedAt": "2025-01-17T09:30:00Z",
        "relatedStocks": [
          {"stockCode": "005930", "companyName": "삼성전자"}
        ]
      }
    ],
    "page": 0,
    "totalPages": 2,
    "totalElements": 25,
    "keyword": "반도체"
  }
}
```

---

### 4. ETF 관련 뉴스 조회

ETF 구성종목들의 뉴스를 조회합니다.

**Request**
```
GET /api/v1/news/etf/{etfId}?size=10
```

| Parameter | Type | 필수 | 설명 |
|-----------|------|------|------|
| etfId | number | O | ETF ID |
| size | int | X | 조회 개수 (기본 10, 최대 50) |

**Response**
```json
{
  "success": true,
  "data": {
    "etf": {
      "id": 1,
      "ticker": "091160",
      "name": "KODEX 반도체"
    },
    "news": [
      {
        "id": 1,
        "title": "삼성전자, HBM 대규모 수주 성공",
        "source": "한국경제",
        "publishedAt": "2025-01-17T09:30:00Z",
        "relatedStock": {
          "stockCode": "005930",
          "companyName": "삼성전자",
          "weightPct": 35.5
        }
      },
      {
        "id": 5,
        "title": "SK하이닉스, 신규 투자 계획 발표",
        "source": "매일경제",
        "publishedAt": "2025-01-17T08:00:00Z",
        "relatedStock": {
          "stockCode": "000660",
          "companyName": "SK하이닉스",
          "weightPct": 28.2
        }
      }
    ],
    "totalCount": 15
  }
}
```

> 구성종목 비중이 높은 종목의 뉴스가 상위에 표시됩니다.

---

### 5. 종목 뉴스 조회

특정 종목의 뉴스를 조회합니다.

**Request**
```
GET /api/v1/news/stock/{stockCode}?size=10
```

| Parameter | Type | 필수 | 설명 |
|-----------|------|------|------|
| stockCode | string | O | 종목 코드 (6자리) |
| size | int | X | 조회 개수 (기본 10, 최대 50) |

**Response**
```json
{
  "success": true,
  "data": {
    "stock": {
      "stockCode": "005930",
      "companyName": "삼성전자"
    },
    "news": [
      {
        "id": 1,
        "title": "삼성전자, HBM 대규모 수주 성공",
        "source": "한국경제",
        "publishedAt": "2025-01-17T09:30:00Z"
      }
    ],
    "totalCount": 25
  }
}
```

---

## 크롤링 시스템 (자동화)

### 데이터 소스
- **네이버 증권 종목뉴스** (finance.naver.com)
- 네이버가 이미 뉴스-종목 매핑을 해놓음

### 크롤링 흐름

```
1. 종목 메인 페이지에서 뉴스 링크 추출
   GET finance.naver.com/item/main.naver?code={stockCode}

2. news_read.naver → redirect URL 추출

3. n.news.naver.com에서 본문 추출
   - 제목, 본문, 언론사, 썸네일, 발행일
   - 100% 본문 추출 가능

4. DB 저장
   - news_article: 뉴스 정보
   - news_stock_mapping: 뉴스-종목 연결
```

### 스케줄 (APScheduler)

| 작업 | 주기 | 대상 |
|------|------|------|
| ETF 구성종목 뉴스 | 30분 | 상위 100개 ETF + 관심 ETF + 포트폴리오 ETF 구성종목 |
| KRX 공시 체크 | 매일 09:00 | ETF 상장/폐지/리밸런싱 공시 |

### ETF 관련 뉴스 조회 방식

```sql
-- ETF 관련 뉴스 조회 쿼리 예시
SELECT na.*, ci.stock_code, ci.company_name, esc.weight_pct
FROM news_article na
JOIN news_stock_mapping nsm ON na.id = nsm.news_id
JOIN company_info ci ON nsm.company_id = ci.id
JOIN etf_stock_composition esc ON ci.id = esc.company_id
WHERE esc.etf_id = {etfId}
ORDER BY esc.weight_pct DESC, na.published_at DESC
```

---

## 에러 코드

| 코드 | 에러명 | 설명 |
|------|--------|------|
| NEWS001 | NEWS_NOT_FOUND | 뉴스를 찾을 수 없음 |
| NEWS002 | INVALID_KEYWORD | 유효하지 않은 검색어 (2~50자) |
| ETF001 | ETF_NOT_FOUND | ETF를 찾을 수 없음 |
| STOCK001 | STOCK_NOT_FOUND | 종목을 찾을 수 없음 |
