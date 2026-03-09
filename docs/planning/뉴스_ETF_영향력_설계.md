# 뉴스-ETF 영향력 분석 설계

> ⚠️ **문서 폐기 안내** (2025-03-08)
>
> 이 문서는 **기존 LLM 기반 뉴스 분석 방식**을 설명합니다.
> 현재 시스템은 **네이버 증권 종목뉴스 방식**으로 변경되었습니다.
>
> **현재 방식:**
> - 네이버가 이미 뉴스-종목 매핑을 해놓음 (LLM 분석 불필요)
> - `news_stock_mapping` 테이블 사용
> - ETF 관련 뉴스 = ETF 구성종목(`etf_stock_composition`)의 뉴스
>
> **참고 문서:**
> - `docs/api/API_03_뉴스.md` - 현재 뉴스 API 명세
> - `docs/planning/WhatsYourETF_기획안.md` 4.3절 - 현재 뉴스 설계
>
> 아래 내용은 **참고용으로만 보존**합니다.

---

# [폐기됨] 기존 LLM 기반 뉴스 분석 설계

> 뉴스 기사가 어떤 회사/산업/ETF에 영향을 미치는지 분석하는 기능 설계 문서

---

## 1. 개요

### 1.1 목적

- **뉴스 상세 페이지**에서 "이 뉴스와 관련된 ETF" 섹션 제공
- **ETF 상세 페이지**에서 "관련 뉴스" + "주요 이슈 타임라인" 제공
- 과거 뉴스는 **실제 주가 데이터 기반**으로 정확한 영향도 제공

### 1.2 핵심 설계 원칙

| 원칙 | 설명 |
|------|------|
| **All-LLM 분석** | GPT-4o가 직접 모든 뉴스 분석 (품질 우선) |
| **Spam Filter** | Whitelist + Blacklist 키워드로 명백한 스팸 제거 |
| **ETF 직접 매핑** | 뉴스 → ETF 직접 연결 (news_etf_influence) |
| **타임라인 검증** | ETF 타임라인은 장 마감 후 검증된 데이터만 표시 |
| **키워드/요약 저장** | news_article에 keywords, content_summary 저장 |

### 1.3 왜 All-LLM인가?

| 방식 | 장점 | 단점 |
|------|------|------|
| **Score-based 룰필터 + LLM** | 비용 절감 | 복잡한 구현, 엣지케이스 놓침, 유지보수 부담 |
| **All-LLM (GPT-4o)** | 높은 품질, 단순 구현, 맥락 이해 | 비용 ~$12/월 |

**SSAFY 7주 프로젝트에서 $12/월은 감당 가능하고, 품질이 더 중요함.**

### 1.4 구현 파일 구조

```
app/
├── models/
│   ├── news.py              # NewsArticle 모델
│   ├── news_impact.py       # NewsImpact 모델 (1:N 영향 매핑)
│   └── company.py           # CompanyInfo, IndustryClassification
├── services/
│   ├── news_impact_analyzer.py  # GPT-4o 분석 서비스 (Constrained LLM)
│   └── llm_service.py           # OpenAI API 래퍼
├── scrapers/
│   ├── google_scraper.py    # Google News RSS 크롤러
│   ├── naver_scraper.py     # Naver News API 크롤러
│   ├── content_scraper.py   # 본문 + 썸네일(og:image) 크롤러
│   └── keywords.py          # 14개 카테고리 + 키워드 매핑
├── utils/
│   └── spam_filter.py       # Spam Filter (Whitelist/Blacklist)
└── schedulers/
    └── scheduler.py         # APScheduler 스케줄러
```

### 1.5 뉴스 카테고리 (14개)

| 코드 | 이름 | 키워드 예시 |
|------|------|-------------|
| NEWS_SEMI | 반도체 | 반도체, HBM, 파운드리, 메모리 |
| NEWS_IT | IT/전자 | IT, 전자, 소프트웨어, AI |
| NEWS_BIO | 바이오/의약 | 바이오, 제약, 신약 |
| NEWS_AUTO | 자동차 | 자동차, 전기차, 배터리 |
| NEWS_CHEM | 화학/소재 | 화학, 철강, 소재 |
| NEWS_ENERGY | 에너지 | 에너지, 태양광, 원자력 |
| NEWS_FINANCE | 금융 | 금융, 은행, 보험 |
| NEWS_CONSTRUCT | 건설/부동산 | 건설, 부동산, 인프라 |
| NEWS_CONSUMER | 소비재 | 소비재, 유통, 식품 |
| NEWS_TELECOM | 통신/미디어 | 통신, 미디어, 엔터 |
| NEWS_TRANSPORT | 운송/물류 | 운송, 물류, 항공 |
| NEWS_INDUSTRY | 산업재 | 기계, 조선, 방산 |
| NEWS_ETC | 기타 | (매칭 안됨) |
| NEWS_MARKET | 시장/경제 | 금리, 환율, 코스피, 연준 |

