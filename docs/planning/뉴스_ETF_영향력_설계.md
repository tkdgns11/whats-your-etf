# 뉴스-ETF 영향력 분석 설계

> 뉴스 기사가 어떤 ETF에 영향을 미치는지 2-Step 분석으로 매핑하는 기능 설계 문서

---

## 1. 개요

### 1.1 목적

- **뉴스 상세 페이지**에서 "이 뉴스와 관련된 ETF" 섹션 제공
- **ETF 상세 페이지**에서 "관련 뉴스" + "주요 이슈 타임라인" 제공
- 과거 뉴스는 **실제 주가 데이터 기반**으로 정확한 영향도 제공

### 1.2 핵심 설계 원칙

| 원칙 | 설명 |
|------|------|
| **2-Step 분석** | 뉴스 → 산업 매핑 (LLM) → ETF 매핑 (데이터 기반) |
| **실시간 표시** | 뉴스 상세의 관련 ETF는 실시간 시세로 즉시 표시 |
| **타임라인 검증** | ETF 타임라인은 장 마감 후 검증된 데이터만 표시 |

### 1.3 화면 예시

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

**ETF 상세 - 주요 이슈 타임라인**
```
┌─────────────────────────────────────────┐
│ 주요 이슈 타임라인                        │
│                                         │
│ ● 2024.03.20                            │
│   연준 기준금리 동결 발표                 │
│   → 시장 예상치 부합, 기술주 반등세 지속   │
│                                         │
│ ● 2024.02.15                            │
│   주요 기업 4분기 실적 발표               │
│   → 반도체 섹터 어닝 서프라이즈           │
│                                         │
│ 타임라인 더보기 ▼                        │
└─────────────────────────────────────────┘
```

---

## 2. 뉴스 본문 처리 전략

### 2.1 본문 필요성 분석

| 용도 | 본문 필요 여부 | 설명 |
|------|---------------|------|
| **사용자 UI (뉴스 상세)** | 필수 | 기사 내용을 읽을 수 있어야 함 |
| **ETF 영향도 분석 (LLM)** | 선택 | 제목+요약으로도 분석 가능, 본문 있으면 더 정확 |

### 2.2 분석 품질 비교

```
# 제목만
"삼성전자, 2분기 실적 발표"
→ 긍정? 부정? 판단 불가

# 제목 + RSS snippet
"삼성전자, 2분기 실적 발표 - 영업이익 30% 증가, 시장 예상치 상회"
→ 긍정 판단 가능 ✓

# 제목 + 본문 전체
→ 더 정확한 맥락 파악 가능 ✓✓
```

### 2.3 하이브리드 처리 방식

```
┌─────────────────────────────────────────────────────────────┐
│                    뉴스 수집 (RSS)                           │
│                                                             │
│  모든 뉴스: 제목 + RSS snippet → content_summary 저장        │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   본문 크롤링 시도                            │
│                                                             │
│  news_source.is_content_available = TRUE 인 언론사만         │
│  성공 시 → content 컬럼에 본문 저장                          │
│  실패 시 → content = NULL (RSS snippet만 유지)               │
└────────────────────────┬────────────────────────────────────┘
                         │
          ┌──────────────┴──────────────┐
          ▼                              ▼
┌──────────────────────┐      ┌──────────────────────┐
│   LLM 영향도 분석     │      │    사용자 UI 표시     │
├──────────────────────┤      ├──────────────────────┤
│ 입력: 제목 +          │      │ 본문 있는 뉴스만      │
│      content_summary  │      │ 목록에 표시          │
│                       │      │                      │
│ ※ 본문 유무 상관없이   │      │ ※ 본문 없는 뉴스는   │
│   분석 가능           │      │   목록에서 제외       │
│                       │      │                      │
└──────────────────────┘      └──────────────────────┘
```

### 2.4 LLM 분석 출력 구조

Step 1에서 LLM은 뉴스 분석 후 다음 3가지를 한번에 출력:

```json
{
  "keywords": ["금리동결", "나스닥", "빅테크", "반도체"],
  "content_summary": {
    "bullets": [
      "미국 연방준비제도(Fed)가 기준금리를 현 수준에서 동결하기로 만장일치 결정",
      "파월 의장은 인플레이션 둔화세가 뚜렷해질 때까지 신중한 접근 유지 시사",
      "금리 인상 사이클 종료 기대감에 나스닥 등 주요 기술주들이 강한 매수세"
    ]
  },
  "industry_influence": [
    {"group_code": "IT_SEMI", "relevance": 0.85, "sentiment": "POSITIVE"},
    {"group_code": "IT_ELEC", "relevance": 0.72, "sentiment": "POSITIVE"}
  ]
}
```

| 필드 | 저장 위치 | 용도 |
|------|----------|------|
| keywords | `news_article.keywords` (JSONB) | 뉴스 상세 키워드 칩 |
| content_summary | `news_article.content_summary` (JSONB) | AI 핵심 요약 (3개 불릿) |
| industry_influence | `news_industry_influence` 테이블 | 산업-ETF 매핑용 |

| 조건 | 처리 |
|------|------|
| 본문 있음 | LLM으로 keywords + 3개 불릿 요약 생성 |
| 본문 없음 | UI에 미표시 (목록에서 제외) |

### 2.5 언론사별 처리

```sql
-- 언론사 관리 테이블 (news_source)
-- is_content_available = TRUE: 본문 크롤링 시도
-- is_content_available = FALSE: RSS snippet만 사용

-- 현재 본문 크롤링 가능 언론사 (6개)
-- hankyung.com, sedaily.com, edaily.co.kr, news1.kr, etoday.co.kr, bizwatch.co.kr

-- 본문 크롤링 불가 언론사 (봇 차단 등)
-- mk.co.kr, yna.co.kr, biz.chosun.com, mt.co.kr, fnnews.com ...
```

### 2.6 결론

| 항목 | 결정 |
|------|------|
| **본문 없는 뉴스 UI 표시?** | ❌ 표시하지 않음 (목록에서 제외) |
| **본문 없는 뉴스 LLM 분석?** | ⭕ 분석함 (제목 + snippet으로 분석 가능) |
| **LLM 분석 입력** | 제목 + content_summary (snippet or 요약) |
| **사용자 UI** | 본문 있는 뉴스만 목록에 표시 |
| **예상 커버리지** | LLM 분석 100%, UI 표시 약 30~40% (본문 있는 언론사만) |

---

## 3. 2-Step 분석 방식

### 3.1 왜 2-Step인가?

| 기존 방식 | 2-Step 방식 |
|----------|------------|
| 뉴스 → ETF 직접 매핑 (LLM 예측) | 뉴스 → 산업 → ETF (데이터 기반) |
| 예측이라 부정확할 수 있음 | 산업 매핑은 객관적 |
| 과거 데이터 신뢰도 낮음 | **실제 주가로 검증** |

### 3.2 처리 흐름

```
[Step 1] 뉴스 발행 시점 (10분 배치)
┌──────────────┐
│ 뉴스 크롤러   │  실시간 뉴스 수집
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ news_article │  뉴스 저장 (URL 중복 체크)
└──────┬───────┘
       │
       ▼ 10분 배치
┌───────────────────────────────────────┐
│ LLM 분석 (뉴스 → 산업)                  │
│ - 금융/경제 카테고리만                   │
│ - 미분석 뉴스만 대상 (중복 분석 방지)     │
│ - 유사 뉴스 필터링 (제목 유사도 체크)     │
└──────┬────────────────────────────────┘
       │
       ▼
┌────────────────────────┐
│ news_industry_influence │  산업 매핑 저장
└────────────────────────┘


[Step 2] 다음날 장 마감 후 (일 1회 배치)
┌────────────────────────┐
│ news_industry_influence │
└──────┬─────────────────┘
       │
       ▼
┌───────────────────────────────────────┐
│ ETF 영향도 계산                         │
│ - industry → ETF 매핑 (etf_compositions)│
│ - 실제 ETF 가격 변동률 조회              │
│ - 영향도 산정                           │
└──────┬────────────────────────────────┘
       │
       ▼
┌─────────────────────┐
│ news_etf_influence  │  검증된 영향도 저장
│ (is_verified=TRUE)  │
└─────────────────────┘
```

