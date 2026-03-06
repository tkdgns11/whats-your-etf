# 뉴스-ETF 영향력 분석 설계

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
| **Constrained Selection** | LLM이 우리 DB 목록에서만 "선택" (Hallucination 방지) |
| **1:N 영향 매핑** | 뉴스 하나가 여러 회사/산업에 각각 다른 영향도로 매핑 |
| **타임라인 검증** | ETF 타임라인은 장 마감 후 검증된 데이터만 표시 |

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
│   └── news_scraper.py      # Google News RSS 크롤러 + Spam Filter
├── utils/
│   └── spam_filter.py       # Spam Filter (Whitelist/Blacklist)
└── schedulers/
    └── scheduler.py         # APScheduler 스케줄러
```

### 1.5 화면 예시

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
│  경제 뉴스 RSS 수집                                          │
│  - 정기 크롤링: 10분마다 (우선순위 키워드)                     │
│  - 전체 크롤링: 1시간마다 (모든 키워드)                        │
│  - Google News + Naver News                                 │
│  - ~100개/일 (중복 제거 후)                                  │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  Spam Filter (spam_filter.py)                               │
│  - Whitelist: 경제 키워드 있으면 무조건 통과                  │
│  - Blacklist: 스포츠/연예/날씨/광고 등 스팸 제거              │
│  - 패턴 매칭: [광고], [스포츠] 등                            │
│  - ~90개 통과                                               │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  본문 크롤링 + 썸네일 추출                                    │
│  - content_scraper.py (og:image 포함)                       │
│  - 본문 없어도 RSS snippet으로 LLM 분석 가능                  │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  GPT-4o 분석 (NewsImpactAnalyzer)                            │
│  - 입력: 뉴스 제목/본문 + 관심 ETF 회사 + 상위 100개 회사      │
│  - LLM이 DB 목록에서 관련 회사/산업 "선택" (Constrained)      │
│  - 출력: impacts[] + summary[] + keywords[]                 │
│  - 비용: ~$0.40/일 ≈ $12/월                                 │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  DB 저장                                                    │
│  - news_article: 뉴스 기본 정보 + content_summary + keywords │
│  - news_impact: 회사/산업별 영향도 (1:N)                     │
│  - 관련 없는 뉴스: impacts = [] (저장은 하되 매핑 없음)        │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  알림 발송                                                   │
│  - user_holding_etf → etf → etf_composition → company_info  │
│  - news_impact와 조인 → 관련 사용자에게 알림                  │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Spam Filter

> 명백한 스팸만 제거하고, 나머지는 GPT-4o가 판단

### 3.1 구현 (`app/utils/spam_filter.py`)

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

### 3.2 왜 Whitelist가 필요한가?

| 케이스 | 제목 | Blacklist만 | Whitelist 포함 |
|--------|------|------------|----------------|
| 정상 | "삼성전자 야구장 건설 계획 발표" | ❌ 스팸 (야구) | ✅ 통과 (삼성) |
| 정상 | "손흥민 연봉 협상" | ❌ 스팸 (연봉=?) | ❌ 스팸 |
| 스팸 | "[스포츠] 월드컵 예선" | ❌ 스팸 | ❌ 스팸 |

**Whitelist 우선순위로 경제 뉴스 놓치지 않음. 애매한 건 GPT-4o가 판단.**

### 3.3 news_scraper.py 연동

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

### 3.4 수집 통계 반환

```python
async def scrape_by_keyword(self, keyword: str, max_items: int = 5) -> dict:
    """
    Returns:
        {"saved": int, "spam": int, "duplicate": int}
    """