### 1.6 화면 예시

**뉴스 상세 - 관련 ETF (실시간)**
```
┌─────────────────────────────────────────┐
│ [뉴스 상세]                              │
│                                         │
│ #금리동결 #나스닥 #빅테크 #반도체         │
│                                         │
│ "미국 연준 금리 동결 발표, 기술주 강세"    │
│ 2024-01-15 | 한국경제                    │
│                                         │
│ ┌─ AI 핵심 요약 ───────────────────────┐ │
│ │ • 연준이 기준금리를 현 수준에서 동결   │ │
│ │ • 파월 의장, 신중한 접근 유지 시사     │ │
│ │ • 기술주 중심 강한 매수세             │ │
│ └─────────────────────────────────────┘ │
│                                         │
├─────────────────────────────────────────┤
│ 관련 ETF 추천                 ← 실시간   │
│ ┌─────────────────────────────────────┐ │
│ │ KODEX 반도체      +1.24%  ● 긍정적  │ │
│ │ TIGER 반도체      +1.56%  ● 긍정적  │ │
│ └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘

※ 등락률은 실시간 시세 (장중) 또는 전일 종가 대비 (장 시작 전)
```

---

## 2. 전체 파이프라인

```
┌─────────────────────────────────────────────────────────────┐
│  경제 뉴스 수집                                               │
│  [메인] Naver News API (naver_scraper.py)                    │
│    - n.news.naver.com URL → 본문 크롤링 성공률 높음 (15개)    │
│    - originallink → 언론사명 추출                            │
│  [보조] Google News RSS (google_scraper.py)                  │
│    - 본문 크롤링 가능한 6개 언론사만 사용                      │
│  - 정기 크롤링: 10분마다 / 전체 크롤링: 1시간마다              │
│  - 키워드별 카테고리 자동 할당 (14개)                         │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  Spam Filter (spam_filter.py)                               │
│  - Whitelist: 경제 키워드 있으면 무조건 통과                  │
│  - Blacklist: 스포츠/연예/날씨/광고 등 스팸 제거              │
│  - 패턴 매칭: [광고], [스포츠] 등                            │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  본문 크롤링 + 썸네일 추출 (content_scraper.py)               │
│  - Naver News: 15개 언론사 (n.news.naver.com 통합 구조)       │
│  - Google News: 6개 언론사 (직접 크롤링 가능한 곳만)          │
│  - 썸네일: og:image / twitter:image 메타태그 추출             │
│  - ⚠️ 본문 크롤링 실패 시 → LLM 분석 대상 제외               │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  GPT-4o 분석 (NewsAnalyzer)                                  │
│  - 대상: 본문(content)이 있는 뉴스만                          │
│  - 입력: 뉴스 제목/본문                                      │
│  - 출력: keywords[] + content_summary (bullets)              │
│  - 비용: ~$0.40/일 ≈ $12/월                                 │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  DB 저장                                                    │
│  - news_article: 뉴스 기본 정보 + content_summary + keywords │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  장 마감 후 ETF 영향 검증                                     │
│  - 뉴스 카테고리/키워드 기반 관련 ETF 매핑                    │
│  - 실제 ETF 주가 변동률 확인                                  │
│  - news_etf_influence 저장 (is_verified=TRUE)                │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  알림 발송                                                   │
│  - user_holding_etf → etf → news_etf_influence              │
│  - 관련 사용자에게 알림                                      │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. 썸네일 추출

### 3.1 추출 방식

본문 크롤링 시 `og:image` 메타태그에서 썸네일 URL 추출:

```python
def _extract_og_image(self, html: str) -> Optional[str]:
    """Open Graph 이미지 추출"""
    soup = BeautifulSoup(html, 'html.parser')

    # 1순위: og:image
    og_image = soup.find('meta', property='og:image')
    if og_image and og_image.get('content'):
        return og_image['content']

    # 2순위: twitter:image
    twitter_image = soup.find('meta', attrs={'name': 'twitter:image'})
    if twitter_image and twitter_image.get('content'):
        return twitter_image['content']

    return None
```

### 3.2 저장 필드

| 테이블 | 컬럼 | 타입 |
|--------|------|------|
| news_article | thumbnail_url | VARCHAR(1000) |

### 3.3 처리 흐름

```
content_scraper.extract_content(url)
    │
    ├── 본문 추출 → news_article.content
    │
    └── 썸네일 추출 → news_article.thumbnail_url
            │
            └── og:image / twitter:image 메타태그