### 3.3 관련 ETF 등락률 표시 시점

뉴스 상세 페이지의 "관련 ETF"는 **실시간 시세**를 표시합니다. 뉴스가 나온 시점에 이미 시장에 반영되었다고 보기 때문에, 장 마감까지 기다릴 필요 없이 즉시 표시합니다.

| 뉴스 발행 시점 | 등락률 표시 |
|---------------|------------|
| 장중 (09:00~15:30) | **즉시** 실시간 등락률 |
| 장 시작 전 (06:00~09:00) | 장 시작 후 실시간 반영 |
| 장 마감 후 (15:30~24:00) | 다음 거래일 장 시작 후 반영 |
| 주말/공휴일 | 다음 거래일 장 시작 후 반영 |

```
예시:
├─ 뉴스: 월요일 10:00 발행 → 즉시 실시간 등락률 표시
├─ 뉴스: 월요일 19:00 발행 → 화요일 09:00 이후 등락률 표시
└─ 뉴스: 토요일 11:00 발행 → 월요일 09:00 이후 등락률 표시
```

**뉴스 상세 vs ETF 타임라인 차이:**

| 화면 | 데이터 소스 | 표시 시점 |
|------|-----------|----------|
| 뉴스 상세 → 관련 ETF | 실시간 시세 API | 장 열리면 즉시 |
| ETF 상세 → 타임라인 | `news_etf_influence` (검증됨) | 장 마감 후 검증 완료 시 |

---

## 4. 테이블 구조

### 4.1 news_industry_influence (1차 분석: 뉴스 → 산업)

```sql
CREATE TABLE "news_industry_influence" (
    "id" BIGSERIAL PRIMARY KEY,
    "news_id" BIGINT NOT NULL,
    "industry_code" VARCHAR(10) NOT NULL,     -- industry_classification FK
    "relevance_score" DECIMAL(5,4),           -- 0.0 ~ 1.0 (관련도)
    "sentiment" VARCHAR(20),                  -- POSITIVE / NEGATIVE / NEUTRAL
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "fk_news_industry_news" FOREIGN KEY ("news_id")
        REFERENCES "news_article"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_news_industry_code" FOREIGN KEY ("industry_code")
        REFERENCES "industry_classification"("code") ON DELETE CASCADE,
    CONSTRAINT "uk_news_industry" UNIQUE ("news_id", "industry_code")
);
```

### 4.2 news_etf_influence (2차 분석: 검증된 ETF 영향)

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

### 4.3 컬럼 설명

**news_industry_influence**

| 컬럼 | 타입 | 설명 |
|------|------|------|
| news_id | BIGINT | 뉴스 FK |
| industry_code | VARCHAR(10) | 산업분류 FK |
| relevance_score | DECIMAL(5,4) | 관련도 (0.0 ~ 1.0) |
| sentiment | VARCHAR(20) | POSITIVE / NEGATIVE / NEUTRAL |

**news_etf_influence**

| 컬럼 | 타입 | 설명 |
|------|------|------|
| news_id | BIGINT | 뉴스 FK |
| etf_id | BIGINT | ETF FK |
| influence_score | DECIMAL(5,4) | 영향도 (0.0 ~ 1.0) |
| influence_type | VARCHAR(20) | POSITIVE / NEGATIVE / NEUTRAL |
| timeline_title | VARCHAR(100) | 타임라인 제목 (20자 이내) |
| timeline_summary | VARCHAR(200) | 타임라인 요약 (50자 이내) |
| analysis_reason | TEXT | 상세 분석 근거 |
| actual_change_rate | DECIMAL(8,4) | 실제 ETF 변동률 |
| verified_at | TIMESTAMP | 검증 시점 |
| is_verified | BOOLEAN | 검증 완료 여부 |