```

---

## 4. GPT-4o 분석 (Constrained LLM)

### 4.1 핵심 원칙: "생성" 아닌 "선택"

| 나쁜 예 | 좋은 예 |
|---------|---------|
| "이 뉴스의 산업 분류를 알려주세요" | "아래 목록에서 관련된 것을 선택하세요" |
| LLM이 새로운 분류 만들 수 있음 | 우리 DB에 있는 값만 선택 가능 |
| Hallucination 위험 | **Hallucination 방지** |

### 4.2 구현 (`app/services/news_impact_analyzer.py`)

```python
# 시스템 프롬프트
SYSTEM_PROMPT = """당신은 금융 뉴스 분석 전문가입니다.
뉴스 기사를 분석하여 관련된 회사와 산업을 아래 목록에서만 선택하고, 각각의 영향도를 평가합니다.

## 규칙
1. **반드시 제공된 목록에서만 선택** (목록에 없으면 선택 불가)
2. 직접 언급된 것 + 간접 영향 받는 것 모두 포함
3. 영향도 점수: -1.0(매우 부정) ~ +1.0(매우 긍정)
4. 관련 없으면 빈 배열 반환
5. 요약은 3개 이내 bullet point로

## 출력 형식 (JSON)
{
  "impacts": [
    {"target": "삼성전자", "type": "company", "score": 0.7, "reason": "AI 반도체 투자 확대"},
    {"target": "IT_SEMI", "type": "industry", "score": 0.5, "reason": "반도체 업황 개선 기대"}
  ],
  "summary": [
    "삼성전자가 AI 반도체에 10조원 투자 발표",
    "2025년까지 생산능력 2배 확대 계획"
  ],
  "keywords": ["AI반도체", "삼성전자", "투자확대"]
}"""


class NewsImpactAnalyzer:
    def __init__(self, db: Session):
        self.db = db
        self.llm = LLMService(db)
        self._companies_cache = None
        self._industries_cache = None

    def _load_companies(self) -> List[Dict]:
        """회사 목록 로드 (캐싱)"""
        # 관심 ETF 포함 회사 + 상위 100개
        if self._companies_cache is None:
            companies = self.db.query(CompanyInfo).filter(
                CompanyInfo.is_active == True
            ).all()
            self._companies_cache = [
                {"id": c.id, "name": c.stock_name, "code": c.stock_code, "group": c.industry_group}
                for c in companies
            ]
        return self._companies_cache

    def _build_user_message(self, news: NewsArticle) -> str:
        """프롬프트 입력 메시지 생성"""
        companies = self._load_companies()
        industries = self._load_industries()

        content = news.content or ""
        if len(content) > 1000:
            content = content[:1000] + "..."

        # 회사 목록 (상위 100개)
        company_list = "\n".join([
            f"- {c['name']} ({c['code']}, {c['group'] or '기타'})"
            for c in companies[:100]
        ])

        # 산업 목록
        industry_list = "\n".join([
            f"- {ind['code']}: {ind['name']}"
            for ind in industries
        ])

        return f"""[뉴스]
제목: {news.title}
본문: {content}
출처: {news.source}
발행일: {news.published_at}

[회사 목록] - 이 중에서만 선택
{company_list}

[산업 목록] - 이 중에서만 선택 (group_code 사용)
{industry_list}

위 뉴스를 분석하여 관련 회사/산업과 영향도를 JSON으로 출력하세요."""
```

### 4.3 회사 목록 전달 전략

| 전략 | 토큰 | 장점 | 단점 |
|------|------|------|------|
| 전체 2000개 | ~4000 | 모든 회사 분석 | 비용 증가 |
| **상위 100개** | ~400 | 비용 절약 | 소형주 놓침 |
| 관심 ETF 회사 | 가변 | 사용자 맞춤 | 구현 복잡 |

**현재 구현**: 상위 100개 (시가총액 기준)
**향후 개선**: 관심 ETF 포함 회사 + 상위 100개

### 4.4 1:N 영향 매핑 예시

```
뉴스: "TSMC HBM 생산 확대, 삼성전자 추격 어려워"