```

---

## 4. Spam Filter

> 명백한 스팸만 제거하고, 나머지는 GPT-4o가 판단

### 4.1 구현 (`app/utils/spam_filter.py`)

```python
# 스팸 키워드 (Blacklist)
SPAM_KEYWORDS = [
    # 비경제 카테고리
    "연예", "스포츠", "날씨", "운세", "로또", "축구", "야구", "농구",
    "배구", "골프", "올림픽", "월드컵", "아이돌", "드라마", "예능",
    # 광고성/이벤트
    "무료체험", "할인쿠폰", "이벤트당첨", "경품", "추첨",
    # 생활/잡다
    "맛집", "레시피", "요리", "다이어트", "뷰티", "패션",
]

# 스팸 패턴 (정규식)
SPAM_PATTERNS = [
    r"^\[광고\]",          # [광고]로 시작
    r"^\[PR\]",            # [PR]로 시작
    r"^\[스포츠\]",        # [스포츠]로 시작
    r"^\[연예\]",          # [연예]로 시작
    r"\d+% ?할인",         # 50% 할인
    r"무료 ?배송",         # 무료 배송
]

# 경제/투자 관련 키워드 (Whitelist - 있으면 무조건 통과)
WHITELIST_KEYWORDS = [
    # 시장/지수
    "코스피", "코스닥", "나스닥", "다우", "S&P", "니케이",
    # 금융
    "금리", "기준금리", "연준", "Fed", "FOMC", "통화정책", "인플레이션",
    "환율", "달러", "원화", "엔화", "유로",
    # 산업
    "반도체", "2차전지", "배터리", "바이오", "AI", "인공지능",
    "전기차", "자율주행", "태양광", "풍력", "원자력",
    # 투자
    "ETF", "펀드", "주식", "채권", "배당", "실적", "어닝",
    "공모주", "IPO", "상장", "매출", "영업이익", "순이익",
    # 회사/기관
    "삼성", "SK", "LG", "현대", "기아", "네이버", "카카오",
    "한국은행", "금융위", "금감원",
]

def is_spam(title: str, description: str = None) -> FilterResult:
    """
    스팸 판단 로직:
    1. Whitelist 체크 → 있으면 무조건 통과
    2. Spam 패턴 체크 → 매칭되면 스팸
    3. Spam 키워드 체크 → 매칭되면 스팸
    4. 기본: 스팸 아님 (GPT-4o가 판단)
    """
```

### 4.2 왜 Whitelist가 필요한가?

| 케이스 | 제목 | Blacklist만 | Whitelist 포함 |
|--------|------|------------|----------------|
| 정상 | "삼성전자 야구장 건설 계획 발표" | ❌ 스팸 (야구) | ✅ 통과 (삼성) |
| 정상 | "손흥민 연봉 협상" | ❌ 스팸 (연봉=?) | ❌ 스팸 |
| 스팸 | "[스포츠] 월드컵 예선" | ❌ 스팸 | ❌ 스팸 |

**Whitelist 우선순위로 경제 뉴스 놓치지 않음. 애매한 건 GPT-4o가 판단.**

### 4.3 news_scraper.py 연동

```python
from app.utils.spam_filter import is_spam

async def _parse_item(self, item, keyword: str) -> Optional[NewsArticle]:
    # ... 파싱 ...

    # Spam Filter 체크
    spam_result = is_spam(title, description)
    if spam_result.is_spam:
        logger.debug(f"[Spam] {title[:50]}... ({spam_result.reason})")
        return None

    return NewsArticle(...)
```

### 4.4 수집 통계 반환

```python
async def scrape_by_keyword(self, keyword: str, max_items: int = 5) -> dict:
    """
    Returns:
        {"saved": int, "spam": int, "duplicate": int}
    """
```

---

## 5. GPT-4o 분석 (뉴스 요약 및 키워드 추출)

### 5.1 핵심 원칙

| 원칙 | 설명 |
|------|------|
| **요약 중심** | 뉴스 핵심 내용을 3개 bullet point로 요약 |
| **키워드 추출** | 투자 관련 핵심 키워드 4~6개 추출 |
| **JSON 출력** | 구조화된 형식으로 출력하여 파싱 용이 |

### 5.2 프롬프트 예시

> ai_prompt 테이블의 'news_analysis' 프롬프트 참조

```python
# 출력 형식 (JSON)
{
  "keywords": ["금리동결", "나스닥", "빅테크", "반도체"],
  "content_summary": {
    "bullets": [
      "핵심 사실 요약 (50자 내외)",
      "영향 및 의미 설명 (50자 내외)",
      "향후 전망 또는 투자 시사점 (50자 내외)"
    ]
  },
  "industry_influence": [
    {"group_code": "IT_SEMI", "relevance": 0.85, "sentiment": "POSITIVE"},
    {"group_code": "IT_ELEC", "relevance": 0.60, "sentiment": "POSITIVE"}
  ]
}
```

### 5.3 ETF 영향도 매핑 흐름

```
뉴스: "TSMC HBM 생산 확대, 삼성전자 추격 어려워"
          │
          ▼ GPT-4o 분석