---

## 5. LLM 프롬프트 설계

### 5.1 1차 분석: 뉴스 → 산업 매핑

**시스템 프롬프트**
```
당신은 금융 뉴스 분석 전문가입니다.
주어진 뉴스 기사가 어떤 산업 분야에 관한 것인지 분석합니다.

분석 규칙:
1. 관련도 0.3 이상인 산업만 반환
2. 최대 3개 산업까지만 반환
3. 각 산업에 대해 sentiment(POSITIVE/NEGATIVE/NEUTRAL) 제공
```

**사용자 프롬프트**
```
[뉴스 정보]
제목: {title}
본문 요약: {content_summary}
카테고리: {category}

[산업 분류 목록]
- C26: 전자부품, 컴퓨터, 영상, 음향 및 통신장비 제조업
- K64: 금융업
- C21: 의료용 물질 및 의약품 제조업
...

위 뉴스가 어떤 산업에 관한 것인지 분석해주세요.
```

**응답 형식**
```json
{
  "industries": [
    {
      "industry_code": "C26",
      "relevance_score": 0.9,
      "sentiment": "POSITIVE"
    },
    {
      "industry_code": "K64",
      "relevance_score": 0.4,
      "sentiment": "NEUTRAL"
    }
  ]
}
```

---

## 6. 배치 작업

### 6.1 Step 1: 뉴스-산업 매핑 (10분 주기)

```python
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

async def analyze_news_industry_influence():
    """10분마다 새 뉴스의 산업 영향력 분석"""

    cutoff_time = datetime.now() - timedelta(minutes=10)

    # 1. 분석 대상 뉴스 조회 (미분석 뉴스만)
    new_news = await db.fetch_all("""
        SELECT n.id, n.title, n.content_summary, n.category
        FROM news_article n
        WHERE n.created_at > :cutoff_time
          AND n.category IN ('금융', 'ETF', '경제')
          AND NOT EXISTS (
              SELECT 1 FROM news_industry_influence nii
              WHERE nii.news_id = n.id
          )
        ORDER BY n.published_at ASC
    """, {"cutoff_time": cutoff_time})

    if not new_news:
        return

    # 2. 유사 뉴스 필터링 (같은 이슈 중복 제거)
    unique_news = filter_similar_news(new_news, threshold=0.8)

    # 3. 산업분류 목록 조회
    industries = await get_industry_list()

    # 4. 뉴스별 LLM 분석
    for news in unique_news:
        try:
            result = await analyze_news_to_industry(news, industries)

            for ind in result['industries']:
                if ind['relevance_score'] >= 0.3:
                    await save_news_industry_influence(
                        news_id=news['id'],
                        industry_code=ind['industry_code'],
                        relevance_score=ind['relevance_score'],
                        sentiment=ind['sentiment']
                    )

                    # 관심 ETF 뉴스 알림 발송
                    await send_news_alert_to_subscribers(
                        news_id=news['id'],
                        industry_code=ind['industry_code'],
                        sentiment=ind['sentiment']
                    )

        except Exception as e:
            print(f"Error: {e}")


def filter_similar_news(news_list: list, threshold: float = 0.8) -> list:
    """제목 유사도 기반 중복 뉴스 필터링"""
    if len(news_list) <= 1:
        return news_list

    titles = [n['title'] for n in news_list]
    vectorizer = TfidfVectorizer()
    tfidf_matrix = vectorizer.fit_transform(titles)

    unique_indices = [0]  # 첫 번째 뉴스는 항상 포함

    for i in range(1, len(news_list)):
        is_unique = True
        for j in unique_indices:
            similarity = cosine_similarity(
                tfidf_matrix[i], tfidf_matrix[j]
            )[0][0]
            if similarity > threshold:
                is_unique = False
                break
        if is_unique:
            unique_indices.append(i)

    return [news_list[i] for i in unique_indices]
```