LLM 분석 결과:
{
  "impacts": [
    {"target": "TSMC", "type": "company", "score": 0.8, "reason": "HBM 시장 점유율 확대"},
    {"target": "삼성전자", "type": "company", "score": -0.3, "reason": "경쟁 심화로 점유율 하락 우려"},
    {"target": "SK하이닉스", "type": "company", "score": 0.2, "reason": "HBM 수요 증가 수혜"},
    {"target": "반도체", "type": "industry", "score": 0.4, "reason": "HBM 시장 전체 성장"}
  ],
  ...
}
```

**포인트:**
- 같은 뉴스에서 회사별로 다른 영향도
- 직접 언급(TSMC) + 간접 영향(삼성전자, SK하이닉스) 모두 캡처
- 산업 전체 영향도 별도 산정

---

## 5. 테이블 구조

### 5.1 news_impact (1차 분석: 뉴스 → 회사/산업)

```sql
CREATE TABLE "news_impact" (
    "id" BIGSERIAL PRIMARY KEY,
    "news_id" BIGINT NOT NULL,

    -- 영향 대상 (둘 중 하나)
    "target_type" VARCHAR(20) NOT NULL,       -- 'COMPANY' | 'INDUSTRY'
    "company_id" BIGINT,                      -- company_info FK (target_type='COMPANY')
    "industry_code" VARCHAR(20),              -- industry_classification FK (target_type='INDUSTRY')

    -- 영향도
    "impact_score" DECIMAL(3,2) NOT NULL,     -- -1.00 ~ +1.00
    "impact_reason" VARCHAR(200),             -- "AI 반도체 투자 확대로 실적 개선 기대"

    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "fk_news_impact_news" FOREIGN KEY ("news_id")
        REFERENCES "news_article"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_news_impact_company" FOREIGN KEY ("company_id")
        REFERENCES "company_info"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_news_impact_industry" FOREIGN KEY ("industry_code")
        REFERENCES "industry_classification"("code") ON DELETE CASCADE,
    CONSTRAINT "chk_target" CHECK (
        (target_type = 'COMPANY' AND company_id IS NOT NULL AND industry_code IS NULL) OR
        (target_type = 'INDUSTRY' AND industry_code IS NOT NULL AND company_id IS NULL)
    )
);

-- 인덱스
CREATE INDEX idx_news_impact_news ON news_impact(news_id);
CREATE INDEX idx_news_impact_company ON news_impact(company_id) WHERE company_id IS NOT NULL;
CREATE INDEX idx_news_impact_industry ON news_impact(industry_code) WHERE industry_code IS NOT NULL;
CREATE INDEX idx_news_impact_score ON news_impact(impact_score);
```

### 5.2 news_etf_influence (2차 분석: 검증된 ETF 영향)

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

### 5.3 테이블 관계

```
┌─────────────┐
│ news_article│
├─────────────┤
│ id (PK)     │
│ title       │
│ content     │
│ keywords    │ ← LLM 생성
│ content_summary │ ← LLM 생성 (JSONB bullets)
└──────┬──────┘
       │
       │ 1:N
       ▼
┌────────────────────────┐
│ news_impact            │
├────────────────────────┤
│ news_id (FK)           │
│ target_type            │ ← 'COMPANY' or 'INDUSTRY'
│ company_id (FK)        │───────► company_info
│ industry_code (FK)     │───────► industry_classification
│ impact_score           │ ← -1.0 ~ +1.0
│ impact_reason          │
└────────────────────────┘
       │
       │ 장 마감 후 검증
       ▼