┌─────────────────────────────────────┐
│ news_article 업데이트                │
│ ├─ keywords: ["HBM", "TSMC", ...]  │
│ └─ content_summary: {bullets: [...]}│
│ └─ industry_influence (참고용)       │
└─────────────────────────────────────┘
          │
          ▼ 장 마감 후 ETF 매핑 (별도 배치)
┌─────────────────────────────────────┐
│ news_etf_influence                   │
│ ├─ etf_id: 45 (반도체 ETF)           │
│ │   influence_type: POSITIVE        │
│ │   actual_change_rate: +1.24%      │
│ └─ is_verified: TRUE                │
└─────────────────────────────────────┘
```

**포인트:**
- 뉴스 분석 시점에는 keywords, summary만 저장
- ETF 영향도는 장 마감 후 실제 주가 데이터로 검증 후 저장
- news_etf_influence는 검증된 데이터만 표시

---

## 6. 테이블 구조

### 6.1 news_article (뉴스 기본 정보 + AI 분석 결과)

```sql
CREATE TABLE "news_article" (
    "id" BIGSERIAL PRIMARY KEY,
    "title" VARCHAR(500) NOT NULL,
    "content" TEXT,                               -- 뉴스 본문 전체
    "content_summary" JSONB,                      -- AI 핵심 요약 {"bullets": ["...", "...", "..."]}
    "source" VARCHAR(100),                        -- 언론사명
    "source_url" VARCHAR(1000) NOT NULL UNIQUE,   -- 원본 URL
    "thumbnail_url" VARCHAR(1000),
    "category_code" VARCHAR(30),                  -- category FK (NEWS_MACRO, NEWS_SEMI 등)
    "keywords" JSONB,                             -- 키워드 태그 ["금리동결", "나스닥", "빅테크"]
    "published_at" TIMESTAMP,
    "view_count" INTEGER DEFAULT 0,
    "is_active" BOOLEAN DEFAULT TRUE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_news_category" FOREIGN KEY ("category_code") REFERENCES "category"("code")
);

-- 인덱스
CREATE INDEX idx_news_published ON news_article(published_at DESC);
CREATE INDEX idx_news_category ON news_article(category_code);
CREATE INDEX idx_news_keywords ON news_article USING GIN(keywords);
```

### 6.2 news_etf_influence (검증된 ETF 영향)

```sql
CREATE TABLE "news_etf_influence" (
    "id" BIGSERIAL PRIMARY KEY,
    "news_id" BIGINT NOT NULL,
    "etf_id" BIGINT NOT NULL,

    -- 영향력 점수
    "influence_score" DECIMAL(5,4),               -- 0.0000 ~ 1.0000
    "influence_type" VARCHAR(20),                 -- POSITIVE / NEGATIVE / NEUTRAL

    -- 타임라인용 (UI 표시)
    "timeline_title" VARCHAR(100),                -- "연준 기준금리 동결 발표"
    "timeline_summary" VARCHAR(200),              -- "시장 예상치 부합, 기술주 중심 반등세"

    -- 상세 분석
    "analysis_reason" TEXT,                       -- 상세 분석 근거

    -- 실제 데이터 기반 검증
    "actual_change_rate" DECIMAL(8,4),            -- 뉴스 발행 후 ETF 실제 변동률
    "verified_at" TIMESTAMP,                      -- 검증 시점 (장 마감 후)
    "is_verified" BOOLEAN DEFAULT FALSE,          -- 실제 데이터로 검증됨

    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "fk_news_influence_news" FOREIGN KEY ("news_id")
        REFERENCES "news_article"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_news_influence_etf" FOREIGN KEY ("etf_id")
        REFERENCES "etf"("id") ON DELETE CASCADE,
    CONSTRAINT "uk_news_etf" UNIQUE ("news_id", "etf_id")
);
```

### 6.3 테이블 관계

```
┌─────────────┐
│ news_article│
├─────────────┤
│ id (PK)     │
│ title       │
│ content     │
│ keywords    │ ← LLM 생성 (JSONB)
│ content_summary │ ← LLM 생성 (JSONB bullets)
│ category_code │ ← 뉴스 카테고리 (NEWS_SEMI 등)
└──────┬──────┘
       │
       │ 1:N (장 마감 후 ETF 매핑)
       ▼
