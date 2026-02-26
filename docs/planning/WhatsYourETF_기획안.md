# What's your ETF — 기획서

> **팀 규모**: 6명 (백엔드 3 + Android 3)
> **기간**: 7주 스프린트
> **작성일**: 2026.02
> **분류**: SSAFY 2학기 특화프로젝트 (핀테크 / AI)

---

## 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [시스템 아키텍처](#2-시스템-아키텍처)
3. [외부 API 활용 계획](#3-외부-api-활용-계획)
4. [AI 모델 상세](#4-ai-모델-상세)
5. [핵심 기능 상세](#5-핵심-기능-상세)
6. [모바일 앱 화면 구성](#6-모바일-앱-화면-구성)
7. [데이터 수집 계획](#7-데이터-수집-계획)
8. [기술 스택](#8-기술-스택)
9. [팀 구성 및 역할](#9-팀-구성-및-역할)
10. [스프린트 계획 (0주차 준비 + 7주 개발)](#10-스프린트-계획)
11. [발표 스토리라인](#11-발표-스토리라인)
12. [리스크 및 대응](#12-리스크-및-대응)
13. [참고 자료](#13-참고-자료)

---

## 1. 프로젝트 개요

### 1.1 한 줄 요약
> **"ETF 클러스터링으로 관계를 파악하고, 목표 기반 시뮬레이션으로 전략을 세우고, AI Bull/Bear가 검증해주는 — ETF 탐색·시뮬·검증 플랫폼"**

### 1.2 핵심 컨셉

```
[왜 이 서비스가 필요한가]

  ETF 시장이 폭발적으로 성장하고 있다.
  2026년 기준 국내 상장 ETF 800개+, 순자산 150조원 돌파.

  그런데 기존 ETF 앱/사이트들은:
  - 수익률 순으로 나열할 뿐, ETF 간 관계를 보여주지 않는다
  - "KODEX 200이 좋대" 수준의 단편 정보만 제공한다
  - 내가 만든 포트폴리오가 실제로 어떤 성과를 냈을지 시뮬레이션할 방법이 없다
  - "이 조합이 정말 괜찮은 건지" 검증해줄 사람이 없다
  - "1년 전에 이 ETF를 샀으면 어떻게 됐을까?" 에 답해주는 곳이 없다
  - 재무제표, 섹터, 밸류에이션으로 ETF를 필터링할 수 없다
  - 과거 예측/전망이 실제로 맞았는지 사후 검증해주는 서비스가 없다

  → 기존 서비스: "어떤 ETF가 올랐다" (결과만)
  → What's your ETF: "ETF를 찾고, 비교하고, 시뮬레이션하고, 검증하는
    모든 과정을 한 곳에서. 스스로 판단하는 투자자로 성장하게."

[핵심 루프]

  ① Discovery    ETF 클러스터 맵에서 관계를 시각적으로 파악
      ↓
  ② Planning     목표(기간·금액·리스크)를 정하고 ETF 포트폴리오 구성
      ↓
  ③ Simulation   과거 데이터 기반 백테스트로 성과 검증
      ↓
  ④ Analysis     AI Bull/Bear 리뷰 + 뉴스 영향력 맵
      ↓
  ⑤ Strategy     전략을 저장하고, 이후 실제 시장에서의 움직임 추적

[서비스 철학]

  우리는 "이 ETF를 사라"고 말하지 않는다.
  사용자가 앱에 의존하는 게 아니라, 스스로 정보를 찾고 판단하는 실력을 키워주는 것이 목표.

  이를 위해 해주는 것:

  - ETF 간 숨겨진 관계를 시각적으로 보여준다 (클러스터 맵)
  - 재무제표·섹터·밸류에이션 등 다양한 기준으로 ETF를 필터링한다
  - 사용자의 목표에 맞는 조합을 직접 구성하게 돕는다 (슬라이더 UI)
  - "1년 전에 이걸 샀으면?" — 과거 데이터로 시뮬레이션해본다 (백테스트)
  - AI가 찬성(Bull)과 반대(Bear) 양쪽 관점에서 검증해준다
  - 과거 시점의 전망이 현재 기준으로 맞았는지 돌아본다 (전략 추적)
  - 뉴스가 내 포트폴리오에 어떤 영향을 주는지 바로 확인한다

  → "ETF 관련 정보는 여기서 다 찾는다"
  → "앱이 답을 주는 게 아니라, 사용자가 스스로 판단할 수 있는 도구를 준다"
  → 궁극적 목표: 앱 없이도 ETF/주식 투자를 잘 할 수 있는 실력을 키워주는 것
```

### 1.3 서비스명

**What's your ETF**
- "당신의 ETF는 무엇인가요?" — 나만의 ETF 조합을 찾아가는 여정
- 줄임: WYE (와이이)

### 1.4 타겟 사용자

```
[1차 타겟 — ETF 입문자]
  · ETF에 관심은 있지만 800개 중 뭘 골라야 할지 모르는 사람
  · "KODEX 200이랑 TIGER 200이 뭐가 다른 거야?"
  · 수익률 표만 봐서는 판단이 안 되는 사람
  → 클러스터 맵으로 관계를 한눈에 파악하게 도움

[2차 타겟 — 전략적 투자자]
  · 이미 ETF를 하고 있지만, 포트폴리오 최적화를 하고 싶은 사람
  · "내 조합이 진짜 괜찮은 건지" 검증하고 싶은 사람
  · 다양한 시나리오(기간, 리스크)에서 시뮬레이션하고 싶은 사람
  → 시뮬레이션 + AI Bull/Bear 리뷰로 전략 검증
```

### 1.5 기존 서비스 대비 차별점

| 구분 | 기존 ETF 앱 (삼성증권, NH 등) | ETF 정보 사이트 (funETF 등) | 우리 서비스 |
|---|---|---|---|
| ETF 탐색 | 수익률/카테고리 나열 | 수익률/비용 비교 | **클러스터 맵 (상관관계 + 구성종목 유사도 기반 2D 관계도)** |
| 포트폴리오 | 없음 (개별 매매) | 없음 | **목표 기반 포트폴리오 빌더 (슬라이더 + 실시간 지표)** |
| 시뮬레이션 | 없음 | 단순 과거 수익률 | **백테스트 (수익률·MDD·샤프비율·연환산수익률)** |
| AI 검증 | 없음 | 없음 | **Bull/Bear 캐릭터 리뷰 (찬성·반대 양면 분석)** |
| 뉴스 연동 | 전체 뉴스 | 없음 | **뉴스 → 클러스터 매핑 → 영향받는 ETF 그룹 하이라이트** |
| 전략 관리 | 없음 | 없음 | **전략 저장 + 이후 실제 움직임 추적** |

---

## 2. 시스템 아키텍처

### 2.1 전체 구조

```
┌─────────────── 데이터 소스 ──────────────────────────────┐
│                                                          │
│  [ETF 시세/구성]     [뉴스]                    [사용자 데이터] │
│   KIS API            네이버 뉴스                포트폴리오    │
│   실시간 시세/NAV     BigKinds                  시뮬 결과    │
│   ETF 구성종목 비중   섹터/테마 뉴스              전략 저장    │
│   거래량/등락률                                              │
│                                                          │
└──────┬──────────────────┬───────────────┬────────────────┘
       │                  │               │
       ▼                  ▼               ▼
┌─────── AI 엔진 ──────────────────────────────────────────┐
│                                                          │
│  [ETF 클러스터링]          [뉴스 영향력 분석]                │
│   상관관계 + 구성종목       뉴스 → 클러스터 매핑             │
│   유사도 기반 2D 맵 생성   → 맵 하이라이트                   │
│                                                          │
│  [Bull/Bear 리뷰]         [백테스트 엔진]                   │
│   포트폴리오 → LLM 프롬프트  과거 데이터 기반                 │
│   → 찬성(Bull)/반대(Bear)   수익률/MDD/샤프비율 계산         │
│   캐릭터 의견 생성                                        │
│                                                          │
│  ※ LLM = 외부 API (GPT/Gemini/Claude)                   │
│  ※ 클러스터링 = EC2 서버 내 FastAPI(Python) 연산            │
│  ※ 백테스트 = Spring Boot(Java) 내부 처리                   │
└──────────────────────┬───────────────────────────────────┘
                       │
                       ▼
┌─────── 실시간 시세 수집 ────────────────────────────────┐
│  KIS 시세 수집기 (별도 컨테이너, 항상 UP)                  │
│  → KIS API WebSocket 상시 연결                           │
│  → 수신 시세 → Redis SET + PUBLISH                       │
│  → Spring Boot 재시작과 무관하게 독립 동작                  │
└──────────┬──────────────────────────────────────────────┘
           │ Redis
           ▼
┌─────── 백엔드 서비스 ────────────────────────────────────┐
│  Spring Boot (Java 21, Virtual Thread)                   │
│  → ETF 시세/구성종목 API                                  │
│  → 포트폴리오 빌더 + 시뮬레이션 API                         │
│  → AI Bull/Bear 리뷰 API                                │
│  → 전략 저장/추적 API                                     │
│  → 뉴스 수집 + 클러스터 매핑 API                           │
│  → Redis에서 실시간 시세 읽기 → 클라이언트 WebSocket 푸시    │
└──────────┬──────────────────────────────────────────────┘
           │
           ▼
┌─────── Android 앱 ─────────────────────────────────────┐
│  Jetpack Compose / WebSocket / StateFlow                │
│  메인 / 탐색(클러스터맵) / 시뮬레이션 / 나의전략 / 마이페이지  │
└─────────────────────────────────────────────────────────┘
```

### 2.2 인프라 구성

```
┌── EC2 (SSAFY, 16GB/4vCPU/320GB) ─────────────────────┐
│                                                        │
│  [Docker Compose]                                      │
│   ├─ Spring Boot (Java 21, Virtual Thread)    (~2GB)   │
│   │   ├─ 백테스트 엔진 (Java 내부 처리)                 │
│   │   ├─ 뉴스 수집 스케줄러 (10분마다)                   │
│   │   ├─ LLM API 호출 (Bull/Bear, 뉴스 분석)            │
│   │   └─ 클라이언트 WebSocket 시세 푸시 (Redis에서 읽기) │
│   ├─ KIS 시세 수집기 (경량 서비스)             (~256MB) │
│   │   └─ KIS WebSocket 상시 연결 → Redis 실시간 저장    │
│   ├─ PostgreSQL 16          ← 메인 DB          (~4GB)  │
│   ├─ Redis                  ← 캐시/실시간 시세  (~1GB)  │
│   └─ FastAPI (Python)       ← 클러스터링 전용   (~1GB)  │
│       └─ HDBSCAN + UMAP (주 1회 배치)                   │
│                                                        │
│  여유 메모리: ~7GB (OS + 버퍼)                          │
│  디스크: ETF + 뉴스 = ~50GB → 320GB 충분               │
└──────────────────────┬─────────────────────────────────┘
                       │
          ┌────────────┼────────────┐
          ▼            ▼            ▼
   [Android 앱]  [외부 LLM API]  [외부 데이터 API]
                  GPT / Gemini    KIS API
                  Claude          네이버 뉴스

[KIS 시세 수집기 ↔ Spring Boot 통신]
  KIS 시세 수집기: KIS API WebSocket 상시 연결 (별도 컨테이너, 항상 UP)
  → 실시간 시세 수신 → Redis SET (etf:price:{ticker})
  → Spring Boot: Redis에서 최신 시세 읽기 → 클라이언트 WebSocket 푸시
  → Spring Boot 재시작해도 시세 수집 끊기지 않음
  → Kafka 불필요 (구독자 1개, 최신값만 의미, Redis Pub/Sub로 충분)

[Spring Boot ↔ FastAPI 통신]
  Spring Boot → REST 호출 → FastAPI (클러스터링 요청)
  FastAPI → 클러스터링 결과 → DB 직접 저장
  → 주 1회 배치이므로 REST로 충분

[비용]
  EC2: SSAFY 제공 (무료)
  LLM API: SSAFY GMS 크레딧 (GPT/Gemini/Claude)
  외부 데이터 API: 전부 무료 (KIS API, 네이버 뉴스)
```

### 2.3 DB 아키텍처

```
[PostgreSQL 16] 단일 DB, 단일 스키마
  ETF/클러스터/포트폴리오/시뮬레이션/전략/뉴스 전부 한 곳에서 관리
  병렬쿼리, 인덱스 최적화
```

#### 주요 테이블

```sql
-- 사용자
CREATE TABLE users (
    user_id        BIGSERIAL PRIMARY KEY,
    email          VARCHAR(100) UNIQUE NOT NULL,
    password_hash  VARCHAR(200),                     -- 자체 로그인 시 bcrypt 해시
    provider       VARCHAR(20) DEFAULT 'LOCAL',      -- LOCAL / KAKAO / GOOGLE
    provider_id    VARCHAR(200),                     -- 소셜 로그인 provider 고유 ID
    nickname       VARCHAR(50),
    profile_image  VARCHAR(300),
    risk_tolerance VARCHAR(20) DEFAULT 'MODERATE',   -- CONSERVATIVE/MODERATE/AGGRESSIVE
    created_at     TIMESTAMPTZ DEFAULT NOW(),
    last_login_at  TIMESTAMPTZ
);

-- ★ ETF 목록 (국내 상장 ETF ~800종)
CREATE TABLE etf_list (
    etf_id         BIGSERIAL PRIMARY KEY,
    ticker         VARCHAR(20) UNIQUE NOT NULL,     -- ETF 코드 (069500, 102110 등)
    name           VARCHAR(200) NOT NULL,           -- KODEX 200, TIGER 200 등
    -- 분류
    category       VARCHAR(50),                     -- 국내주식형/해외주식형/채권형/원자재형/통화형 등
    strategy_type  VARCHAR(30),                     -- MARKET/THEME/DIVIDEND/BOND/COVERED_CALL
    theme          VARCHAR(100),                    -- 반도체/2차전지/AI/배당/ESG/금리/헬스케어 등
    sector         VARCHAR(50),                     -- 소비재/증권/반도체/제조/보험/IT/에너지/금융 등
    asset_class    VARCHAR(30),                     -- EQUITY/BOND/COMMODITY/MIXED
    asset_manager  VARCHAR(50),                     -- 삼성(KODEX)/미래에셋(TIGER)/KB(KBSTAR) 등
    -- 속성 플래그
    is_leveraged   BOOLEAN DEFAULT FALSE,           -- 레버리지 ETF 여부
    is_inverse     BOOLEAN DEFAULT FALSE,           -- 인버스 ETF 여부
    is_hedged      BOOLEAN,                         -- 환헤지 여부 (NULL=해당없음, TRUE=H, FALSE=UH)
    -- 비용/규모
    expense_ratio  DECIMAL(6,4),                    -- 총보수 (%)
    nav            DECIMAL(14,2),                   -- 최근 NAV
    aum            BIGINT,                          -- 순자산총액 (원)
    -- 배당
    dividend_yield DECIMAL(6,3),                    -- 배당률 (%)
    dividend_freq  VARCHAR(10),                     -- MONTHLY/QUARTERLY/SEMI_ANNUAL/ANNUAL/NONE
    -- 밸류에이션 (구성종목 가중평균, 배치 계산)
    avg_per        DECIMAL(8,2),                    -- 가중평균 P/E
    avg_pbr        DECIMAL(8,2),                    -- 가중평균 P/B
    avg_roe        DECIMAL(8,2),                    -- 가중평균 ROE (%)
    -- 위험 분류 (변동성 기반, 배치 계산)
    risk_grade     VARCHAR(20),                     -- HIGH_RISK/MODERATE/STABLE
    volatility_1y  DECIMAL(8,4),                    -- 최근 1년 변동성 (%)
    -- 생애주기
    listing_date   DATE,
    delisted_date  DATE,                            -- 상장폐지일 (NULL이면 현재 상장 중)
    is_active      BOOLEAN DEFAULT TRUE,
    updated_at     TIMESTAMPTZ DEFAULT NOW()
);

-- ★ ETF 구성종목 (ETF → 보유 종목 비중)
CREATE TABLE etf_compositions (
    composition_id BIGSERIAL PRIMARY KEY,
    etf_ticker     VARCHAR(20) NOT NULL,            -- ETF 코드
    component_ticker VARCHAR(20),                   -- 구성 종목 코드
    component_name   VARCHAR(100),
    industry_code  VARCHAR(10),                     -- 표준산업소분류코드 (6자리, 예: 032601=반도체)
    industry_group VARCHAR(50),                     -- 대분류 그룹명 (반도체/전자IT/바이오/금융 등)
    sector         VARCHAR(50),                     -- 기존 섹터 (호환용)
    weight_pct     DECIMAL(6,3),                    -- 비중 (%)
    base_date      DATE NOT NULL,
    UNIQUE(etf_ticker, component_ticker, base_date)
);

-- ★ ETF 일별 시세 (클러스터링 + 백테스트용)
CREATE TABLE etf_prices (
    ticker         VARCHAR(20) NOT NULL,
    trade_date     DATE NOT NULL,
    open           DECIMAL(14,2),
    high           DECIMAL(14,2),
    low            DECIMAL(14,2),
    close          DECIMAL(14,2),
    nav            DECIMAL(14,2),
    volume         BIGINT,
    change_rate    DECIMAL(8,4),                    -- 등락률
    PRIMARY KEY (ticker, trade_date)
);

-- ★ ETF 클러스터 (클러스터링 결과)
CREATE TABLE etf_clusters (
    cluster_id     BIGSERIAL PRIMARY KEY,
    cluster_label  INT NOT NULL,                    -- 클러스터 번호
    cluster_name   VARCHAR(100),                    -- AI가 부여한 클러스터 이름 (예: "반도체/AI 성장")
    cluster_description TEXT,                       -- 클러스터 특징 설명
    etf_count      INT,                             -- 소속 ETF 수
    avg_return_1y  DECIMAL(8,4),                    -- 클러스터 평균 1년 수익률
    avg_volatility DECIMAL(8,4),                    -- 클러스터 평균 변동성
    computed_at    TIMESTAMPTZ DEFAULT NOW()         -- 계산 시점
);

-- ETF-클러스터 매핑 (N:1)
CREATE TABLE etf_cluster_members (
    member_id      BIGSERIAL PRIMARY KEY,
    etf_ticker     VARCHAR(20) NOT NULL,
    cluster_id     BIGINT REFERENCES etf_clusters(cluster_id),
    x_coord        DECIMAL(10,6),                   -- 2D 맵 X좌표
    y_coord        DECIMAL(10,6),                   -- 2D 맵 Y좌표
    distance_to_center DECIMAL(8,4),                -- 클러스터 중심과의 거리
    computed_at    TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(etf_ticker, computed_at)
);

-- ★ 사용자 포트폴리오 (계정당 최대 10개)
CREATE TABLE portfolios (
    portfolio_id   BIGSERIAL PRIMARY KEY,
    user_id        BIGINT REFERENCES users(user_id),
    name           VARCHAR(100) NOT NULL,           -- "나의 성장형 포트폴리오"
    -- 목표 설정
    goal_amount    DECIMAL(18,2),                   -- 목표 금액
    invest_amount  DECIMAL(18,2),                   -- 투자 금액
    invest_period  INT,                             -- 투자 기간 (개월)
    risk_level     VARCHAR(20),                     -- CONSERVATIVE/MODERATE/AGGRESSIVE
    -- 상태
    status         VARCHAR(20) DEFAULT 'DRAFT',     -- DRAFT/ACTIVE/ARCHIVED
    created_at     TIMESTAMPTZ DEFAULT NOW(),
    updated_at     TIMESTAMPTZ DEFAULT NOW()
);

-- 포트폴리오 내 ETF 구성 (포트폴리오당 최대 10개 ETF)
CREATE TABLE portfolio_etfs (
    portfolio_etf_id BIGSERIAL PRIMARY KEY,
    portfolio_id   BIGINT REFERENCES portfolios(portfolio_id),
    etf_ticker     VARCHAR(20) NOT NULL,
    weight_pct     DECIMAL(6,3) NOT NULL,           -- 비중 (%, 합 = 100)
    UNIQUE(portfolio_id, etf_ticker)
);

-- ★ 시뮬레이션 (백테스트 요청)
CREATE TABLE simulations (
    simulation_id  BIGSERIAL PRIMARY KEY,
    portfolio_id   BIGINT REFERENCES portfolios(portfolio_id),
    user_id        BIGINT REFERENCES users(user_id),
    -- 백테스트 설정
    start_date     DATE NOT NULL,
    end_date       DATE NOT NULL,
    initial_amount DECIMAL(18,2) NOT NULL,          -- 초기 투자 금액
    rebalance_period VARCHAR(20) DEFAULT 'MONTHLY', -- MONTHLY/QUARTERLY/YEARLY/NONE
    -- 결과
    final_amount   DECIMAL(18,2),
    total_return   DECIMAL(8,4),                    -- 총 수익률
    annualized_return DECIMAL(8,4),                 -- 연환산 수익률 (CAGR)
    max_drawdown   DECIMAL(8,4),                    -- 최대 낙폭 (MDD)
    sharpe_ratio   DECIMAL(8,4),                    -- 샤프 비율
    volatility     DECIMAL(8,4),                    -- 변동성
    -- 벤치마크 비교
    benchmark_ticker VARCHAR(20) DEFAULT '069500',  -- KODEX 200
    benchmark_return DECIMAL(8,4),
    alpha          DECIMAL(8,4),                    -- 초과 수익률
    -- 메타
    status         VARCHAR(20) DEFAULT 'PENDING',   -- PENDING/RUNNING/COMPLETED/FAILED
    created_at     TIMESTAMPTZ DEFAULT NOW(),
    completed_at   TIMESTAMPTZ
);

-- 시뮬레이션 일별 결과 (백테스트 차트용)
CREATE TABLE simulation_daily (
    daily_id       BIGSERIAL PRIMARY KEY,
    simulation_id  BIGINT REFERENCES simulations(simulation_id),
    trade_date     DATE NOT NULL,
    portfolio_value DECIMAL(18,2),                  -- 포트폴리오 평가액
    benchmark_value DECIMAL(18,2),                  -- 벤치마크 평가액
    daily_return   DECIMAL(8,6),                    -- 일별 수익률
    cumulative_return DECIMAL(8,4),                 -- 누적 수익률
    drawdown       DECIMAL(8,4),                    -- 낙폭
    UNIQUE(simulation_id, trade_date)
);

-- ★ AI 리뷰 (Bull/Bear)
CREATE TABLE ai_reviews (
    review_id      BIGSERIAL PRIMARY KEY,
    portfolio_id   BIGINT REFERENCES portfolios(portfolio_id),
    simulation_id  BIGINT REFERENCES simulations(simulation_id),
    user_id        BIGINT REFERENCES users(user_id),
    -- Bull 리뷰
    bull_summary   TEXT,                            -- 찬성 요약
    bull_points    JSONB,                           -- 찬성 근거 리스트
    bull_score     INT,                             -- 찬성 점수 (0~100)
    -- Bear 리뷰
    bear_summary   TEXT,                            -- 반대 요약
    bear_points    JSONB,                           -- 반대 근거 리스트
    bear_score     INT,                             -- 반대 점수 (0~100)
    -- 종합
    verdict        VARCHAR(20),                     -- STRONG_BUY/BUY/NEUTRAL/SELL/STRONG_SELL
    overall_comment TEXT,                           -- 종합 코멘트
    created_at     TIMESTAMPTZ DEFAULT NOW()
);

-- 뉴스 기사
CREATE TABLE news_articles (
    news_id        BIGSERIAL PRIMARY KEY,
    title          TEXT NOT NULL,
    content_summary TEXT,
    source         VARCHAR(100),
    category       VARCHAR(50),                     -- 경제/산업/기업/정책
    keywords       JSONB,                           -- 추출 키워드
    sentiment      DECIMAL(3,2),                    -- -1.0 ~ +1.0
    -- 클러스터 매핑
    affected_clusters JSONB,                        -- 영향받는 클러스터 ID + 영향도
    affected_etfs  JSONB,                           -- 영향받는 ETF 티커 리스트
    published_at   TIMESTAMPTZ,
    created_at     TIMESTAMPTZ DEFAULT NOW()
);

-- ★ 전략 저장 (Strategy)
CREATE TABLE strategies (
    strategy_id    BIGSERIAL PRIMARY KEY,
    user_id        BIGINT REFERENCES users(user_id),
    portfolio_id   BIGINT REFERENCES portfolios(portfolio_id),
    simulation_id  BIGINT REFERENCES simulations(simulation_id),
    review_id      BIGINT REFERENCES ai_reviews(review_id),
    name           VARCHAR(100),
    description    TEXT,
    -- 저장 시점 스냅샷
    saved_at       TIMESTAMPTZ DEFAULT NOW(),
    snapshot_etfs  JSONB,                           -- 저장 시점 ETF 구성 + 비중
    snapshot_metrics JSONB,                         -- 저장 시점 시뮬 지표
    -- 추적
    is_tracking    BOOLEAN DEFAULT TRUE,            -- 이후 움직임 추적 중
    tracking_start_date DATE,                       -- 추적 시작일
    current_return DECIMAL(8,4),                    -- 현재 수익률 (실시간 갱신)
    updated_at     TIMESTAMPTZ DEFAULT NOW()
);

-- 전략 일별 추적 (저장 이후 실제 시장 움직임)
CREATE TABLE strategy_tracking (
    tracking_id    BIGSERIAL PRIMARY KEY,
    strategy_id    BIGINT REFERENCES strategies(strategy_id),
    trade_date     DATE NOT NULL,
    portfolio_value DECIMAL(18,2),
    benchmark_value DECIMAL(18,2),
    daily_return   DECIMAL(8,6),
    cumulative_return DECIMAL(8,4),
    UNIQUE(strategy_id, trade_date)
);

-- 즐겨찾기 (ETF)
CREATE TABLE favorites (
    favorite_id    BIGSERIAL PRIMARY KEY,
    user_id        BIGINT REFERENCES users(user_id),
    etf_ticker     VARCHAR(20) NOT NULL,
    created_at     TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, etf_ticker)
);

-- 인덱스
CREATE INDEX idx_etf_prices_ticker_date ON etf_prices(ticker, trade_date DESC);
CREATE INDEX idx_etf_compositions_ticker ON etf_compositions(etf_ticker, base_date DESC);
CREATE INDEX idx_etf_compositions_component ON etf_compositions(component_ticker);
CREATE INDEX idx_etf_compositions_industry ON etf_compositions(industry_code);
CREATE INDEX idx_etf_cluster_members_ticker ON etf_cluster_members(etf_ticker);
CREATE INDEX idx_portfolios_user ON portfolios(user_id, status);
CREATE INDEX idx_simulations_portfolio ON simulations(portfolio_id, status);
CREATE INDEX idx_simulations_user ON simulations(user_id, created_at DESC);
CREATE INDEX idx_ai_reviews_portfolio ON ai_reviews(portfolio_id);
CREATE INDEX idx_news_keywords ON news_articles USING GIN(keywords);
CREATE INDEX idx_news_published ON news_articles USING BRIN(published_at);
CREATE INDEX idx_news_clusters ON news_articles USING GIN(affected_clusters);
CREATE INDEX idx_strategies_user ON strategies(user_id, is_tracking);
CREATE INDEX idx_strategy_tracking_date ON strategy_tracking(strategy_id, trade_date DESC);
CREATE INDEX idx_favorites_user ON favorites(user_id);
CREATE INDEX idx_etf_list_category ON etf_list(category, is_active);
CREATE INDEX idx_etf_list_strategy ON etf_list(strategy_type, is_active);
CREATE INDEX idx_etf_list_risk ON etf_list(risk_grade, is_active);
CREATE INDEX idx_etf_list_dividend ON etf_list(dividend_freq, is_active);
CREATE INDEX idx_etf_list_manager ON etf_list(asset_manager, is_active);
```

#### 테이블별 데이터 규모

| 테이블 | 건수 | 비고 |
|---|---|---|
| **etf_prices** | **~100만** | 800 ETF × 5년 일봉 = ~100만 (최대 테이블) |
| etf_list | ~800 | 국내 상장 ETF 전체 |
| etf_compositions | ~14만 | 800 ETF × 평균 170 구성종목 |
| etf_clusters | ~20 | 클러스터 수 (재계산 시 갱신) |
| etf_cluster_members | ~800 | ETF당 1건 |
| news_articles | ~50만 | BRIN 인덱스 |
| portfolios | ~수천 | 사용자당 여러 개 |
| simulations | ~수만 | 시뮬레이션 요청 |
| simulation_daily | ~수십만 | 시뮬당 평균 250영업일 |
| ai_reviews | ~수만 | 시뮬레이션당 1건 |
| strategies | ~수천 | 저장된 전략 |

```
총계: 약 165만건+
```

#### 표준산업소분류코드 대분류 매핑

ETF 구성종목의 `industry_code`를 아래 대분류로 그룹핑하여 필터/클러스터링에 활용한다.

| industry_group | 대표 소분류 코드 | 포함 업종 |
|---|---|---|
| **반도체** | 032601 | 반도체 제조업 |
| **전자/IT** | 032602~06 | 전자부품, 컴퓨터, 통신장비, 영상음향 |
| **소프트웨어** | 105802, 106201 | SW 개발/공급, 프로그래밍/SI |
| **바이오/의약** | 032101~03 | 의약물질, 의약품, 의료용품 |
| **자동차/운송장비** | 033001~03, 033101~09 | 자동차, 부품, 선박, 항공 |
| **화학/소재** | 032001~05 | 기초화학, 비료, 합성고무, 화학섬유 |
| **철강/금속** | 032401~03, 032501~09 | 1차 철강, 비철금속, 금속가공 |
| **에너지** | 031901~02, 020501~02 | 석유 정제, 코크스, 석탄, 원유 |
| **유틸리티** | 043501~03 | 전기, 가스, 증기 |
| **금융** | 116401~09 | 은행, 투자기관, 기타 금융 |
| **보험** | 116501~03 | 보험, 재보험, 연금 |
| **건설** | 064101~02, 064201~05 | 건물건설, 토목, 전문공사 |
| **유통/소매** | 074601~09, 074701~09 | 도매, 소매, 전자상거래 |
| **식품/음료** | 031001~08, 031101~02 | 식품 제조, 음료 |
| **통신/미디어** | 106001~02, 106101~02 | 방송, 통신, 우편 |
| **부동산** | 126801~02 | 부동산 임대, 관련 서비스 |
| **기계/장비** | 032901~02 | 일반/특수 목적 기계 |
| **의료장비** | 032701~04 | 의료기기, 정밀기기, 광학기기 |
| **전기장비** | 032801~09 | 전동기, 전지, 조명, 가전 |
| **소비재** | 031301~09, 031401~04, 033201~09 | 섬유, 의복, 가구, 생활용품 |
| **기타** | 그 외 | 매핑 불가 시 |

```
[매핑 방법]
  1. KIS API 구성종목 조회 시 종목코드로 표준산업소분류코드 매핑
     → KIS API 업종분류 코드 or 외부 매핑 테이블 활용
  2. 6자리 소분류코드 → 위 대분류 매핑 룰 적용 → industry_group 저장
  3. 매월 1회 구성종목 갱신 시 함께 갱신

[필터 활용]
  "기초자산 섹터" 필터 = ETF 내 구성종목의 industry_group 비중 최대값 기준
  예: KODEX 반도체 → 구성종목의 80%가 "반도체" → 필터에서 "반도체"로 분류

[클러스터링 활용]
  기존: 구성종목 비중 벡터 코사인 유사도
  추가: 산업분류 비중 벡터 유사도 (20차원 대분류 벡터)
  → 같은 산업 구성이면 클러스터링에서 가까이 배치
```

---

## 3. 외부 API 활용 계획

### 3.1 KIS API (한국투자증권)

```
[공식 포털] https://apiportal.koreainvestment.com
[비용] 무료 (한국투자증권 계좌 필요)
[인증] OAuth 2.0 (AppKey + AppSecret → Access Token, 유효기간 24시간)

[핵심: 국내 ETF 데이터 단일 소스]
  실시간 시세/NAV: WebSocket
  구성종목 비중:   REST (FHKST121600C0)
  과거 일봉:       REST (FHKST01010300)
  거래량/등락률:   REST (FHPST01700000, FHPST01710000)
```

#### 사용할 KIS API 목록

```
[ETF 시세/정보 — 핵심]

  ┌──────────────────────────────────────────────────────────────┐
  │ tr_id         │ 기능                    │ 용도                │
  ├──────────────────────────────────────────────────────────────┤
  │ FHKST01010100 │ 현재가 시세              │ 현재가, 등락률, 거래량│
  │ FHKST01010300 │ 일자별 시세              │ 과거 일봉 → 백테스트 │
  │ FHPST02400000 │ ETF 현재가/NAV          │ NAV, 괴리율, 추적오차│
  │ FHKST121600C0 │ ETF 구성종목 비중(%)    │ ★ 클러스터링 핵심    │
  │ FHPST02440000 │ ETF NAV 비교추이(일별)  │ NAV vs 시장가 차트   │
  └──────────────────────────────────────────────────────────────┘

[순위/분석]

  ┌──────────────────────────────────────────────────────────────┐
  │ tr_id         │ 기능                    │ 용도                │
  ├──────────────────────────────────────────────────────────────┤
  │ FHPST01700000 │ 거래량순위              │ "인기 ETF" 표시      │
  │ FHPST01710000 │ 등락률순위              │ 급등/급락 ETF        │
  │ FHKST01010600 │ 업종현재가              │ 섹터별 상황          │
  └──────────────────────────────────────────────────────────────┘

[실시간 — WebSocket]

  ┌──────────────────────────────────────────────────────────────┐
  │ tr_id       │ 기능                    │ 용도                  │
  ├──────────────────────────────────────────────────────────────┤
  │ H0STCNT0    │ 실시간 체결              │ ETF 현재가 실시간 갱신│
  │ H0STNAV0    │ ETF 실시간 NAV          │ NAV 실시간 갱신       │
  └──────────────────────────────────────────────────────────────┘
```

### 3.2 뉴스 API

```
[네이버 뉴스 검색 API]
  무료 (일 25,000건)
  → 섹터/테마 키워드 기반 검색 ("반도체 ETF", "2차전지", "금리 인하" 등)
  → LLM이 요약 + ETF 클러스터 매핑

[BigKinds]
  과거 뉴스 벌크 수집용 (초기 적재)
  → 경제/산업 카테고리 CSV 다운로드

[뉴스 활용 방식]
  뉴스 예측 X → 뉴스 영향력 매핑 O
  "이 뉴스가 어떤 ETF 클러스터에 영향을 주는지" 시각화
  → 클러스터 맵 위에 뉴스 임팩트를 색상/크기로 표시
```

### 3.3 LLM API

```
[사용 모델] GPT-4o / Gemini / Claude (SSAFY GMS 크레딧)

[용도]
  1. Bull/Bear 리뷰 생성
     → 포트폴리오 구성 + 시뮬레이션 결과 + 시장 상황 → 찬반 의견
  2. 뉴스 요약 + ETF 클러스터 매핑
     → 뉴스 → 어떤 섹터/테마에 영향 → 어떤 클러스터에 매핑
  3. 클러스터 네이밍
     → 클러스터 구성 ETF 목록 → "반도체/AI 성장" 같은 직관적 이름 부여
  4. [Stretch Goal] AI 맞춤 꾸러미 추천
     → 사용자 프로필 + 목표 → 맞춤 ETF 조합 추천
     → 착수 조건: 핵심 5기능(맵/빌더/시뮬/리뷰/전략) 완료 후
     → 수락 기준: 유저 프로필(리스크 수준+투자 기간) 입력 시
                  3개 이상 ETF 조합 + 비중 제안 가능
```

### 3.4 벤치마킹 데이터

```
[funETF] https://www.funetf.com
  국내 ETF 정보 사이트 — 데이터 구조 참고
  ETF 분류체계, 비용 비교 방식 벤치마킹

[All About ETF] (네이버 증권 ETF 섹션)
  섹터/테마 분류 방식 참고
  ETF 비교 UI/UX 벤치마킹
```

---

## 4. AI 모델 상세

### 4.1 AI 기능 총괄

| # | AI 기능 | 역할 | 기술 |
|---|---|---|---|
| 1 | **ETF 클러스터링** | 상관관계 + 구성종목 유사도 → 2D 맵 | 상관 행렬 + 코사인 유사도 + t-SNE/UMAP |
| 2 | **뉴스 영향력 분석** | 뉴스 → 클러스터 매핑 → 맵 하이라이트 | LLM + 키워드 매칭 |
| 3 | **Bull/Bear 리뷰** | 포트폴리오 → 찬성/반대 캐릭터 의견 | LLM 프롬프팅 |
| 4 | **백테스트 엔진** | 과거 데이터 기반 포트폴리오 성과 계산 | Java (Spring Boot 내부 처리) |
| 5 | **[Stretch] AI 맞춤 꾸러미** | 유저 프로필 → ETF 조합 추천 | LLM + 규칙 기반 |

### 4.2 ETF 클러스터링 상세

```
[목적]
  "KODEX 200과 TIGER 200은 비슷한가?" — 800개 ETF의 관계를 한눈에 파악
  단순 카테고리 분류가 아닌, 실제 가격 움직임과 구성종목 유사도 기반 관계도

[2단계 클러스터링]

  ── Step 1: 유사도 행렬 계산 ──

    A. 수익률 상관관계 (Pearson Correlation)
       최근 1년 일별 수익률 간 상관계수 계산
       → 800×800 상관 행렬
       → 같이 오르고 같이 내리는 ETF = 높은 상관

    B. 구성종목 유사도 (Cosine Similarity)
       ETF 구성종목 비중 벡터 간 코사인 유사도
       → KODEX 200과 TIGER 200은 구성종목이 거의 같음 → 유사도 ~0.99
       → KODEX 200과 KODEX 골드선물은 구성종목이 완전 다름 → 유사도 ~0.0

    C. 산업분류 유사도 (Industry Vector Cosine Similarity)
       표준산업소분류코드 대분류(20개 그룹) 비중 벡터 간 코사인 유사도
       → ETF 구성종목의 industry_group별 비중 합산 → 20차원 벡터
       → 같은 산업 구성 ETF끼리 높은 유사도
       → 구성종목이 달라도 같은 산업이면 유사하다고 판단 가능

    D. 통합 유사도
       similarity = α × correlation + β × cosine_similarity + γ × industry_similarity
       α = 0.5, β = 0.3, γ = 0.2 (수익률 상관 > 종목 유사도 > 산업 유사도)
       ※ 가중치는 튜닝 대상

  ── Step 2: 클러스터링 + 차원 축소 ──

    A. 클러스터링: HDBSCAN (밀도 기반, 클러스터 수 자동 결정)
       → 노이즈 ETF(어디에도 속하지 않는)도 자연스럽게 처리
       → 예상 클러스터: 10~20개
         - 국내 대형주 추종 (KODEX 200, TIGER 200, ...)
         - 반도체/AI (KODEX 반도체, TIGER 반도체TOP10, ...)
         - 2차전지/신에너지
         - 채권형 (국고채, 회사채)
         - 원자재 (금, 원유)
         - 배당형
         - 해외주식 추종 (미국 S&P500, 나스닥)
         - ...

    B. 2D 시각화: UMAP (또는 t-SNE)
       고차원 유사도 행렬 → 2D 좌표 (x, y)
       → 가까이 있는 ETF = 유사한 ETF
       → Android 앱에서 인터랙티브 2D 맵으로 표시

[갱신 주기]
  매주 1회 배치 (주말)
  → 최근 1년 수익률 + 최신 구성종목 비중으로 재계산
  → 결과: etf_clusters + etf_cluster_members 테이블 갱신
```

### 4.3 뉴스 영향력 분석 상세

```
[목적]
  "트럼프 관세 뉴스가 떴다 → 어떤 ETF 클러스터가 영향받는가?"
  뉴스를 읽고 → 관련 클러스터를 매핑 → 맵 위에 하이라이트

[흐름]
  1. 네이버 뉴스 API로 경제/산업 뉴스 수집 (10분마다)
  2. LLM에 뉴스 전달:
     "이 뉴스가 영향을 주는 투자 테마/섹터는?
      영향 방향(긍정/부정)과 강도(high/medium/low)는?"
  3. 테마/섹터 → 클러스터 매핑 (키워드 룩업)
     "반도체" → 클러스터 #3 (반도체/AI 성장)
     "금리 인하" → 클러스터 #7 (채권형) + 클러스터 #1 (국내 대형주)
  4. 맵 UI에 반영:
     영향받는 클러스터를 색상(빨강=부정, 초록=긍정)과
     크기(영향도)로 하이라이트

[LLM 프롬프트]
  "금융 뉴스 분석기. 다음 뉴스가 어떤 투자 테마/섹터에 영향을 주는지 판단해.
   JSON으로 응답:
   {
     'themes': ['반도체', '수출'],
     'sentiment': -0.7,
     'impact': 'high',
     'reasoning': '한 줄 근거'
   }"

[데이터 계층 — Tier 2 (이벤트 트리거)]
  뉴스 발생 시 1회 분석 → DB 저장 → 이후 재조회 시 LLM 불필요
```

### 4.4 Bull/Bear 리뷰 상세

```
[목적]
  사용자가 만든 포트폴리오에 대해
  찬성(Bull)과 반대(Bear) 양쪽 관점에서 AI가 리뷰

[트리거]
  사용자가 시뮬레이션 완료 후 "AI 리뷰 요청" 시 생성

[입력 (LLM 프롬프트에 주입)]
  - 포트폴리오 구성: ETF 목록 + 비중
  - 시뮬레이션 결과: 수익률, MDD, 샤프비율
  - 각 ETF의 최근 뉴스 감성 요약
  - 현재 시장 상황 (금리, 환율 등 거시 지표)

[LLM 프롬프트]

  # Role: Bull Analyst (소 캐릭터)
  당신은 낙관적인 투자 분석가입니다.
  주어진 포트폴리오의 긍정적 측면을 3~5가지 제시하세요.
  근거는 구체적 데이터를 기반으로 하되, 전망은 긍정적으로 해석하세요.

  # Role: Bear Analyst (곰 캐릭터)
  당신은 비관적인 투자 분석가입니다.
  주어진 포트폴리오의 리스크와 문제점을 3~5가지 제시하세요.
  근거는 구체적 데이터를 기반으로 하되, 전망은 보수적으로 해석하세요.

[출력 예시]
  🐂 Bull의 의견 (찬성 점수: 72)
  "이 포트폴리오는 반도체와 AI 테마에 집중 투자하고 있어요.
   ① 글로벌 AI 투자 사이클이 지속 중 (데이터센터 CAPEX +30%)
   ② KODEX 반도체의 최근 6개월 수익률이 벤치마크 대비 +12%p
   ③ 채권 20% 편입으로 방어력도 확보
   전체적으로 공격+방어가 균형 잡힌 구성입니다."

  🐻 Bear의 의견 (반대 점수: 58)
  "몇 가지 리스크를 짚어볼게요.
   ① 반도체 비중 40%로 섹터 집중도가 높음 → 조정 시 낙폭 확대
   ② 최근 미국 반도체 수출 규제 확대 이슈
   ③ 채권은 20%인데 금리 인하 속도가 예상보다 느림
   집중도를 낮추고 배당 ETF 편입을 검토해보세요."

[생성 방식]
  Bull/Bear 각각 별도 LLM 호출 (같은 컨텍스트, 다른 역할)
  → 점수는 LLM이 0~100으로 채점
  → 종합 판정(verdict)은 Bull/Bear 점수 차이로 결정
```

### 4.5 [Stretch Goal] AI 맞춤 꾸러미

```
[개요]
  사용자의 투자 목표 + 리스크 성향 → AI가 ETF 조합을 추천
  "목표: 3년 후 3,000만원, 리스크: 중간" → AI가 최적 조합 제안

[고민 포인트]
  - 추천의 책임 문제 → "참고용"으로 명시
  - 단순 규칙 기반 vs LLM 기반 → 하이브리드가 현실적
  - 핵심 루프(직접 탐색→구성→시뮬→검증)와 상충 가능

[구현한다면]
  규칙 기반: 리스크 성향별 자산 배분 (주식 ETF/채권 ETF/원자재 비율)
  LLM 보강: 규칙 결과를 LLM이 현재 시장 상황 고려하여 미세 조정
  → 5주차 이후 여유 있으면 구현
```

### 4.6 데이터 계층 전략 (LLM 호출 최적화)

```
[Tier 1: 사전 생성 (배치 → DB 저장 → 즉시 응답)]

  매주 1회 (주말):
    - ETF 클러스터링 재계산 → etf_clusters + etf_cluster_members 갱신
    - 클러스터 네이밍 (LLM) → cluster_name, cluster_description 갱신
    - 밸류에이션 배치 계산 → etf_list.avg_per, avg_pbr, avg_roe 갱신
      (구성종목별 재무비율 × 비중 가중평균)
    - 위험 분류 갱신 → etf_list.risk_grade, volatility_1y 갱신
      (최근 1년 일별 수익률 표준편차 기반)

  매일 새벽 (장 마감 후):
    - ETF 시세 갱신 → etf_prices 갱신
    - 전략 추적 갱신 → strategy_tracking 갱신
    - 배당률/배당주기 갱신 → etf_list.dividend_yield, dividend_freq 갱신

[Tier 2: 이벤트 트리거 (발생 시 → 분석 → DB 저장)]

  뉴스 발생 시 (10분마다):
    → 뉴스 수집 → LLM 분석 → 클러스터 매핑 → news_articles 저장
    → 관련 종목 추출 → etf_compositions JOIN → 영향 ETF 판정
    → 해당 ETF 포트폴리오 보유 사용자에게 푸시 알림
    → 이후 재조회 시 LLM 불필요

  시뮬레이션 요청 시:
    → 백테스트 연산 → simulation_daily 저장

  AI 리뷰 요청 시:
    → LLM Bull/Bear 생성 → ai_reviews 저장

[Tier 3: 실시간]

  WebSocket 시세 갱신:
    → KIS API → Redis 캐시 → 앱 푸시

  클러스터 맵 인터랙션:
    → DB에서 즉시 조회 (LLM 호출 0회)
```

---

## 5. 핵심 기능 상세

### 5.1 Discovery — ETF 탐색 + 클러스터 맵

```
[기능]
  800개 국내 ETF를 2D 클러스터 맵으로 시각화
  → 가까이 있는 ETF = 비슷하게 움직이는 ETF
  → 클러스터별 색상 + 이름 표시
  → 터치하여 ETF 상세 정보 확인

[Wow Point]
  기존: "KODEX 200, TIGER 200, KBSTAR 200... 뭐가 다르지?"
  WYE:  맵에서 세 개가 거의 같은 위치 → "아, 거의 같은 거구나"
        옆에 KODEX 반도체가 조금 떨어져 있음 → "반도체는 좀 다르게 움직이는구나"

[인터랙션]
  - 핀치 줌: 맵 확대/축소
  - 클러스터 탭: 해당 클러스터 ETF 목록 패널
  - ETF 노드 탭: ETF 상세 (이름, 수익률, 구성종목, 비용)
  - 뉴스 오버레이: 뉴스 영향받는 클러스터 색상 하이라이트

[트리맵 보기 (대안 시각화)]
  거래량/순자산 기반 트리맵
  → 큰 블록 = 거래가 활발하거나 순자산이 큰 ETF
  → 색상 = 당일 등락률 (빨강/초록)

[필터] — 상단 필터 바 + 상세 필터 바텀시트

  ── 상단 퀵 필터 (칩 형태, 한 줄) ──
  카테고리: 국내주식/해외주식/채권/원자재/통화
  테마: 반도체/2차전지/AI/배당/ESG/금리/헬스케어/에너지

  ── 상세 필터 (바텀시트, "필터 더보기" 탭 시) ──

  위험 분류         : 하이리스크 하이리턴 / 위험 분산형 / 안정형
  투자 전략         : 시장 대표 / 테마형 / 배당형 / 채권형 / 커버드콜
  기초자산 섹터     : 반도체/전자IT/SW/바이오의약/자동차/화학소재/철강금속/에너지
                      유틸리티/금융/보험/건설/유통소매/식품음료/통신미디어/소비재 등
                      (표준산업소분류코드 대분류 기반 20개 그룹, 구성종목 비중 최대값으로 분류)
  운용사            : KODEX(삼성) / TIGER(미래에셋) / KBSTAR(KB) / ARIRANG(한화) / SOL(신한) / 기타
  배당률            : 0~5% / 5~10% / 10%+
  배당 주기         : 월 / 분기 / 반기 / 년
  레버리지          : O / X
  인버스            : O / X
  환헤지            : 환헤지(H) / 비헤지(UH) / 해당없음
  P/E (가중평균)    : 10배 이하 / 10~20배 / 20배 이상
  P/B (가중평균)    : 1배 미만 / 1~3배 / 3배 이상
  ROE (가중평균)    : 5% 미만 / 5~15% / 15% 이상
  운용보수(수수료)  : 0.05% 미만 / 0.05~0.5% / 0.5% 이상
  순자산총액(규모)  : 100억 미만 / 100~1000억 / 1000억 이상

  ※ 복수 선택 가능, 선택된 필터는 상단 칩으로 표시
  ※ P/E, P/B, ROE는 구성종목 가중평균 → 주간 배치 계산
  ※ 위험 분류는 최근 1년 변동성 기준 자동 분류
```

### 5.2 Planning — 목표 기반 포트폴리오 구성

```
[기능]
  투자 목표를 정하고 ETF 조합을 직접 구성
  슬라이더로 비중을 조절하면 실시간으로 지표가 변함

[제한]
  계정당 포트폴리오: 최대 10개
  포트폴리오당 ETF: 최대 10종

[흐름]
  1. 목표 설정
     투자 금액(invest_amount): 1,000만원    ← 실제 투입 자금
     목표 금액(goal_amount):   1,300만원    ← 기간 후 도달 희망 금액 (선택)
     투자 기간: 3년
     리스크 수준: 중간 (MODERATE)
     * 목표 금액을 입력하면 필요 연환산 수익률을 자동 계산하여 안내
       예) "목표 달성에 연 9.1% 수익률 필요" → 리스크 감수 기준 제시

  2. ETF 선택
     클러스터 맵에서 ETF 탭 → "포트폴리오에 추가"
     또는 ETF 검색 → 직접 추가

  3. 비중 조절 (슬라이더)
     KODEX 반도체    ████████░░ 40%    [────────●──]
     TIGER 미국S&P500 ██████░░░░ 30%    [──────●────]
     KODEX 국고채10년  ████░░░░░░ 20%    [────●──────]
     KODEX 골드선물    ██░░░░░░░░ 10%    [──●────────]
                                합계: 100%

  4. 실시간 지표 표시 (비중 변경 시 즉시 갱신)
     예상 수익률 (1년): +12.5%
     예상 변동성: 15.2%
     분산도: 높음 (4개 자산군에 걸쳐 분산)
     비용 (총보수): 0.18%

[Wow Point]
  슬라이더를 움직이면 지표가 실시간으로 변함
  → "반도체를 50%로 올리면 수익률은 올라가지만 변동성도 확 올라가는구나"
  → 직관적으로 리스크-리턴 트레이드오프를 체감
```

### 5.3 Simulation — 백테스트

```
[기능]
  구성한 포트폴리오로 "과거에 이렇게 투자했으면 어떻게 됐을까" 시뮬레이션

[백테스트 설정]
  기간: 2023.01 ~ 2025.12 (3년)
  초기 투자금: 10,000,000원
  리밸런싱: 분기별 (매 분기 초 목표 비중으로 복원)
  벤치마크: KODEX 200 (069500)

[결과 대시보드]

  ┌─────────────────────────────────────────────┐
  │  백테스트 결과                                │
  │                                             │
  │  📈 총 수익률:     +38.7% (+3,870,000원)      │
  │  📊 연환산(CAGR):  +11.5%                    │
  │  📉 최대 낙폭(MDD): -18.2%                    │
  │  ⚡ 샤프 비율:      0.85                       │
  │  🔄 변동성:         14.8%                     │
  │                                             │
  │  vs 벤치마크 (KODEX 200):                     │
  │  📊 벤치마크 수익률: +22.1%                    │
  │  ✅ 초과수익(α):    +16.6%p                    │
  │                                             │
  │  [라인 차트: 내 포트폴리오 vs 벤치마크 추이]     │
  │  [파이 차트: 수익 기여도 (ETF별)]              │
  │  [드로다운 차트: 낙폭 추이]                     │
  └─────────────────────────────────────────────┘

[백테스트 엔진 (Java — Spring Boot 내부)]
  1. etf_prices 테이블에서 기간 내 일봉 조회
  2. 포트폴리오 비중 × 일별 수익률 → 가중평균 수익률 (double 배열 연산)
  3. 리밸런싱 주기마다 비중 복원 (거래비용 0.1% 가정)
  4. 누적 수익률, MDD, 샤프비율, 변동성 계산
  5. 벤치마크도 동일 기간으로 계산 → 알파(α) 산출
  ※ Java 21 Virtual Thread로 복수 사용자 동시 백테스트 처리
  ※ 단순 수학 연산이므로 Python 없이 Java로 충분

[수익 기여도 분석]
  각 ETF가 전체 수익에 얼마나 기여했는지 분해
  → "반도체 ETF가 수익의 60%를 만들었지만, 리스크의 70%도 이 ETF"
```

### 5.4 Analysis — AI 뉴스 + Bull/Bear 리뷰

```
[뉴스 영향력 분석]
  클러스터 맵 위에 뉴스 임팩트 오버레이
  → 최근 24시간 뉴스가 어떤 ETF 그룹에 영향을 주는지 시각화
  → 터치하면 해당 뉴스 요약 확인

[뉴스 → ETF 매칭 (구성종목 기반)]
  ETF에 별도 라벨링 하지 않음. 구성종목 데이터(etf_compositions)로 자동 매칭.

  ── 매칭 흐름 ──
  ① 뉴스 수신: "HBM 메모리 수요 폭증"
  ② LLM 분석 → 관련 키워드/종목: ["HBM", "SK하이닉스", "삼성전자"]
  ③ etf_compositions에서 해당 종목 포함 ETF + 비중 조회
     → KODEX 반도체 (SK하이닉스 20%), TIGER 200 (SK하이닉스 5%), ...
  ④ 비중 임계값(예: 10%) 이상인 ETF만 영향 대상으로 판정
  ⑤ 해당 ETF를 포트폴리오에 담은 사용자에게 푸시 알림

  ── 왜 라벨링이 필요 없는가 ──
  "반도체" 안에도 메모리/장비/세라믹 등 세부 분야가 다름.
  섹터 대분류로 라벨링하면 "메모리 뉴스"에 장비 ETF까지 알림이 감.
  → 구성종목 직접 매칭이 가장 정확함.
  → etf_compositions는 월 1회 리밸런싱 시 자동 갱신되므로 유지보수 불필요.

[알림 예시]
  뉴스: "반도체 장비 수주 감소"
  → 관련 종목: 한미반도체, 리노공업
  → 한미반도체 비중 높은 ETF만 매칭 (메모리 위주 ETF는 매칭 X)
  → "회원님의 포트폴리오 내 KODEX 반도체장비 관련 뉴스가 있습니다"

[Bull/Bear 리뷰]
  시뮬레이션 완료 후 요청 시 생성
  → 소(Bull) 캐릭터: 낙관적 분석 3~5가지
  → 곰(Bear) 캐릭터: 비관적 분석 3~5가지
  → 종합 판정: STRONG_BUY ~ STRONG_SELL

[Wow Point]
  기존 로보어드바이저: "이거 좋다" (일방적 의견)
  WYE: "찬성 이유 3가지 vs 반대 이유 3가지 → 당신이 판단하세요"
  → 양면을 모두 보여주므로 사용자가 더 나은 판단을 할 수 있음
```

### 5.5 Strategy — 전략 저장 + 추적

```
[기능]
  마음에 드는 포트폴리오 + 시뮬레이션 결과 + AI 리뷰를 "전략"으로 저장
  저장 이후 실제 시장에서의 움직임을 추적

[저장 시점]
  포트폴리오 구성 + 시뮬레이션 결과 + AI 리뷰 → [전략으로 저장]
  → 저장 시점의 ETF 구성, 비중, 시뮬 지표 스냅샷

[추적]
  저장 시점부터 1년간 고정 추적 (비중 변경 없음, 리밸런싱 없음)
  매일 장 마감 후:
  → 저장 시점 비중 그대로 실제 ETF 시세 적용
  → 일별 평가액 + 수익률 계산
  → 벤치마크 대비 성과 추적
  → 1년 경과 시 추적 자동 종료
  * 비중을 바꾸고 싶으면 새 포트폴리오를 만들어서 새로 시뮬/저장

[출력]
  "3월 15일에 저장한 '반도체+채권 전략':
   저장 이후 20영업일 경과
   현재 수익률: +5.2% (벤치마크: +3.1%)
   당시 Bull 점수: 72 / Bear 점수: 58
   → Bull의 예측이 더 맞아가고 있어요"

[Wow Point]
  단순 과거 백테스트가 아니라
  "내가 이 전략을 실행했으면 지금 어떻게 됐을까"를 실시간으로 추적
  → 시뮬레이션 결과와 실제 결과를 비교하는 학습 도구
```

---

## 6. 모바일 앱 화면 구성

### 6.1 바텀 네비게이션 5탭

```
메인(홈) | 탐색(맵) | 시뮬레이션 | 나의전략 | 마이페이지
```

### 6.2 화면별 구성

```
[탭 1: 메인 (홈)]
  ├─ "What's your ETF" 로고 + 인사
  ├─ 오늘의 ETF 시장 요약 (KOSPI/KOSDAQ 등락, 인기 ETF)
  ├─ 뉴스 영향력 미니맵 (클러스터 맵 축소판, 뉴스 하이라이트)
  ├─ 인기 ETF 카드 리스트 (거래량 상위)
  ├─ 내 전략 요약 (추적 중인 전략 수익률)
  └─ 퀵 액션: "포트폴리오 만들기" 버튼

[탭 2: 탐색 (클러스터 맵)]
  ├─ ETF 클러스터 2D 맵 (전체 화면)
  │   ├─ 핀치 줌 / 스크롤
  │   ├─ 클러스터별 색상 구분 + 이름 라벨
  │   ├─ ETF 노드 (점) — 탭 시 미니 카드
  │   └─ 뉴스 오버레이 토글 (영향받는 클러스터 하이라이트)
  ├─ 상단 필터 바 (퀵 필터 칩)
  │   ├─ 카테고리 (국내주식/해외/채권/원자재)
  │   ├─ 테마 (반도체/2차전지/AI/배당)
  │   └─ [필터 더보기] → 상세 필터 바텀시트
  │       ├─ 위험 분류 / 투자 전략 / 섹터 / 운용사
  │       ├─ 배당률 / 배당 주기
  │       ├─ 레버리지 / 인버스 / 환헤지
  │       ├─ P/E / P/B / ROE (가중평균)
  │       └─ 운용보수 / 순자산총액
  ├─ 하단 바텀시트 (ETF 미니 카드 탭 시)
  │   ├─ ETF 이름 + 현재가 + 등락률
  │   ├─ 1년 수익률 / 총보수 / 순자산
  │   ├─ 구성종목 TOP 5
  │   └─ [상세 보기] [포트폴리오에 추가]
  └─ 트리맵 보기 전환 (거래량/순자산 기준)

[ETF 상세 화면] — 탐색 → ETF 탭 시 진입
  ├─ ETF 이름 + 현재가 + 등락률
  ├─ 일봉 차트 (1M/3M/6M/1Y/3Y)
  ├─ 기본 정보 카드 (총보수, 순자산, 상장일, 운용사)
  ├─ NAV vs 시장가 (괴리율)
  ├─ 구성종목 리스트 (비중 순)
  ├─ 소속 클러스터 정보 ("반도체/AI 성장 클러스터 #3")
  ├─ 같은 클러스터 ETF 리스트 (유사 ETF 추천)
  ├─ 관련 뉴스 리스트
  └─ [즐겨찾기] [포트폴리오에 추가]

[탭 3: 시뮬레이션]
  ├─ 포트폴리오 목록 (내가 만든 포트폴리오)
  │   └─ [+ 새 포트폴리오 만들기]
  ├─ 포트폴리오 상세 (선택 시)
  │   ├─ ETF 구성 + 비중 슬라이더
  │   ├─ 실시간 지표 (예상 수익률, 변동성, 비용)
  │   ├─ [ETF 추가] [ETF 삭제] 버튼
  │   └─ [시뮬레이션 실행] 버튼
  ├─ 시뮬레이션 설정 (실행 전)
  │   ├─ 기간 설정 (시작일~종료일)
  │   ├─ 초기 투자금
  │   ├─ 리밸런싱 주기 (없음/월/분기/연)
  │   └─ 벤치마크 선택
  └─ 시뮬레이션 결과 (완료 후)
      ├─ 성과 지표 대시보드 (수익률, CAGR, MDD, 샤프)
      ├─ 라인 차트 (포트폴리오 vs 벤치마크)
      ├─ 파이 차트 (ETF별 수익 기여도)
      ├─ 드로다운 차트
      ├─ [AI Bull/Bear 리뷰 요청] 버튼
      └─ [전략으로 저장] 버튼

[AI Bull/Bear 리뷰 화면] — 바텀시트 or 풀스크린
  ├─ 🐂 Bull 캐릭터 + 의견 (낙관)
  │   ├─ 찬성 점수 (72점)
  │   └─ 근거 리스트 (3~5개)
  ├─ 🐻 Bear 캐릭터 + 의견 (비관)
  │   ├─ 반대 점수 (58점)
  │   └─ 근거 리스트 (3~5개)
  ├─ 종합 판정 (BUY / NEUTRAL / SELL 등)
  └─ [전략으로 저장]

[탭 4: 나의전략]
  ├─ 저장된 전략 리스트
  │   ├─ 전략 이름 + 저장일
  │   ├─ 추적 수익률 (실시간)
  │   └─ Bull/Bear 점수 뱃지
  ├─ 전략 상세 (선택 시)
  │   ├─ 포트폴리오 구성 (저장 시점)
  │   ├─ 시뮬레이션 결과 요약
  │   ├─ AI Bull/Bear 리뷰 요약
  │   ├─ 추적 라인 차트 (저장 이후 실제 수익률 추이)
  │   └─ "Bull이 맞아가고 있어요" or "Bear가 맞아가고 있어요" 피드백
  └─ 전략 비교 (2개 전략 선택 → 나란히 비교)

[탭 5: 마이페이지]
  ├─ 프로필 (닉네임, 이미지)
  ├─ 즐겨찾기 ETF 리스트
  ├─ 투자 성향 (리스크 수준 설정)
  ├─ 알림 설정 (뉴스 알림, 전략 추적 알림)
  └─ 앱 설정 (테마, 로그아웃)
```

### 6.3 푸시 알림 시나리오

```
[뉴스]   📰 "반도체 수출 규제 완화" — 반도체/AI 클러스터에 긍정 영향
[전략]   📊 '나의 성장형 전략' 추적 수익률 +5% 돌파!
[시장]   📈 오늘 ETF 시장 요약: 거래량 TOP 3 — KODEX 레버리지, TIGER 반도체...
[리뷰]   🐂🐻 AI Bull/Bear 리뷰가 완성되었습니다
```

---

## 7. 데이터 수집 계획

### 7.1 데이터 소스 (전부 무료)

| 소스 | 데이터 | 수집 방법 | 건수 | 용도 |
|---|---|---|---|---|
| **KIS API (ETF)** | ETF 현재가/NAV/구성종목 비중 | REST + WebSocket | ~14만 | 클러스터링/시세/구성종목 |
| **KIS API** | ETF 일자별 시세 (5년) | REST (FHKST01010300) | ~100만 | 백테스트 + 클러스터링 |
| **KIS API** | 거래량/등락률 순위 | REST | 실시간 | 인기 ETF 표시 |
| **BigKinds / 네이버** | 금융 뉴스 | CSV / 검색 API | ~50만 | 뉴스 영향력 분석 |

### 7.2 데이터 규모

```
ETF 일봉 5년치 (800종 × 1,250일)          → 약 100만 row
ETF 구성종목 (800종 × 평균 170종목)         → 약 14만 row
ETF 목록                                   → 약 800 row
뉴스 (경제/산업)                            → 약 50만 row
────────────────────────────────────
총: 약 165만건 (수집 데이터만, 사용자 생성 데이터 별도)
```

### 7.3 수집 방법 상세

#### ETF 목록 + 구성종목 (KIS API)
```python
# ETF 구성종목: KIS API (FHKST121600C0)
def get_etf_components(etf_ticker: str, access_token: str):
    """KIS API로 ETF 구성종목 비중 조회"""
    url = "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/quotations/inquire-etf-component"
    headers = {
        "Authorization": f"Bearer {access_token}",
        "tr_id": "FHKST121600C0",
    }
    params = {"FID_INPUT_ISCD": etf_ticker}
    resp = requests.get(url, headers=headers, params=params)
    return resp.json()["output2"]  # 종목코드 + 종목명 + 비중%

# 국내 상장 ETF ~800종 × 구성종목 = ~14만건
# 매월 1회 갱신 (ETF 리밸런싱 주기에 맞춰)
# 구성종목별 표준산업소분류코드 매핑:
#   → KIS API 업종 코드 or 외부 매핑 테이블로 industry_code 부여
#   → industry_code → industry_group 대분류 변환 후 저장
```

#### ETF 일봉 (KIS API)
```python
# ETF 일자별 시세: KIS API (FHKST01010300)
def get_etf_daily_prices(etf_ticker: str, start_date: str, end_date: str):
    """KIS API로 ETF 일별 시세 조회 (최대 100일씩)"""
    url = "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/quotations/inquire-daily-price"
    headers = {"tr_id": "FHKST01010300"}
    params = {
        "FID_INPUT_ISCD": etf_ticker,
        "FID_INPUT_DATE_1": start_date,
        "FID_INPUT_DATE_2": end_date,
        "FID_PERIOD_DIV_CODE": "D",
    }
    resp = requests.get(url, headers=headers, params=params)
    return resp.json()["output2"]

# 800 ETF × 5년 = 800 × 1,250일 ≈ 100만건
# 초기 수집: KIS REST API (100일씩 페이지네이션)
# Rate Limit: 초당 20건 (실전) → 800 ETF × 13페이지 ≈ 수 시간
# 일일 갱신: 800건 (당일 종가만)
# 상장폐지 감지: 주간 ETF 목록 재수집 시 기존 목록과 diff
#   → 사라진 종목은 etf_list.delisted_date 기록 + is_active=FALSE
#   → 신규 상장 ETF는 자동 추가 + 일봉 수집 시작
```

#### 뉴스 (네이버 + BigKinds)
```
[과거 뉴스 — 초기 적재]
  BigKinds (bigkinds.or.kr): 경제/산업 CSV 다운로드 (~50만건)

[일일 뉴스 — 서비스 운영 중]
  네이버 뉴스 검색 API (일 25,000건)
  → 섹터/테마 키워드 검색: "반도체 ETF", "금리 인하", "2차전지" 등
  → 10분마다 수집 → LLM 분석 → 클러스터 매핑 → DB 저장
```

### 7.4 수집 일정 (1주차)

```
월  KIS API 계정 + AppKey 발급 + 인증 테스트
    ETF 목록 수집 (800종) + etf_list 적재
화  KIS API ETF 구성종목 수집 (FHKST121600C0, 800종)
    → etf_compositions 적재
수  KIS API ETF 일봉 5년치 수집 시작 (100만건)
    → etf_prices 적재 (수 시간 소요, 돌려놓기)
목  BigKinds 뉴스 CSV 다운로드 (~50만건) → news_articles 적재
    데이터 검증 + 결측치 처리
금  클러스터링 첫 실행 (테스트)
    → etf_clusters + etf_cluster_members 생성
→ 1주일이면 전체 데이터 수집 + 초기 클러스터링 완료
```

---

## 8. 기술 스택

| 레이어 | 기술 | 배포 위치 | 선택 근거 |
|---|---|---|---|
| **모바일** | Android (Jetpack Compose, Kotlin) | 팀원 기기 | 선언형 UI, 클러스터 맵 커스텀 렌더링 |
| | WebSocket (OkHttp) | | 실시간 시세 수신 |
| | Canvas (Compose) | | 클러스터 맵 2D 렌더링 |
| **백엔드** | Spring Boot + JPA (Java 21) | EC2 | Virtual Thread 고성능 I/O |
| | Virtual Thread | EC2 | LLM API + KIS API 동시 호출 |
| **시세 수집기** | 경량 서비스 (KIS WebSocket 전용) | EC2 (별도 컨테이너) | 운영서버 재시작과 독립, 상시 시세 수집 |
| **캐시** | Redis | EC2 | 실시간 시세 저장, 캐싱, API Rate Limit |
| **DB** | PostgreSQL 16 | EC2 | JSONB + 병렬쿼리 |
| **AI/ML** | FastAPI + Python (scikit-learn) | EC2 | 클러스터링 전용 (주 1회 배치) |
| | HDBSCAN + UMAP | EC2 | 클러스터링 + 차원 축소 |
| **백테스트** | Java (Spring Boot 내부) | EC2 | double 배열 연산, Virtual Thread 동시 처리 |
| **LLM** | GPT-4o / Gemini / Claude API | 외부 API | Bull/Bear 리뷰, 뉴스 분석 |
| **인프라** | Docker Compose | EC2 | 전체 서비스 단일 서버 |
| **외부 API** | KIS API (한국투자증권) | EC2 | ETF 시세/구성종목/NAV |

### 기술스택 매칭 근거

```
[Canvas (Jetpack Compose)]
  클러스터 맵은 800개 노드를 2D로 렌더링해야 함
  → 기존 차트 라이브러리로는 한계
  → Compose Canvas로 커스텀 렌더링 + 제스처(핀치 줌, 드래그)

[HDBSCAN]
  K-Means: 클러스터 수를 미리 정해야 함
  DBSCAN: 하이퍼파라미터 민감
  HDBSCAN: 클러스터 수 자동 결정 + 노이즈 ETF 자연 처리 → 최적

[UMAP]
  t-SNE: 느림, 재현성 없음
  PCA: 비선형 관계 못 잡음
  UMAP: 빠름 + 글로벌 구조 보존 → 맵 시각화에 적합

[Virtual Thread]
  LLM API 호출(~3초) + KIS API 호출(~1초) + DB 조회가 동시 발생
  → Virtual Thread로 블로킹 I/O 최적화
```

### 인증 및 보안

```
[인증 방식]
  JWT (Access Token + Refresh Token)
  - Access Token:  만료 30분, Authorization 헤더로 전달
  - Refresh Token: 만료 14일, Redis에 저장 + 갱신 시 Rotation

[로그인 지원]
  1. 자체 로그인 (email + password)
     → bcrypt 해싱, users.password_hash 저장
  2. 소셜 로그인 (카카오 / 구글) — 스트레치 목표
     → OAuth2 Authorization Code Flow
     → users.provider / provider_id로 식별

[API 보안]
  - Spring Security + JWT Filter
  - 인증 불필요 엔드포인트: 회원가입, 로그인, ETF 목록 조회 (공개)
  - 인증 필수: 포트폴리오, 시뮬레이션, 전략, AI 리뷰, 마이페이지
```

### API 버전 관리 및 에러 처리

```
[API 버전 관리]
  - 모든 엔드포인트 prefix: /api/v1/...
  - 예: /api/v1/etfs, /api/v1/portfolios, /api/v1/simulations
  - 향후 Breaking Change 발생 시 /api/v2/... 병행 운영

[표준 에러 응답 포맷]
  {
    "status": 404,
    "code": "ETF_NOT_FOUND",
    "message": "해당 ETF를 찾을 수 없습니다.",
    "timestamp": "2026-02-25T10:30:00Z"
  }

[HTTP 상태 코드 규칙]
  200 OK          : 정상 조회/수정
  201 Created     : 생성 성공 (포트폴리오, 전략 등)
  400 Bad Request : 입력값 검증 실패 (비중 합계 ≠ 100% 등)
  401 Unauthorized: JWT 만료/미전송
  404 Not Found   : 리소스 없음
  429 Too Many    : KIS API Rate Limit 전파
  500 Internal    : 서버 오류
```

---

## 9. 팀 구성 및 역할 (6명: 백엔드 3 + Android 3)

### 백엔드 (3명)

| 역할 | 담당 | 주요 업무 |
|---|---|---|
| **BE 리드** | 팀원A | Spring Boot 핵심 API, WebSocket 시세 푸시, Redis, DB 설계/튜닝, Docker 배포 |
| **AI 전담** | 팀원B | ETF 클러스터링 엔진, 백테스트 엔진, 뉴스 분석, LLM 프롬프트 설계, 데이터 수집 |
| **BE + API** | 팀원C | 포트폴리오/시뮬레이션/전략 CRUD, KIS API 연동, 회원 관리, 단위 테스트 |

#### 팀원A (BE 리드) 상세
```
[핵심]
  Spring Boot 아키텍처 설계 (Virtual Thread 기반)
  WebSocket 서버 (KIS API → 앱 시세 푸시)
  Redis 캐시 전략 (시세 캐싱 + KIS API Rate Limiter)
  PostgreSQL 스키마 설계 + 인덱스 튜닝
  EC2 Docker Compose 구성 + 배포
  Spring Batch/Scheduler 배치 작업 관리
```

#### 팀원B (AI 전담) 상세
```
[모델 개발]
  ETF 클러스터링: 상관관계 + 코사인 유사도 → HDBSCAN + UMAP (FastAPI/Python)
  백테스트 엔진:  포트폴리오 비중 × 일별 수익률 → 성과 지표 계산 (Java 로직 설계)
  뉴스 분석:      LLM 뉴스 요약 + 클러스터 매핑
  Bull/Bear 리뷰: LLM 프롬프트 설계 (찬성/반대 캐릭터)
  클러스터 네이밍: LLM으로 클러스터에 직관적 이름 부여

[데이터]
  KIS API ETF 데이터 수집 (목록/구성종목/일봉)
  BigKinds 뉴스 CSV 적재
  배치 스케줄러 연동 (주간 클러스터링, 일일 시세 갱신)

주차별:
  1주차: 데이터 수집 + DB 적재 + 초기 클러스터링 테스트
  2주차: 클러스터링 고도화 + UMAP 시각화 + 백테스트 엔진 v1
  3주차: 뉴스 분석 파이프라인 + 클러스터 매핑 + 백테스트 고도화
  4주차: Bull/Bear 리뷰 LLM 프롬프트 + 뉴스 영향력 분석
  5주차: 전략 추적 로직 + [Stretch] AI 맞춤 꾸러미
  6주차: 최종 튜닝 + 성능 지표 정리
  ※ AI 맞춤 꾸러미는 Stretch Goal — 핵심 5기능 완료 후에만 착수
```

#### 팀원C (BE + API) 상세
```
[담당]
  포트폴리오 CRUD (생성/수정/삭제, ETF 추가/비중 조정)
  시뮬레이션 요청/결과 조회 API
  전략 저장/목록/상세/추적 API
  KIS API 연동 모듈 (인증, 시세 조회, Rate Limiter)
  회원가입/로그인/프로필 API
  즐겨찾기 API

성장 경로:
  1~2주차: Spring Boot 학습 + 회원 CRUD + KIS API 연동
  3~4주차: 포트폴리오 + 시뮬레이션 API + 전략 저장 API
  5주차~:  전략 추적 API + 통합 테스트 + 문서화
```

### Android (3명)

| 역할 | 담당 | 주요 업무 |
|---|---|---|
| **Android 리드** | 팀원D | 앱 아키텍처, 클러스터 맵 (Canvas 렌더링), 메인 홈 화면, 네비게이션 |
| **Android** | 팀원E | 시뮬레이션 화면 (포트폴리오 빌더, 슬라이더, 결과 차트), AI Bull/Bear 리뷰 화면 |
| **Android** | 팀원F | 나의전략 화면, ETF 상세 화면, 마이페이지, 뉴스 피드, 푸시 알림, 온보딩 |

---

## 10. 스프린트 계획

### 0주차 (사전 준비주)

#### 전체 공통
```
□ Git 저장소 + 브랜치 전략 (main / develop / feature/*)
□ Jira/노션 프로젝트 보드 세팅
□ 코드 컨벤션 합의
□ EC2 접속 확인 + Docker 설치
□ UI 와이어프레임 기반 화면 설계 확정
□ 이 기획서 전원 숙지 + 질문 정리
```

#### 팀원A (BE 리드)
```
□ Spring Boot 프로젝트 초기화 (Java 21, Virtual Thread)
□ Docker Compose (PostgreSQL + Redis + FastAPI)
□ EC2 배포 + 동작 확인
□ DB 스키마 SQL 작성 + 테이블 생성
□ API 명세서 초안 (Swagger)
□ WebSocket 엔드포인트 설계
```

#### 팀원B (AI 전담)
```
□ Python 가상환경 (requirements.txt: numpy, pandas, scikit-learn, hdbscan, umap-learn)
□ KIS API 계정 + AppKey 발급 + 인증 테스트
□ BigKinds 계정 생성
□ 외부 LLM API 키 발급 + 테스트 (GPT/Gemini/Claude)
□ KIS API ETF 조회 테스트 (구성종목 3~5개 ETF 테스트 호출)
□ HDBSCAN + UMAP 라이브러리 설치 + 샘플 테스트
```

#### 팀원C (BE + API)
```
□ Spring Boot 기초 학습 (Controller → Service → Repository)
□ KIS API 문서 정독 (ETF 시세 위주)
□ Postman으로 KIS API 호출 테스트
□ 팀원A 프로젝트 clone + 로컬 실행 확인
```

#### 팀원D (Android 리드)
```
□ Android 프로젝트 초기화 (Jetpack Compose + Hilt + Retrofit)
□ Canvas 기반 2D 맵 렌더링 프로토타입 (핀치 줌 + 노드 탭)
□ 바텀 네비게이션 + 화면 라우팅
□ 공통 테마/컬러/타이포 정의
```

#### 팀원E
```
□ Figma: 시뮬레이션 화면, 포트폴리오 빌더, 결과 대시보드, Bull/Bear 리뷰
□ 차트 라이브러리 선정 (MPAndroidChart / Vico)
□ 슬라이더 컴포넌트 프로토타입
```

#### 팀원F
```
□ Figma: 나의전략, ETF 상세, 마이페이지, 뉴스 피드, 온보딩
□ FCM 푸시 알림 기본 설정
□ 리스트/카드 UI 컴포넌트 설계
□ Retrofit + API 통신 기본 구조 작성
```

#### 0주차 산출물 체크리스트
```
✓ EC2 Docker Compose 구동 (PostgreSQL + Redis + FastAPI)
✓ Spring Boot + Android 프로젝트 초기화
✓ DB 스키마 생성 완료
✓ API 키 전부 발급 (KIS, BigKinds, LLM API)
✓ KIS API ETF 조회 테스트 완료
✓ Canvas 2D 맵 렌더링 프로토타입 완료
✓ Figma 와이어프레임 초안
✓ 팀원C: KIS API ETF 시세 조회 테스트 성공
→ 1주차 시작 시 바로 개발 돌입 가능한 상태
```

---

### 1주차: 기반 세팅 + 데이터 수집
```
BE-A(리드) : Docker 환경 확정, Spring Boot API 구조, 인증 모듈
BE-B(AI)  : KIS API ETF 데이터 수집 (목록/구성종목/일봉 100만건)
             BigKinds 뉴스 적재, 초기 클러스터링 테스트
BE-C       : 회원가입/로그인 API, KIS API 연동 모듈 (인증+시세)
AOS-D(리드): 앱 아키텍처, Compose 초기화, 바텀 네비게이션
AOS-E      : 와이어프레임 구체화, 차트 컴포넌트 프로토타입
AOS-F      : 와이어프레임 구체화, 공통 컴포넌트 (카드, 리스트)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
산출물: ERD, API 명세서, 와이어프레임, 개발환경, ETF 데이터 100만건 적재
```

### 2주차: 클러스터링 + 기본 기능
```
BE-A(리드) : ETF 목록/상세 조회 API, WebSocket 시세 푸시 기반, Redis 캐시
BE-B(AI)  : 클러스터링 고도화 (HDBSCAN+UMAP), 백테스트 엔진 v1
BE-C       : 즐겨찾기 API, ETF 검색 API, 포트폴리오 CRUD 기본
AOS-D(리드): 클러스터 맵 Canvas 구현 (노드 렌더링, 핀치 줌, 탭 인터랙션), 메인 홈 화면 레이아웃
AOS-E      : ETF 리스트/검색 화면, 시뮬레이션 화면 기본 구조 설계
AOS-F      : ETF 상세 화면, 마이페이지 기본
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
산출물: 클러스터링 v1, 클러스터 맵 프로토타입, 백테스트 엔진 v1, 기본 CRUD
```

### 3주차: 시뮬레이션 + 뉴스 분석
```
BE-A(리드) : 시뮬레이션 실행/결과 API, 배치 스케줄러 (일일 시세 갱신)
BE-B(AI)  : 백테스트 엔진 고도화 (리밸런싱, 수익기여도), 뉴스 분석 파이프라인
BE-C       : 포트폴리오 빌더 API (ETF 추가/비중 조절/실시간 지표 계산)
AOS-D(리드): 클러스터 맵 ↔ 백엔드 연동, 뉴스 오버레이 기본
AOS-E      : 포트폴리오 빌더 화면 (슬라이더 UI + 실시간 지표)
AOS-F      : 뉴스 피드 화면, 온보딩 화면
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
산출물: 시뮬레이션 API, 뉴스 클러스터 매핑, 포트폴리오 빌더 UI
```

### 4주차: AI Bull/Bear 리뷰 + 통합
```
BE-A(리드) : AI 리뷰 API, 전략 저장 API, 전체 API ↔ 프론트 연동 지원
BE-B(AI)  : Bull/Bear 리뷰 LLM 프롬프트 완성, 뉴스 영향력 분석 고도화, 클러스터 네이밍
BE-C       : 전략 저장/조회/추적 API, 시뮬레이션 결과 조회 고도화
AOS-D(리드): 클러스터 맵 ↔ 뉴스 연동 완성, 메인 홈 화면 완성
AOS-E      : 시뮬레이션 결과 대시보드 (차트 3종), AI Bull/Bear 리뷰 화면
AOS-F      : 나의전략 화면 기본, ETF 상세 ↔ 백엔드 연동
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
산출물: AI Bull/Bear 리뷰, 뉴스 영향력 맵, 전략 저장
```

### 5주차: 전략 추적 + 고도화
```
BE-A(리드) : 전략 추적 배치 (일일 수익률 갱신), 성능 최적화 시작
BE-B(AI)  : 전략 추적 로직, [Stretch] AI 맞춤 꾸러미 프로토타입
BE-C       : 전략 추적 API, 전략 비교 API
AOS-D(리드): 트리맵 보기 구현, 클러스터 맵 고도화 (애니메이션)
AOS-E      : 시뮬레이션 비교 기능, 슬라이더 고도화
AOS-F      : 나의전략 상세 (추적 차트), ETF 상세 고도화
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
산출물: 전략 추적 완성, 전체 핵심 기능 완료
```

### 6주차: 통합 + 품질
```
BE-A(리드) : 전체 API 통합 테스트, DB 튜닝 (EXPLAIN ANALYZE), 성능 최적화
BE-B(AI)  : AI 모델 최종 튜닝, 엣지케이스 처리, 성능 지표 정리
BE-C       : API 문서화, 단위 테스트 보강
AOS-D(리드): 전체 화면 통합, 애니메이션/트랜지션
AOS-E      : 버그 수정, UX 개선
AOS-F      : 버그 수정, 로딩/에러 상태 처리, 알림 최종
전체        : 통합 테스트, 시나리오별 E2E 테스트
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
산출물: 통합 서비스 완성, SQL 튜닝 전/후 비교 자료
```

### 7주차: 발표 준비
```
전체    : 발표 자료 제작, 라이브 데모 준비, 리허설 2회+

데모 시나리오 (발표 스토리라인과 동일 흐름):
  1. 클러스터 맵에서 직접 ETF 탐색
     → 800개 ETF 2D 맵 → "KODEX 200이랑 TIGER 200이 같은 위치!"
     → 필터로 조건 좁히기
  2. ETF 4개 선택 → 포트폴리오 빌더에서 비중 조절
     → 슬라이더 움직이면 지표 실시간 변동
  3. 백테스트 실행 → 3년 성과 결과 대시보드
     → "KODEX 200 대비 +16.6%p 초과 수익!"
  4. AI Bull/Bear 리뷰 요청
     → 소: "반도체 사이클 상승, 분산 투자 Good"
     → 곰: "섹터 집중 리스크, 금리 환경 주의"
  5. 전략으로 저장 → 나의전략 탭에서 추적 수익률 확인
  6. 뉴스 영향력 맵 시연
     → "트럼프 관세" 뉴스 → 수출 클러스터 빨간색 하이라이트
  7. SQL 튜닝 전/후 성능 비교
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
산출물: 최종 발표 + 시연
```

---

## 11. 발표 스토리라인

```
[페르소나: 싸피 교육생 김민수 (25세, 투자 초보)]

1. 문제 제기 — "ETF, 뭘 사야 하죠?"
   "싸피 월 지원금 100만원. ETF로 투자를 시작해보려 합니다."
   "검색하면 800개... 수익률 순으로 나열만 되어 있고."
   "KODEX 200이랑 TIGER 200 뭐가 다른 거야? 어떻게 골라야 해?"

2. ETF 탐색 — "800개 ETF, 관계가 보이면 다르다"
   클러스터 맵 시연: "KODEX 200이랑 TIGER 200이 같은 위치? 거의 같은 거구나"
   필터로 조건 좁히기 → 반도체, 채권, 배당 ETF 발견
   "관계가 보이니까 뭘 골라야 할지 감이 잡힌다"

3. 포트폴리오 구성 + 시뮬레이션
   목표: 100만원, 3년, 리스크 중간
   ETF 4개 선택 → 슬라이더로 비중 조절 → 지표 실시간 변동
   백테스트: 3년 성과 → 수익률, MDD, 샤프비율
   "KODEX 200 대비 +16.6%p 초과 수익"

4. AI Bull/Bear 리뷰 — "찬성과 반대, 양쪽 다 듣고 판단"
   Bull(소): "반도체 사이클 상승, 분산 투자 Good"
   Bear(곰): "섹터 집중 리스크, 금리 환경 주의"
   → 일방적 추천이 아닌 양면 분석

5. 전략 저장 + 추적
   전략으로 저장 → 실제 시장 움직임 추적
   → 나의전략 탭에서 실시간 수익률 확인

6. 뉴스 영향력 맵
   "트럼프 관세" 뉴스 → 수출 클러스터 빨간색 하이라이트
   "내 포트폴리오에 영향이 있나?" 바로 확인

7. 마무리 — 성장 스토리
   "800개 ETF 앞에서 막막했던 초보 투자자가,
    ETF 간의 관계를 이해하고, 직접 전략을 세우고, AI로 검증하는
    똑똑한 투자자로 성장합니다.
    What's your ETF가 그 여정을 함께합니다."

[기술적 성과]
  "100만건 ETF 시세 데이터, HDBSCAN + UMAP 클러스터링"
  "KIS API 실시간 연동, WebSocket 시세 푸시"
  "Virtual Thread (Java 21) 고성능 동시 처리"
  "PostgreSQL 인덱스 튜닝 + 병렬쿼리"
  "외부 LLM API로 Bull/Bear 리뷰 + 뉴스 분석"
```

---

## 12. 리스크 및 대응

| 리스크 | 대응 방안 |
|---|---|
| 클러스터링 품질 (의미 없는 군집) | 유사도 가중치(α) 튜닝 + 클러스터 수 모니터링 + LLM 네이밍으로 직관적 검증 |
| KIS API ETF 일봉 수집 속도 (100만건) | 초기 수집은 수 시간 배치, 이후 일일 갱신은 800건 뿐 |
| KIS API Rate Limit (초당 20건) | Redis Rate Limiter + 실시간은 WebSocket으로 REST 회피 |
| LLM Bull/Bear 리뷰 품질 | Few-shot 프롬프트 + 시뮬 데이터를 컨텍스트에 주입하여 환각 방지 |
| Canvas 맵 성능 (800 노드 렌더링) | 뷰포트 밖 노드 컬링 + 줌 레벨별 상세도 조절 |
| 뉴스 → 클러스터 매핑 정확도 | LLM + 테마 키워드 사전 혼합, 매핑 실패 시 "기타" 처리 |
| 7주 일정 부족 | 핵심 5개(맵/빌더/시뮬/리뷰/전략) 우선, AI 맞춤 꾸러미는 Stretch Goal (5주차 이후 여유 시에만 착수) |
| 백테스트 과적합 (좋은 결과만 보여줌) | MDD·변동성·샤프비율 함께 표시, Bear 리뷰가 리스크 지적 |
| 뉴스 수집 차단 | BigKinds CSV + 네이버 뉴스 API 복수 소스 |
| 외부 LLM API 장애 | Tier 1/2 캐시로 대부분 즉시 응답, LLM은 리뷰/뉴스만 사용 |

---

## 13. 참고 자료

- KIS API 포털: https://apiportal.koreainvestment.com
- funETF: https://www.funetf.com — 국내 ETF 정보 벤치마킹
- HDBSCAN 논문: Campello et al. "Density-Based Clustering" (2013)
- UMAP 논문: McInnes et al. "UMAP: Uniform Manifold Approximation" (2018)
- 「ETF 투자 바이블」 — ETF 투자 전략 참고
- BigKinds: https://www.bigkinds.or.kr
- OpenAI API: https://platform.openai.com
- Google Gemini API: https://ai.google.dev
- Anthropic Claude API: https://docs.anthropic.com
- Jetpack Compose Canvas: https://developer.android.com/develop/ui/compose/graphics/draw/overview
- Redis Documentation: https://redis.io/docs
- Spring Security JWT: https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html
- Docker Compose: https://docs.docker.com/compose

---

> **Note**: 본 문서는 프로젝트 진행에 따라 업데이트됩니다. 변경사항은 Git 커밋 이력을 참고하세요.