### 6.2 Step 2: ETF 영향도 검증 (일 1회, 장 마감 후)

#### 6.2.1 검증 타이밍 규칙

| 뉴스 발행 시점 | 검증 시점 | 비교 대상 |
|---------------|----------|----------|
| 장중 (09:00~15:30) | **당일** 장 마감 후 | 당일 종가 vs 전일 종가 |
| 장 마감 후 (15:30~) | **다음 거래일** 장 마감 후 | 다음날 종가 vs 당일 종가 |
| 주말/공휴일 | **다음 거래일** 장 마감 후 | 월요일 종가 vs 금요일 종가 |

```
예시:
├─ 뉴스: 3/4(월) 오전 10시 → 검증: 3/4(월) 저녁 → 3/4 종가 vs 3/3 종가
├─ 뉴스: 3/4(월) 오후 7시  → 검증: 3/5(화) 저녁 → 3/5 종가 vs 3/4 종가
└─ 뉴스: 3/8(토) 오전 11시 → 검증: 3/10(월) 저녁 → 3/10 종가 vs 3/7 종가
```

#### 6.2.2 검증 대상 날짜 계산

```python
from datetime import datetime, date, time, timedelta
import holidays

kr_holidays = holidays.KR()

def get_verification_trade_date(news_published_at: datetime) -> tuple[date, date]:
    """
    뉴스 발행 시점 기준 검증에 사용할 거래일 반환

    Returns:
        (trade_date, prev_trade_date): 종가 비교할 두 거래일
    """
    market_close = time(15, 30)

    if news_published_at.time() <= market_close:
        # 장중 발행 → 당일이 기준
        target_date = news_published_at.date()
    else:
        # 장 마감 후 발행 → 다음날이 기준
        target_date = news_published_at.date() + timedelta(days=1)

    # 거래일로 조정 (주말/공휴일 스킵)
    trade_date = get_next_trading_day(target_date)
    prev_trade_date = get_prev_trading_day(trade_date)

    return trade_date, prev_trade_date


def get_next_trading_day(d: date) -> date:
    """다음 거래일 반환 (주말/공휴일 스킵)"""
    while d.weekday() >= 5 or d in kr_holidays:
        d += timedelta(days=1)
    return d


def get_prev_trading_day(d: date) -> date:
    """이전 거래일 반환 (주말/공휴일 스킵)"""
    d -= timedelta(days=1)
    while d.weekday() >= 5 or d in kr_holidays:
        d -= timedelta(days=1)
    return d
```

#### 6.2.3 검증 배치 로직