┌─────────────────────┐
│ news_etf_influence  │
├─────────────────────┤
│ news_id (FK)        │
│ etf_id (FK)         │───────► etf
│ influence_score     │ ← 0.0 ~ 1.0
│ influence_type      │ ← POSITIVE / NEGATIVE / NEUTRAL
│ timeline_title      │ ← 타임라인 제목
│ timeline_summary    │ ← 타임라인 요약
│ actual_change_rate  │ ← 실제 ETF 변동률
│ is_verified         │ ← 검증 여부
└─────────────────────┘
```

**핵심 변경점:**
- `news_impact` 테이블 제거 → 직접 `news_etf_influence`로 매핑
- 뉴스 분석 결과는 `news_article.keywords`, `news_article.content_summary`에 저장
- ETF 영향도는 장 마감 후 실제 주가 데이터 기반으로 검증

### 6.4 news_etf_influence 장중/장외 처리 흐름

> news_impact는 뉴스 수집 즉시 생성되지만, news_etf_influence는 **주가 데이터가 필요**하므로 장 운영 시간에 따라 처리가 달라집니다.

#### 처리 흐름 요약

| 뉴스 발행 시점 | INSERT 시점 | UPDATE (검증) 시점 |
|---------------|-------------|-------------------|
| **장중** (09:00~15:30) | 즉시 (현재가 기준) | 당일 장 마감 후 (15:30~) |
| **장 외** (15:30~익일 09:00) | 익일 장 시작 후 (09:00~) | 익일 장 마감 후 (15:30~) |
| **주말/공휴일** | 다음 거래일 장 시작 후 | 다음 거래일 장 마감 후 |

#### 상세 흐름

```
[Case 1: 장중 뉴스]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

09:00                    09:30                    15:30
  │                        │                        │
  ├── 장 시작 ─────────────┼────────────────────────┤── 장 마감
                           │                        │
                       뉴스 발행                     │
                           │                        │
                           ▼                        │
                    ┌─────────────┐                 │
                    │ INSERT      │                 │
                    │ - 현재가 기준│                 │
                    │ - is_verified=FALSE           │
                    └─────────────┘                 │
                                                    ▼
                                             ┌─────────────┐
                                             │ UPDATE      │
                                             │ - 종가 기준  │
                                             │ - actual_change_rate 계산
                                             │ - is_verified=TRUE
                                             └─────────────┘


[Case 2: 장 외 뉴스]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

15:30          22:00                09:00                15:30
  │              │                    │                    │
  ├── 장 마감 ───┼────────────────────┼── 익일 장 시작 ────┤── 익일 장 마감
                 │                    │                    │
             뉴스 발행                 │                    │
                 │                    │                    │
                 │ (대기)             ▼                    │
                 │             ┌─────────────┐             │
                 │             │ INSERT      │             │
                 │             │ - 시가 기준  │             │
                 │             │ - is_verified=FALSE       │
                 │             └─────────────┘             │
                 │                                         ▼
                 │                                  ┌─────────────┐
                 │                                  │ UPDATE      │
                 │                                  │ - 종가 기준  │
                 │                                  │ - actual_change_rate
                 │                                  │ - is_verified=TRUE
                 │                                  └─────────────┘


[Case 3: 주말/공휴일 뉴스]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

금요일 15:30        토요일 10:00        월요일 09:00        월요일 15:30
     │                  │                   │                   │
     ├── 장 마감 ───────┼───────────────────┼── 장 시작 ────────┤── 장 마감
                        │                   │                   │
                    뉴스 발행               │                   │
                        │                   │                   │
                        │ (대기)            ▼                   │
                        │            ┌─────────────┐            │
                        │            │ INSERT      │            │
                        │            │ - 시가 기준  │            │
                        │            └─────────────┘            │
                        │                                       ▼
                        │                                ┌─────────────┐
                        │                                │ UPDATE      │
                        │                                │ - 종가 기준  │
                        │                                └─────────────┘
```

#### 스케줄러 작업

| 작업 | 실행 시점 | 설명 |
|------|----------|------|
| `insert_etf_influence_job` | 장 시작 후 (09:05) | 장 외 발행 뉴스 중 미처리 건 INSERT |
| `update_etf_influence_job` | 장 마감 후 (15:35) | is_verified=FALSE 건 UPDATE |

#### 구현 예시

```python
async def insert_etf_influence_job():
    """장 시작 후: 미처리 뉴스 → ETF 영향 INSERT"""
    # 1. news_impact 있지만 news_etf_influence 없는 뉴스 조회
    # 2. news_impact → etf_compositions 조인 → ETF 매핑
    # 3. 현재 시가로 influence_score 계산
    # 4. INSERT (is_verified=FALSE)
    pass