┌─────────────────────┐
│ news_etf_influence  │
├─────────────────────┤
│ news_id (FK)        │
│ etf_id (FK)         │───────► etf
│ influence_score     │
│ actual_change_rate  │ ← 실제 ETF 변동률
│ is_verified         │
└─────────────────────┘
```

---

## 6. 스케줄러 (`app/schedulers/scheduler.py`)

### 6.1 스케줄 구성

| 작업 | 주기 | 설명 |
|------|------|------|
| **정기 크롤링** | 10분 | 우선순위 키워드 뉴스 수집 |
| **전체 크롤링** | 1시간 | 모든 키워드 뉴스 수집 |
| **AI 분석** | 15분 | 미분석 뉴스 GPT-4o 분석 |
| **ETF 검증** | 매일 16:00 | 장 마감 후 영향도 검증 |
| **KRX 공시** | 매일 09:00 | KIND 공시 체크 |

### 6.2 뉴스 수집 작업

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

### 6.3 GPT-4o 분석 작업

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

### 6.4 분석 결과 저장 (`_save_impacts`)

```python
def _save_impacts(self, news: NewsArticle, result: AnalysisResult):
    """분석 결과를 DB에 저장"""
    companies = {c["name"]: c["id"] for c in self._load_companies()}
    industries = {ind["code"]: ind["code"] for ind in self._load_industries()}

    for impact in result.impacts or []:
        target = impact.get("target")
        target_type = impact.get("type", "").upper()
        score = max(-1.0, min(1.0, float(impact.get("score", 0))))
        reason = impact.get("reason", "")

        if target_type == "COMPANY" and target in companies:
            news_impact = NewsImpact(
                news_id=news.id,
                target_type="COMPANY",
                company_id=companies[target],
                impact_score=score,
                impact_reason=reason[:200]
            )
            self.db.add(news_impact)

        elif target_type == "INDUSTRY" and target in industries:
            news_impact = NewsImpact(
                news_id=news.id,
                target_type="INDUSTRY",
                industry_code=target,
                impact_score=score,
                impact_reason=reason[:200]
            )
            self.db.add(news_impact)

    # keywords, summary 저장
    if result.keywords:
        news.keywords = result.keywords
    if result.summary:
        news.content_summary = {"bullets": result.summary}

    self.db.commit()
```

### 6.5 ETF 영향도 검증 (매일 16:00)

```python
async def verify_news_etf_job():
    """뉴스-ETF 검증 작업 (장 마감 후)"""
    # news_impact → etf_compositions 조인 → ETF 매핑
    # 실제 주가 데이터로 영향도 검증
    # news_etf_influence 테이블에 저장
```

---

## 7. 사용자 관련 뉴스 조회

### 7.1 내 포트폴리오 관련 뉴스 쿼리

```sql
-- 내 보유 ETF 관련 뉴스 (회사/산업 매칭)
SELECT DISTINCT
    n.id,
    n.title,
    n.content_summary,
    n.thumbnail_url,
    n.published_at,
    ni.impact_score,
    ni.impact_reason,
    ni.target_type,
    CASE
        WHEN ni.target_type = 'COMPANY' THEN ci.stock_name
        WHEN ni.target_type = 'INDUSTRY' THEN ic.name
    END as target_name
FROM news_article n
JOIN news_impact ni ON n.id = ni.news_id
LEFT JOIN company_info ci ON ni.company_id = ci.id
LEFT JOIN industry_classification ic ON ni.industry_code = ic.code
WHERE n.id IN (SELECT DISTINCT news_id FROM news_impact)  -- 영향 분석된 뉴스만
  AND (
    -- 내 ETF 구성종목과 매칭 (회사)
    (ni.target_type = 'COMPANY' AND ni.company_id IN (
        SELECT ec.company_id
        FROM user_holding_etf uhe
        JOIN etf_compositions ec ON uhe.etf_id = ec.etf_id
        WHERE uhe.user_id = :user_id
    ))
    OR
    -- 내 ETF 산업과 매칭 (산업)
    (ni.target_type = 'INDUSTRY' AND ni.industry_code IN (
        SELECT ic2.code
        FROM user_holding_etf uhe
        JOIN etf_sector_breakdown esb ON uhe.etf_id = esb.etf_id
        JOIN industry_classification ic2 ON ic2.group_code = esb.group_code
        WHERE uhe.user_id = :user_id
    ))
  )