```python
async def verify_news_etf_influence():
    """장 마감 후 실제 데이터 기반 ETF 영향도 계산"""

    today = date.today()

    # 1. 검증 대상 뉴스 조회 (아직 미검증 + 오늘 검증 가능한 뉴스)
    news_industries = await db.fetch_all("""
        SELECT nii.*, na.title, na.published_at
        FROM news_industry_influence nii
        JOIN news_article na ON na.id = nii.news_id
        WHERE NOT EXISTS (
            SELECT 1 FROM news_etf_influence nei
            WHERE nei.news_id = nii.news_id AND nei.is_verified = TRUE
        )
        ORDER BY na.published_at ASC
    """)

    for news_ind in news_industries:
        # 2. 검증 가능 여부 확인
        trade_date, prev_trade_date = get_verification_trade_date(
            news_ind['published_at']
        )

        # 아직 검증할 수 없는 뉴스는 스킵
        if trade_date > today:
            continue

        # 3. 해당 산업 관련 ETF 조회
        related_etfs = await get_etfs_by_industry(news_ind['industry_code'])

        for etf in related_etfs:
            # 4. 실제 ETF 변동률 조회
            actual_change = await get_etf_change_rate(
                etf_id=etf['id'],
                trade_date=trade_date,
                prev_trade_date=prev_trade_date
            )

            # 4. 영향도 계산
            influence_score = calculate_influence(
                relevance=news_ind['relevance_score'],
                sentiment=news_ind['sentiment'],
                actual_change=actual_change
            )

            if influence_score >= 0.3:
                # 5. 타임라인용 텍스트 생성 (LLM)
                timeline = await generate_timeline_text(
                    news_title=news_ind['title'],
                    etf_name=etf['name'],
                    sentiment=news_ind['sentiment'],
                    actual_change=actual_change
                )

                # 6. 저장
                await save_news_etf_influence(
                    news_id=news_ind['news_id'],
                    etf_id=etf['id'],
                    influence_score=influence_score,
                    influence_type=determine_type(actual_change),
                    timeline_title=timeline['title'],
                    timeline_summary=timeline['summary'],
                    actual_change_rate=actual_change,
                    is_verified=True
                )
```

### 6.3 영향도 계산 로직

```python
def calculate_influence(
    relevance: float,      # 뉴스-산업 관련도
    sentiment: str,        # POSITIVE/NEGATIVE/NEUTRAL
    actual_change: float   # 실제 ETF 변동률 (%)
) -> float:
    """
    영향도 = (산업 관련도 × 0.5) + (변동률 기여도 × 0.5)
    """

    # 변동률 기여도 (절대값 기준, 최대 1.0)
    change_factor = min(abs(actual_change) / 5.0, 1.0)

    # sentiment와 변동 방향 일치 시 보너스
    if sentiment == 'POSITIVE' and actual_change > 0:
        change_factor *= 1.2
    elif sentiment == 'NEGATIVE' and actual_change < 0:
        change_factor *= 1.2

    influence = (relevance * 0.5) + (change_factor * 0.5)
    return min(influence, 1.0)


def determine_type(actual_change: float) -> str:
    """실제 변동률 기반 영향 유형 결정"""
    if actual_change >= 1.0:
        return 'POSITIVE'
    elif actual_change <= -1.0:
        return 'NEGATIVE'
    else:
        return 'NEUTRAL'
```

### 6.4 중복 방지 전략

#### 6.4.1 3단계 중복 방지

| 단계 | 대상 | 방법 |
|------|------|------|
| **DB 저장** | 동일 URL 뉴스 | `source_url UNIQUE` 제약 |
| **분석 스킵** | 이미 분석된 뉴스 | `news_industry_influence` 존재 여부 |
| **유사 뉴스** | 같은 이슈 다른 언론사 | 제목 유사도 체크 (TF-IDF) |

#### 6.4.2 유사 뉴스 필터링 예시

```
같은 이슈, 다른 언론사:
├─ "연준 금리 동결" - 한경        ← 분석 O (최초)
├─ "Fed 기준금리 유지" - 매경     ← 스킵 (유사도 0.85)
└─ "미국 금리 동결 결정" - 조선   ← 스킵 (유사도 0.82)
```

#### 6.4.3 배치 주기와 비용

| 주기 | 실제 LLM 호출 | 비고 |
|------|--------------|------|
| 10분 | 미분석 + 비중복만 | 알림 지연 최대 10분 |
| 30분 | 동일 | 알림 지연 최대 30분 |

**결론**: 중복 방지 덕분에 배치 주기와 LLM 비용은 무관. 차이는 **알림 지연 시간**뿐.

### 6.5 관심 ETF 뉴스 알림

뉴스 → 산업 분류 완료 시, 해당 산업 관련 ETF를 관심종목에 등록한 사용자에게 알림 발송.