async def update_etf_influence_job():
    """장 마감 후: 미검증 건 → 실제 변동률로 UPDATE"""
    # 1. is_verified=FALSE인 news_etf_influence 조회
    # 2. 종가 대비 actual_change_rate 계산
    # 3. UPDATE (is_verified=TRUE, verified_at=NOW())
    pass
```

#### 뉴스 상세 페이지 "관련 ETF" 표시

| 데이터 소스 | 사용 시점 | 설명 |
|------------|----------|------|
| `news_impact` → ETF 유추 | 항상 | 회사/산업 기반으로 관련 ETF 표시 |
| `news_etf_influence` | INSERT 후 | 실제 주가 기반 영향도 표시 |

> 장 외 뉴스도 `news_impact` 기반으로 "관련 ETF"는 즉시 표시 가능.
> `news_etf_influence`는 실제 주가 데이터 기반의 정확한 영향도 제공용.

---

## 7. 스케줄러 (`app/schedulers/scheduler.py`)

### 7.1 스케줄 구성

| 작업 | 주기 | 설명 |
|------|------|------|
| **정기 크롤링** | 10분 | 우선순위 키워드 뉴스 수집 |
| **전체 크롤링** | 1시간 | 모든 키워드 뉴스 수집 |
| **AI 분석** | 15분 | 미분석 뉴스 GPT-4o 분석 |
| **ETF 검증** | 매일 16:00 | 장 마감 후 영향도 검증 |
| **KRX 공시** | 매일 09:00 | KIND 공시 체크 |

### 7.2 뉴스 수집 작업

```python
async def scrape_news_job():
    """뉴스 크롤링 스케줄 작업 (10분마다)"""
    logger.info("=== 정기 뉴스 크롤링 시작 ===")

    db = SessionLocal()
    service = NewsCollectionService(db)

    try:
        result = await service.collect_all(enrich_content=True)
        logger.info(
            f"=== 정기 뉴스 크롤링 완료 ===\n"
            f"  Google: {result['google_count']}건\n"
            f"  Naver: {result['naver_count']}건\n"
            f"  본문보강: {result['content_enriched']}건\n"
            f"  총: {result['total']}건"
        )

        # 뉴스 수집 후 AI 분석 트리거
        if result['total'] > 0:
            await analyze_news_job()

    except Exception as e:
        logger.error(f"뉴스 크롤링 실패: {e}")
    finally:
        await service.close()
        db.close()
```

### 7.3 GPT-4o 분석 작업

```python
async def analyze_news_job():
    """뉴스 AI 분석 작업 (수집 후 자동 실행 또는 15분마다)"""
    logger.info("=== 뉴스 AI 분석 시작 ===")

    if not settings.openai_api_key:
        logger.warning("OpenAI API 키 미설정 - AI 분석 건너뜀")
        return

    db = SessionLocal()
    analyzer = NewsImpactAnalyzer(db)

    try:
        # 최근 1시간 내 미분석 뉴스 분석
        result = await analyzer.analyze_recent(hours=1, limit=30)
        logger.info(
            f"=== 뉴스 AI 분석 완료 ===\n"
            f"  대상: {result['total']}건\n"
            f"  성공: {result['success']}건\n"
            f"  영향없음: {result['no_impact']}건\n"
            f"  실패: {result['failed']}건"
        )

        # AI 분석 완료 후 ETF 매핑 트리거
        if result['success'] > 0:
            await map_news_etf_job()

    except Exception as e:
        logger.error(f"뉴스 AI 분석 실패: {e}")
    finally:
        await analyzer.close()
        db.close()
```

### 7.4 분석 결과 저장

```python
def _save_analysis(self, news: NewsArticle, result: AnalysisResult):
    """분석 결과를 news_article에 저장"""

    # keywords 저장
    if result.keywords:
        news.keywords = result.keywords

    # content_summary 저장
    if result.summary:
        news.content_summary = {"bullets": result.summary}

    self.db.commit()
```

### 7.5 ETF 영향 매핑 (별도 배치 작업)

```python
async def map_news_to_etf_job():
    """장 마감 후: 뉴스 → ETF 영향 매핑"""

    # 1. 오늘 발행된 미매핑 뉴스 조회
    # 2. 뉴스 카테고리/키워드 기반 관련 ETF 탐색
    # 3. 실제 ETF 주가 변동률 계산
    # 4. news_etf_influence INSERT (is_verified=TRUE)
    pass
```

### 7.5 ETF 영향도 검증 (매일 16:00)

```python
async def verify_news_etf_job():
    """뉴스-ETF 검증 작업 (장 마감 후)"""
    # news_impact → etf_compositions 조인 → ETF 매핑
    # 실제 주가 데이터로 영향도 검증
    # news_etf_influence 테이블에 저장