ORDER BY n.published_at DESC
LIMIT 20;
```

### 7.2 API 응답 예시

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
      "thumbnailUrl": "https://...",
      "publishedAt": "2025-01-17T09:30:00Z",
      "impacts": [
        {"target": "삼성전자", "type": "company", "score": 0.7},
        {"target": "반도체", "type": "industry", "score": 0.5}
      ],
      "relatedEtfs": [
        {"id": 45, "name": "KODEX 반도체", "changeRate": 1.24}
      ]
    }
  ]
}
```

---

## 8. 비용 추정

| 항목 | 수치 |
|------|------|
| 하루 경제 뉴스 수집 | ~100건 |
| Spam Filter 통과 | ~90건 |
| GPT-4o 분석 | ~90건 |
| LLM 토큰 (입력) | ~500 토큰/건 (제목 + 본문 snippet) |
| LLM 토큰 (출력) | ~200 토큰/건 |
| GPT-4o 비용 | 입력 $2.5/1M, 출력 $10/1M |
| **예상 일일 비용** | **~$0.40/일 ≈ $12/월** |

### 8.1 비용 최적화 팁

```
1. 본문 전체 대신 RSS snippet + 제목만 전달 (500 토큰 vs 2000 토큰)
2. 회사/산업 목록은 캐싱해서 재사용
3. 배치 처리로 API 호출 횟수 최소화 (10건씩)
```

---

## 9. 품질 보장 전략

### 9.1 Hallucination 방지

| 전략 | 설명 |
|------|------|
| **Constrained Selection** | LLM이 우리 DB 목록에서만 선택 |
| **JSON 출력 강제** | 파싱 가능한 구조화된 출력 |
| **Ground Truth 기반** | 모든 결과가 DB에 존재하는 값 |

### 9.2 일관성 보장

| 전략 | 설명 |
|------|------|
| **정해진 영향도 범위** | -1.0 ~ +1.0 숫자로 정량화 |
| **정해진 분류 체계** | industry_classification 기반 |
| **검증 프로세스** | 장 마감 후 실제 주가로 검증 |

### 9.3 검증 가능성

```
모든 분석 결과 추적 가능:
- news_impact.impact_score: LLM 예측 영향도
- news_etf_influence.actual_change_rate: 실제 ETF 변동률
- 예측 vs 실제 비교로 LLM 정확도 측정 가능
```

---

## 10. 요약

| 항목 | 값 |
|------|-----|
| **수집 방식** | Google News + Naver News (10분/1시간 주기) |
| **Spam Filter** | Whitelist + Blacklist + 패턴 매칭 (`spam_filter.py`) |
| **분석 방식** | All-LLM (GPT-4o가 직접 분석) |
| **분석 서비스** | `NewsImpactAnalyzer` (Constrained LLM) |
| **회사 목록** | 관심 ETF 회사 + 상위 100개 |
| **Constrained Selection** | LLM이 DB 목록에서만 선택 (Hallucination 방지) |
| **영향 매핑** | 1:N (뉴스 → 여러 회사/산업) |
| **영향도 형식** | -1.0 ~ +1.0 (부정~긍정) |
| **저장 테이블** | news_impact (1차), news_etf_influence (검증 후) |
| **스케줄러** | APScheduler (정기10분, 전체1시간, AI15분, ETF검증16:00) |
| **월 비용** | ~$12 |
| **장점** | 단순 구현, 높은 품질, 맥락 이해, 유지보수 최소화 |

---

## 11. 테스트

### 11.1 Spam Filter 테스트

```bash
cd C:\SSAFY\project2team\backend\data-service
python -m app.utils.spam_filter
```

### 11.2 GPT-4o 분석 테스트

```bash
cd C:\SSAFY\project2team\backend\data-service
python -m scripts.test_news_analyzer
```

테스트 메뉴:
1. 단일 뉴스 분석 테스트 (모의) - API 키 없이 동작 확인
2. DB 뉴스 분석 테스트 (실제 API 호출)
3. 일괄 분석 테스트