```python
async def send_news_alert_to_subscribers(
    news_id: int,
    industry_code: str,
    sentiment: str
):
    """관심 ETF 사용자에게 뉴스 알림 발송"""

    # 1. 해당 산업의 ETF 목록 조회
    related_etfs = await db.fetch_all("""
        SELECT DISTINCT e.id, e.name
        FROM etf e
        JOIN etf_sector_breakdown esb ON e.id = esb.etf_id
        WHERE esb.industry_code = :industry_code
          OR esb.group_code = (
              SELECT group_code FROM industry_classification
              WHERE code = :industry_code
          )
    """, {"industry_code": industry_code})

    # 2. 해당 ETF를 관심종목에 추가한 사용자 조회
    for etf in related_etfs:
        subscribers = await db.fetch_all("""
            SELECT DISTINCT u.id, ft.token
            FROM user_favorite_etf ufe
            JOIN "user" u ON u.id = ufe.user_id
            JOIN notification_setting ns ON ns.user_id = u.id
            JOIN fcm_token ft ON ft.user_id = u.id AND ft.is_active = TRUE
            WHERE ufe.etf_id = :etf_id
              AND ns.news_alert = TRUE
        """, {"etf_id": etf['id']})

        # 3. 뉴스 정보 조회
        news = await get_news_by_id(news_id)

        # 4. 알림 발송
        for sub in subscribers:
            await send_fcm_notification(
                token=sub['token'],
                title=f"📰 {etf['name']} 관련 뉴스",
                body=news['title'],
                data={
                    "type": "NEWS_RELATED",
                    "news_id": news_id,
                    "etf_id": etf['id']
                }
            )

            # 5. 알림 이력 저장
            await save_etf_alert(
                user_id=sub['id'],
                etf_id=etf['id'],
                alert_type="NEWS_RELATED",
                title=f"{etf['name']} 관련 뉴스",
                message=news['title']
            )
```

---

## 7. API 응답 예시

### 7.1 ETF 상세 - 주요 이슈 타임라인

```json
GET /api/etf/45/timeline?limit=20

{
  "etf_id": 45,
  "etf_name": "KODEX 반도체",
  "timeline": [
    {
      "news_id": 123,
      "date": "2024-03-20",
      "timeline_title": "연준 기준금리 동결 발표",
      "timeline_summary": "시장 예상치 부합, 기술주 중심 반등세 지속",
      "influence_type": "POSITIVE",
      "actual_change_rate": 2.34,
      "is_verified": true
    },
    {
      "news_id": 118,
      "date": "2024-02-15",
      "timeline_title": "주요 기업 4분기 실적 발표",
      "timeline_summary": "반도체 섹터 어닝 서프라이즈로 수익률 견인",
      "influence_type": "POSITIVE",
      "actual_change_rate": 3.12,
      "is_verified": true
    }
  ]
}
```

### 7.2 뉴스 상세 - 관련 ETF (실시간)

관련 ETF는 산업 매핑 기반으로 조회하고, 등락률은 **실시간 시세 API**에서 가져옵니다.

```json
GET /api/v1/news/123

{
  "id": 123,
  "title": "미국 연준 금리 동결 발표, 기술주 강세",
  "keywords": ["금리동결", "나스닥", "빅테크", "반도체"],
  "aiSummary": [
    "미국 연방준비제도(Fed)가 기준금리를 현 수준에서 동결하기로 결정",
    "파월 의장은 인플레이션 둔화세가 뚜렷해질 때까지 신중한 접근 유지 시사",
    "금리 인상 사이클 종료 기대감에 나스닥 등 주요 기술주들이 강세"
  ],
  "content": "본문 전체...",
  "source": "한국경제",
  "publishedAt": "2025-01-17T09:30:00Z",
  "relatedEtfs": [
    {
      "id": 45,
      "ticker": "091160",
      "name": "KODEX 반도체",
      "changeRate": 1.24,
      "influenceType": "POSITIVE"
    },
    {
      "id": 46,
      "ticker": "091170",
      "name": "TIGER 반도체",
      "changeRate": 1.56,
      "influenceType": "POSITIVE"
    }
  ]
}
```