```

---

## 8. 사용자 관련 뉴스 조회

### 8.1 내 포트폴리오 관련 뉴스 쿼리

```sql
-- 내 보유 ETF 관련 뉴스 (ETF 영향 기반)
SELECT DISTINCT
    n.id,
    n.title,
    n.content_summary,
    n.thumbnail_url,
    n.published_at,
    n.keywords,
    nei.influence_score,
    nei.influence_type,
    nei.actual_change_rate
FROM news_article n
JOIN news_etf_influence nei ON n.id = nei.news_id
WHERE nei.is_verified = TRUE
  AND nei.etf_id IN (
    SELECT etf_id FROM user_holding_etf WHERE user_id = :user_id
  )
ORDER BY n.published_at DESC
LIMIT 20;
```

### 8.2 카테고리 기반 관련 뉴스 쿼리 (실시간)

```sql
-- 내 보유 ETF 섹터와 관련된 뉴스 (검증 전 실시간)
SELECT DISTINCT
    n.id,
    n.title,
    n.content_summary,
    n.thumbnail_url,
    n.published_at,
    n.keywords
FROM news_article n
WHERE n.category_code IN (
    -- ETF 섹터 → 뉴스 카테고리 매핑
    SELECT DISTINCT
        CASE esc.group_code
            WHEN 'IT_SEMI' THEN 'NEWS_SEMI'
            WHEN 'IT_ELEC' THEN 'NEWS_IT'
            WHEN 'BIO' THEN 'NEWS_BIO'
            WHEN 'AUTO' THEN 'NEWS_AUTO'
            WHEN 'FINANCE' THEN 'NEWS_FINANCE'
            -- ... 기타 매핑
        END
    FROM user_holding_etf uhe
    JOIN etf_sector_cluster esc ON uhe.etf_id = esc.etf_id
    WHERE uhe.user_id = :user_id
      AND esc.weight_pct >= 10  -- 10% 이상 비중인 섹터만
)
ORDER BY n.published_at DESC
LIMIT 20;
```

### 8.3 API 응답 예시

```json
GET /api/v1/news/my-portfolio?limit=10

{
  "news": [
    {
      "id": 123,
      "title": "삼성전자, AI 반도체 대규모 투자 발표",
      "summary": [
        "삼성전자가 AI 반도체에 10조원 투자 발표",
        "2025년까지 생산능력 2배 확대 계획",
        "경쟁사 대비 기술 격차 확대 전망"
      ],
      "keywords": ["AI반도체", "삼성전자", "투자확대"],
      "thumbnailUrl": "https://...",
      "publishedAt": "2025-01-17T09:30:00Z",
      "relatedEtfs": [
        {"id": 45, "name": "KODEX 반도체", "changeRate": 1.24, "influenceType": "POSITIVE"}
      ]
    }
  ]
}
```

---

## 9. 비용 추정

| 항목 | 수치 |
|------|------|
| 하루 경제 뉴스 수집 | ~100건 |
| Spam Filter 통과 | ~90건 |
| GPT-4o 분석 | ~90건 |
| LLM 토큰 (입력) | ~500 토큰/건 (제목 + 본문 snippet) |
| LLM 토큰 (출력) | ~200 토큰/건 |
| GPT-4o 비용 | 입력 $2.5/1M, 출력 $10/1M |
| **예상 일일 비용** | **~$0.40/일 ≈ $12/월** |

### 9.1 비용 최적화 팁

```
1. 본문 전체 대신 RSS snippet + 제목만 전달 (500 토큰 vs 2000 토큰)
2. 회사/산업 목록은 캐싱해서 재사용
3. 배치 처리로 API 호출 횟수 최소화 (10건씩)
```

---

## 10. 품질 보장 전략

### 10.1 Hallucination 방지

| 전략 | 설명 |
|------|------|
| **Constrained Selection** | LLM이 우리 DB 목록에서만 선택 |
| **JSON 출력 강제** | 파싱 가능한 구조화된 출력 |
| **Ground Truth 기반** | 모든 결과가 DB에 존재하는 값 |

### 10.2 일관성 보장

| 전략 | 설명 |
|------|------|
| **정해진 영향도 범위** | -1.0 ~ +1.0 숫자로 정량화 |
| **정해진 분류 체계** | industry_classification 기반 |
| **검증 프로세스** | 장 마감 후 실제 주가로 검증 |

### 10.3 검증 가능성

```
모든 분석 결과 추적 가능:
- news_impact.impact_score: LLM 예측 영향도
- news_etf_influence.actual_change_rate: 실제 ETF 변동률
- 예측 vs 실제 비교로 LLM 정확도 측정 가능
```

---

## 11. 요약

| 항목 | 값 |
|------|-----|
| **수집 방식** | [메인] Naver News API + [보조] Google News RSS |
| **카테고리** | 14개 (투자테마 13 + 시장/경제) - `keywords.py` |
| **본문 크롤링** | Naver 15개 + Google 6개 (본문 없으면 LLM 분석 제외) |
| **썸네일 추출** | og:image / twitter:image 메타태그 |
| **Spam Filter** | Whitelist + Blacklist + 패턴 매칭 (`spam_filter.py`) |
| **분석 방식** | All-LLM (GPT-4o가 직접 분석) |
| **분석 서비스** | `NewsAnalyzer` (키워드/요약 추출) |
| **분석 결과 저장** | news_article.keywords, news_article.content_summary |
| **ETF 영향 매핑** | 장 마감 후 검증 → news_etf_influence |
| **스케줄러** | APScheduler (정기10분, 전체1시간, AI15분, ETF검증16:00) |
| **월 비용** | ~$12 |
| **장점** | 단순 구현, 높은 품질, 검증된 ETF 영향도 제공 |

---

## 12. 테스트

### 12.1 Spam Filter 테스트

```bash
cd C:\SSAFY\project2team\backend\data-service
python -m app.utils.spam_filter
```

### 12.2 GPT-4o 분석 테스트

```bash
cd C:\SSAFY\project2team\backend\data-service
python -m scripts.test_news_analyzer
```

테스트 메뉴:
1. 단일 뉴스 분석 테스트 (모의) - API 키 없이 동작 확인
2. DB 뉴스 분석 테스트 (실제 API 호출)
3. 일괄 분석 테스트

---

## 13. 운영 가이드: 뉴스 스케줄러 & 데이터 동기화

### 13.1 스케줄러 동작 방식

뉴스 크롤링은 **일회성 작업이 아닌 지속적 프로세스**입니다.

| 항목 | 설명 |
|------|------|
| **스케줄러 역할** | 주기적으로 새 뉴스 수집 (실시간 뉴스 서비스) |
| **실행 주기** | 정기 10분 / 전체 1시간 |
| **종료 조건** | 없음 - 서비스 중에는 계속 실행 |
| **정상 동작** | 뉴스 수가 계속 증가하는 것이 정상 |

### 13.2 로컬-원격 데이터 차이

```
┌──────────────────────────────────────────────────────────┐
│  [로컬 서버]              [원격 서버]                      │
│  - 스케줄러 실행 중        - 스케줄러 미실행 (현재)         │
│  - 뉴스: 계속 증가         - 뉴스: 동기화 시점 기준 고정     │
│  - 예: 3,271개            - 예: 3,230개                   │
│           │                        ↑                      │
│           └────── 동기화 필요 ──────┘                      │
└──────────────────────────────────────────────────────────┘
```

**차이 발생 원인:**
- 로컬에서만 스케줄러가 실행 중
- 동기화 이후에도 로컬에 새 뉴스 계속 추가
- 동기화 시점에 따라 수십~수백 건 차이 발생 가능

### 13.3 운영 환경별 권장 구성

| 환경 | 스케줄러 | 설명 |
|------|---------|------|
| **개발 (로컬)** | ✅ 실행 | 뉴스 수집 + AI 분석 테스트 |
| **운영 (원격)** | ✅ 실행 필요 | 실제 서비스는 원격에서 직접 수집해야 함 |
| **개발 중 원격** | ❌ 미실행 | 로컬에서 수집 → 수동 동기화 |

### 13.4 데이터 동기화 방법

**현재 방식 (개발 중):**
```bash
# 1. 새 뉴스 동기화
python scripts/sync_news_simple.py

# 2. 뉴스-종목 매핑 동기화
python scripts/sync_news_mapping.py

# 3. AI 분석 결과 동기화
python scripts/sync_ai_results.py
```

**운영 배포 시:**
- 원격 서버에서 스케줄러 직접 실행
- 로컬-원격 동기화 불필요
- 각 서버가 독립적으로 뉴스 수집/분석

### 13.5 주의사항

```
⚠️ 뉴스 크롤링은 "끝나는" 작업이 아닙니다.
   - 스케줄러가 돌면서 새 뉴스를 계속 수집
   - 서비스 중에는 24시간 동작
   - 데이터 차이는 정상적인 현상

⚠️ 운영 배포 전 체크리스트:
   □ 원격 서버 스케줄러 설정
   □ 환경변수 (API 키 등) 설정
   □ 로그 모니터링 설정
   □ 초기 데이터 마이그레이션 (필요시)
```