**데이터 흐름:**
```
1. 뉴스 → 산업 매핑 (news_industry_influence)
2. 산업 → ETF 매핑 (etf_sector_breakdown)
3. ETF 등락률 조회 (실시간 시세 API)
4. 응답 반환
```

---

## 8. 테이블 관계도

```
┌─────────────┐
│ news_article│
├─────────────┤
│ id (PK)     │
│ title       │
│ content     │
└──────┬──────┘
       │
       │ 1:N
       ▼
┌────────────────────────┐         ┌──────────────────────┐
│ news_industry_influence │         │ industry_classification│
├────────────────────────┤         ├──────────────────────┤
│ news_id (FK)           │────────►│ code (PK)            │
│ industry_code (FK)     │         │ name                 │
│ relevance_score        │         └──────────────────────┘
│ sentiment              │
└────────────────────────┘
       │
       │ 계산
       ▼
┌─────────────────────┐            ┌─────────────┐
│ news_etf_influence  │            │     etf     │
├─────────────────────┤            ├─────────────┤
│ news_id (FK)        │───────────►│ id (PK)     │
│ etf_id (FK)         │            │ name        │
│ influence_score     │            │ sector      │
│ timeline_title      │            └─────────────┘
│ timeline_summary    │
│ actual_change_rate  │
│ is_verified         │
└─────────────────────┘
```

---

## 9. 데이터 흐름 요약

```
[뉴스 발행] ─────────────────────────────────────────────────────────►
     │
     │ 실시간 수집 (10분 배치)
     ▼
[news_article 저장]  ← URL 중복 체크 (source_url UNIQUE)
     │
     │ 10분 배치
     ▼
[중복 체크]
├─ 분석 완료 뉴스 스킵 (news_industry_influence 존재 여부)
├─ 유사 뉴스 필터링 (제목 유사도 > 0.8 스킵)
     │
     ▼
[LLM 분석]
├─ keywords 추출 → news_article.keywords
├─ content_summary 생성 → news_article.content_summary (JSONB)
└─ 산업 매핑 → news_industry_influence 저장
     │
     ├──────────────────────────────────────────►[관심 ETF 뉴스 알림]
     │                                            산업 → ETF → 관심유저 → FCM
     │
     ├──────────────────────────────────────────►[뉴스 상세 - 관련 ETF]
     │                                            산업 매핑 + 실시간 시세 API
     │                                            (장 열리면 즉시 표시)
     │
     │                                              [장 마감 후]
     │                                                    │
     └────────────────────────────────────────────────────┤
                                                          │
                                                          ▼
                                              [ETF 영향도 검증]
                                              - 산업 → ETF 매핑
                                              - 실제 주가 변동률 조회
                                              - 영향도 산정
                                                          │
                                                          ▼
                                              [news_etf_influence 저장]
                                              (is_verified = TRUE)
                                                          │
                                                          ▼
                                              [ETF 타임라인 API 제공]
```

---

## 10. 장점

| 항목 | 설명 |
|------|------|
| **과거 데이터 정확** | 실제 주가 변동 기반이라 신뢰도 높음 |
| **산업 매핑 재사용** | 뉴스→산업은 한번만, ETF 매핑은 자동 |
| **검증 가능** | `is_verified`로 예측 vs 실제 구분 |
| **타임라인 품질** | 각 필드별 목적 분리로 일관된 UI |

---

## 11. 비용 추정

| 항목 | 수치 |
|------|------|
| 하루 금융/경제 뉴스 | 약 100~200건 |
| Step 1 (뉴스→산업) | 건당 ~300 토큰 |
| Step 2 (타임라인 생성) | 건당 ~200 토큰 |
| GPT-4o-mini 비용 | 입력 $0.15/1M, 출력 $0.6/1M |
| **예상 일일 비용** | **$0.1 ~ $0.3** |
