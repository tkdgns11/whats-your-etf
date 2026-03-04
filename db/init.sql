-- =============================================
-- What's Your ETF ERD (DDL)
-- Docker PostgreSQL 초기화용 / PostgreSQL
-- FK 참조 순서 고려하여 테이블 생성 순서 조정
-- =============================================

-- =============================================
-- 1. 기초 테이블 (참조 없음)
-- =============================================

-- 산업분류 코드 테이블 (셀프 참조 트리 구조)
-- level 1: 대분류 (표준산업분류 알파벳, 예: C=제조업)
-- level 2: 중분류 (표준산업분류 2자리, 예: 26=전자부품/컴퓨터)
-- level 3: 소분류 (표준산업분류 3자리, 예: 261=반도체 제조업)
-- level 4: 세분류 (커스텀, 예: 반도체 ETF 내 세라믹/공정/메모리 등)
CREATE TABLE "industry_classification" (
    "code" VARCHAR(10) PRIMARY KEY,              -- 분류코드 (대: C, 중: 26, 소: 261, 세: SEMI_CER 등)
    "name" VARCHAR(100) NOT NULL,                -- 분류명 (예: 제조업, 반도체 제조업, 세라믹 등)
    "level" INTEGER NOT NULL,                    -- 분류 단계 (1=대, 2=중, 3=소, 4=세분류)
    "parent_code" VARCHAR(10),                   -- 상위 분류 코드 (셀프 참조, 대분류는 NULL)
    "group_code" VARCHAR(10),                    -- 대분류 그룹 코드 (자체 정의, 예: IT_SEMI)
    "group_name" VARCHAR(50),                    -- 대분류 그룹명 (예: 반도체)
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "fk_industry_parent" FOREIGN KEY ("parent_code")
        REFERENCES "industry_classification"("code") ON DELETE SET NULL
);

CREATE INDEX "idx_industry_parent" ON "industry_classification"("parent_code");
CREATE INDEX "idx_industry_level" ON "industry_classification"("level");
CREATE INDEX "idx_industry_group" ON "industry_classification"("group_code");

COMMENT ON TABLE "industry_classification" IS '산업분류 코드 (셀프참조 트리, 대/중/소/세분류)';

-- 사용자
CREATE TABLE "user" (
    "id" BIGSERIAL PRIMARY KEY,
    "email" VARCHAR(100) NOT NULL UNIQUE,
    "password" VARCHAR(255),                      -- 비밀번호 (nullable, 소셜만 사용 시 NULL)
    "nickname" VARCHAR(50) UNIQUE,                -- 닉네임 (신규 가입 시 이메일로 설정)
    "is_active" BOOLEAN DEFAULT TRUE,
    "last_login_at" TIMESTAMP,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ETF 목록 (국내 상장 ETF ~800종)
CREATE TABLE "etf" (
    "id" BIGSERIAL PRIMARY KEY,
    "stock_code" VARCHAR(20) UNIQUE NOT NULL,         -- ETF 코드 (069500, 102110 등)
    "name" VARCHAR(200) NOT NULL,                 -- KODEX 200, TIGER 200 등
    -- 분류
    "category" VARCHAR(50),                       -- 국내주식형/해외주식형/채권형/원자재형/통화형 등
    "strategy_type" VARCHAR(30),                  -- MARKET/THEME/DIVIDEND/BOND/DERIVATIVE
    "sector" VARCHAR(50),                         -- 반도체/2차전지/AI/배당/ESG/금리/헬스케어/소비재/금융 등
    "asset_class" VARCHAR(30),                    -- EQUITY/BOND/COMMODITY/MIXED
    "asset_manager" VARCHAR(50),                  -- 삼성(KODEX)/미래에셋(TIGER)/KB(KBSTAR) 등
    -- 속성 플래그
    "is_leveraged" BOOLEAN DEFAULT FALSE,         -- 레버리지 ETF 여부
    "is_inverse" BOOLEAN DEFAULT FALSE,           -- 인버스 ETF 여부
    "is_hedged" BOOLEAN,                          -- 환헤지 여부 (NULL=해당없음, TRUE=H, FALSE=UH)
    -- 비용/규모
    "expense_ratio" DECIMAL(6,4),                 -- 총보수 (%)
    "nav" DECIMAL(14,2),                          -- 최근 NAV
    "aum" BIGINT,                                 -- 순자산총액 (원)
    -- 배당
    "dividend_yield" DECIMAL(6,3),                -- 배당률 (%)
    "dividend_freq" VARCHAR(10),                  -- MONTHLY/QUARTERLY/SEMI_ANNUAL/ANNUAL/NONE
    -- 밸류에이션 (구성종목 가중평균, 배치 계산)
    "avg_per" DECIMAL(8,2),                       -- 가중평균 P/E
    "avg_pbr" DECIMAL(8,2),                       -- 가중평균 P/B
    "avg_roe" DECIMAL(8,2),                       -- 가중평균 ROE (%)
    -- 위험 지표
    "risk_grade" VARCHAR(20),                     -- 위험등급 (HIGH_RISK/MODERATE/STABLE), volatility_1y 기반 산출
    "volatility_1y" DECIMAL(8,4),                 -- 1년 변동성 (%, 일일수익률 표준편차 × √252 연율화)
    -- 생애주기
    "listing_date" DATE,
    "delisted_date" DATE,                         -- 상장폐지일 (NULL이면 현재 상장 중)
    "is_active" BOOLEAN DEFAULT TRUE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX "idx_etf_category" ON "etf"("category", "is_active");
CREATE INDEX "idx_etf_strategy" ON "etf"("strategy_type", "is_active");
CREATE INDEX "idx_etf_risk" ON "etf"("risk_grade", "is_active");
CREATE INDEX "idx_etf_dividend" ON "etf"("dividend_freq", "is_active");
CREATE INDEX "idx_etf_manager" ON "etf"("asset_manager", "is_active");

-- 상장 회사 정보 테이블 (약 2,500개)
CREATE TABLE "company_info" (
    "id" BIGSERIAL PRIMARY KEY,
    "stock_code" VARCHAR(20) NOT NULL UNIQUE,    -- 종목코드 (6자리, 예: 005930)
    "stock_name" VARCHAR(100) NOT NULL,          -- 종목명 (예: 삼성전자)
    "market_type" VARCHAR(20),                   -- KOSPI / KOSDAQ / KONEX

    -- 산업분류
    "industry_code" VARCHAR(10),                 -- 표준산업소분류코드 (6자리)
    "industry_name" VARCHAR(100),                -- 산업분류명
    "industry_group" VARCHAR(50),                -- 대분류 그룹명 (반도체, 전자/IT 등)

    -- 기업 개요
    "description" TEXT,                          -- 회사 설명/사업 내용
    "listing_date" DATE,                         -- 상장일
    "fiscal_month" INTEGER,                      -- 결산월 (12 = 12월 결산)
    "ceo_name" VARCHAR(100),                     -- 대표자명
    "homepage" VARCHAR(200),                     -- 홈페이지 URL
    "region" VARCHAR(50),                        -- 지역 (서울, 경기 등)

    -- 기본 정보 (KRX에서 제공)
    "face_value" INTEGER,                        -- 액면가
    "listed_shares" BIGINT,                      -- 상장주식수

    -- 메타
    "is_active" BOOLEAN DEFAULT TRUE,            -- 상장 유지 여부
    "data_source" VARCHAR(50) DEFAULT 'KRX',     -- 데이터 소스
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "fk_company_industry" FOREIGN KEY ("industry_code")
        REFERENCES "industry_classification"("code") ON DELETE SET NULL
);

CREATE INDEX "idx_company_stock_code" ON "company_info"("stock_code");
CREATE INDEX "idx_company_industry_code" ON "company_info"("industry_code");
CREATE INDEX "idx_company_industry_group" ON "company_info"("industry_group");
CREATE INDEX "idx_company_market" ON "company_info"("market_type");

COMMENT ON TABLE "company_info" IS '상장 회사 정보 (ETF 구성종목 JOIN용)';

-- 주식 일별 시세
CREATE TABLE "stock_prices" (
    "id" BIGSERIAL PRIMARY KEY,
    "company_id" BIGINT NOT NULL,                 -- company_info.id FK
    "trade_date" DATE NOT NULL,
    "open" DECIMAL(14,2),                         -- 시가
    "high" DECIMAL(14,2),                         -- 고가
    "low" DECIMAL(14,2),                          -- 저가
    "close" DECIMAL(14,2),                        -- 종가
    "volume" BIGINT,                              -- 거래량
    "change_rate" DECIMAL(8,4),                   -- 등락률
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE("company_id", "trade_date"),
    CONSTRAINT "fk_stock_prices_company" FOREIGN KEY ("company_id") REFERENCES "company_info"("id") ON DELETE CASCADE
);

CREATE INDEX "idx_stock_prices_company_date" ON "stock_prices"("company_id", "trade_date" DESC);

-- 뉴스 소스 (언론사) 관리
CREATE TABLE "news_source" (
    "id" BIGSERIAL PRIMARY KEY,
    "name" VARCHAR(50) NOT NULL,                  -- 언론사명 (한국경제, 서울경제 등)
    "domain" VARCHAR(100) NOT NULL UNIQUE,        -- 도메인 (hankyung.com, sedaily.com 등)
    "is_content_available" BOOLEAN DEFAULT TRUE,  -- 본문 크롤링 가능 여부
    "last_success_at" TIMESTAMP,                  -- 마지막 본문 크롤링 성공 시점
    "last_failure_at" TIMESTAMP,                  -- 마지막 본문 크롤링 실패 시점
    "failure_count" INTEGER DEFAULT 0,            -- 연속 실패 횟수
    "css_selector" TEXT,                          -- 본문 CSS 선택자
    "notes" TEXT,                                 -- 비고 (차단 사유 등)
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX "idx_news_source_domain" ON "news_source"("domain");
CREATE INDEX "idx_news_source_available" ON "news_source"("is_content_available") WHERE "is_content_available" = TRUE;

COMMENT ON TABLE "news_source" IS '뉴스 소스 (언론사) 관리 - 본문 크롤링 가능 여부 자동 관리';

-- 뉴스 기사
CREATE TABLE "news_article" (
    "id" BIGSERIAL PRIMARY KEY,
    "title" VARCHAR(500) NOT NULL,
    "content" TEXT,                               -- 뉴스 본문 전체
    "content_summary" JSONB,                      -- AI 요약 {"bullets": ["요약1", "요약2", "요약3"]}
    "source" VARCHAR(100),                        -- 언론사명
    "source_url" VARCHAR(1000) NOT NULL UNIQUE,   -- 원본 URL
    "thumbnail_url" VARCHAR(1000),
    "category" VARCHAR(50) DEFAULT '금융',         -- 금융 / ETF / 경제
    "keywords" JSONB,                             -- 검색 키워드 배열
    "published_at" TIMESTAMP,
    "view_count" INTEGER DEFAULT 0,
    "is_active" BOOLEAN DEFAULT TRUE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX "idx_news_published_at" ON "news_article"("published_at" DESC);
CREATE INDEX "idx_news_category" ON "news_article"("category");
CREATE INDEX "idx_news_keywords" ON "news_article" USING GIN("keywords");

-- ETF 공시 정보 (상장폐지 알림용)
CREATE TABLE "etf_disclosure" (
    "id" BIGSERIAL PRIMARY KEY,
    "etf_code" VARCHAR(20) NOT NULL,             -- ETF 종목코드
    "etf_name" VARCHAR(200) NOT NULL,            -- ETF 종목명
    "disclosure_type" VARCHAR(50) NOT NULL,      -- delisting / liquidation / caution / surveillance
    "disclosure_title" TEXT NOT NULL,            -- 공시 제목
    "disclosure_content" TEXT,                   -- 공시 내용 요약
    "disclosure_date" DATE NOT NULL,             -- 공시일
    "effective_date" DATE,                       -- 효력 발생일 (상장폐지일 등)
    "source_url" TEXT,                           -- KIND 원문 URL
    "is_notified" VARCHAR(1) DEFAULT 'N',        -- 알림 발송 여부
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX "idx_disclosure_etf_code" ON "etf_disclosure"("etf_code");
CREATE INDEX "idx_disclosure_type" ON "etf_disclosure"("disclosure_type");
CREATE INDEX "idx_disclosure_date" ON "etf_disclosure"("disclosure_date" DESC);
CREATE INDEX "idx_disclosure_notified" ON "etf_disclosure"("is_notified") WHERE "is_notified" = 'N';

COMMENT ON TABLE "etf_disclosure" IS 'ETF 공시 정보 (KRX KIND 크롤링)';

-- 이메일 인증 토큰
CREATE TABLE "email_verification_token" (
    "id" BIGSERIAL PRIMARY KEY,
    "email" VARCHAR(100) NOT NULL,
    "token" VARCHAR(255) NOT NULL UNIQUE,
    "expires_at" TIMESTAMP NOT NULL,
    "is_verified" BOOLEAN DEFAULT FALSE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 2. user 참조 테이블
-- =============================================

-- 사용자 소셜 계정 연동
CREATE TABLE "user_social_account" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "provider" VARCHAR(20) NOT NULL,              -- KAKAO
    "provider_user_id" VARCHAR(100) NOT NULL,     -- 소셜 서비스에서의 사용자 ID
    "email" VARCHAR(100),                         -- 소셜 계정 이메일
    "is_primary" BOOLEAN DEFAULT FALSE,           -- 주 로그인 수단 여부
    "linked_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_social_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE,
    CONSTRAINT "uk_provider_user" UNIQUE ("provider", "provider_user_id")
);

-- 리프레시 토큰
CREATE TABLE "refresh_token" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "token" VARCHAR(500) NOT NULL UNIQUE,
    "expires_at" TIMESTAMP NOT NULL,
    "is_revoked" BOOLEAN DEFAULT FALSE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_refresh_token_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE
);

-- 비밀번호 재설정 토큰
CREATE TABLE "password_reset_token" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "token" VARCHAR(255) NOT NULL UNIQUE,
    "expires_at" TIMESTAMP NOT NULL,
    "is_used" BOOLEAN DEFAULT FALSE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_reset_token_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE
);

-- 로그인 이력
CREATE TABLE "login_history" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "provider" VARCHAR(20) NOT NULL,              -- KAKAO / EMAIL
    "login_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "ip_address" VARCHAR(45),
    "device_info" VARCHAR(200),
    CONSTRAINT "fk_login_history_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE
);

-- AI 프롬프트 관리
CREATE TABLE "ai_prompt" (
    "id" BIGSERIAL PRIMARY KEY,
    "name" VARCHAR(50) NOT NULL,                  -- 'portfolio_feedback', 'etf_analysis'
    "version" VARCHAR(20) NOT NULL,               -- 'v1.0', 'v1.1'
    "prompt_template" TEXT NOT NULL,              -- 프롬프트 내용
    "description" VARCHAR(200),                   -- 변경 사항 메모
    "is_active" BOOLEAN DEFAULT FALSE,            -- 현재 활성 버전
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "uk_prompt_version" UNIQUE ("name", "version")
);

CREATE INDEX "idx_prompt_active" ON "ai_prompt"("name", "is_active") WHERE "is_active" = TRUE;

-- 포트폴리오 AI 피드백
CREATE TABLE "portfolio_ai_feedback" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "portfolio_snapshot_id" BIGINT,               -- 포트폴리오 스냅샷 ID
    "prompt_id" BIGINT,                           -- 사용된 프롬프트 FK
    -- 진단 결과 헤드라인
    "headline" VARCHAR(100),                      -- "공격적인 수익 추구!"
    "sub_headline" VARCHAR(200),                  -- "기술주 중심의 로켓 포트폴리오"
    "keywords" JSONB,                             -- ["기술주집중", "고변동성", "성장중심"]
    -- 상세 분석
    "analysis" TEXT,                              -- 종합 분석 결과 (요약 상세)
    "llm_model" VARCHAR(50),                      -- 사용된 LLM 모델
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_ai_feedback_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_ai_feedback_prompt" FOREIGN KEY ("prompt_id") REFERENCES "ai_prompt"("id")
);

CREATE INDEX "idx_ai_feedback_user" ON "portfolio_ai_feedback"("user_id");
CREATE INDEX "idx_ai_feedback_created" ON "portfolio_ai_feedback"("created_at" DESC);

-- 알림 설정
CREATE TABLE "notification_setting" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL UNIQUE,
    -- ETF 알림
    "etf_listing_alert" BOOLEAN DEFAULT TRUE,          -- ETF 상장 알림
    "etf_delisting_alert" BOOLEAN DEFAULT TRUE,        -- ETF 상장폐지 알림 (예정+완료)
    -- 포트폴리오 알림
    "portfolio_rebalance_alert" BOOLEAN DEFAULT TRUE,  -- 리밸런싱 알림 (예정+완료)
    "portfolio_return_alert" BOOLEAN DEFAULT TRUE,     -- 수익률 알림 (5%, 10%)
    -- 뉴스 알림
    "news_alert" BOOLEAN DEFAULT TRUE,                 -- 관심 ETF 관련 뉴스
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_notification_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE
);

-- FCM 토큰 (푸시 알림용)
CREATE TABLE "fcm_token" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "token" VARCHAR(500) NOT NULL,
    "device_type" VARCHAR(20),                    -- ANDROID / IOS / WEB
    "is_active" BOOLEAN DEFAULT TRUE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_fcm_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE
);

CREATE INDEX "idx_fcm_user" ON "fcm_token"("user_id");
CREATE INDEX "idx_fcm_token" ON "fcm_token"("token");

-- 꾸러미 (시스템 제공 예시 포트폴리오 - ETF 목록만 추천, 비중은 사용자 결정)
CREATE TABLE "preset_portfolios" (
    "id" BIGSERIAL PRIMARY KEY,
    "name" VARCHAR(100) NOT NULL,                 -- "배당 성장형 꾸러미"
    "short_description" VARCHAR(200),             -- 카드에 표시할 짧은 설명
    "description" TEXT,                           -- 상세 설명
    "category" VARCHAR(50),                       -- 배당/성장/안정/테마 등
    "display_order" INTEGER DEFAULT 0,            -- 노출 순서
    "is_active" BOOLEAN DEFAULT TRUE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 꾸러미 ETF 구성 (ETF 목록만, 비중은 사용자 결정)
CREATE TABLE "preset_portfolio_etfs" (
    "id" BIGSERIAL PRIMARY KEY,
    "preset_portfolio_id" BIGINT NOT NULL,
    "etf_id" BIGINT NOT NULL,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE("preset_portfolio_id", "etf_id"),
    CONSTRAINT "fk_preset_portfolio" FOREIGN KEY ("preset_portfolio_id") REFERENCES "preset_portfolios"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_preset_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE CASCADE
);

-- 사용자 포트폴리오 (계정당 최대 10개)
CREATE TABLE "portfolios" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "name" VARCHAR(100) NOT NULL,                 -- "나의 성장형 포트폴리오"
    "description" TEXT,
    -- 설정
    "invest_amount" DECIMAL(18,2),                -- 투자 금액
    -- 저장 시점 스냅샷
    "snapshot_etfs" JSONB,                        -- 저장 시점 ETF 구성 + 비중
    "snapshot_metrics" JSONB,                     -- 저장 시점 시뮬 지표
    -- 알림
    "is_alert_enabled" BOOLEAN DEFAULT FALSE,     -- 알림 허용 여부
    "current_return" DECIMAL(8,4),                -- 현재 수익률
    "prev_close_value" DECIMAL(18,2),             -- 전일 종가 (포트폴리오 평가액)
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_portfolio_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE
);

CREATE INDEX "idx_portfolios_user" ON "portfolios"("user_id");

-- =============================================
-- 3. etf 참조 테이블
-- =============================================

-- 뉴스-ETF 영향력 매핑
CREATE TABLE "news_etf_influence" (
    "id" BIGSERIAL PRIMARY KEY,
    "news_id" BIGINT NOT NULL,
    "etf_id" BIGINT NOT NULL,                     -- ETF 테이블 FK
    -- 영향력 점수
    "influence_score" DECIMAL(5,4),               -- 영향력 점수 (0.0000 ~ 1.0000)
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
    CONSTRAINT "fk_news_influence_news" FOREIGN KEY ("news_id") REFERENCES "news_article"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_news_influence_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE CASCADE,
    CONSTRAINT "uk_news_etf" UNIQUE ("news_id", "etf_id")
);

CREATE INDEX "idx_news_etf_etf" ON "news_etf_influence"("etf_id");
CREATE INDEX "idx_news_etf_influence_score" ON "news_etf_influence"("influence_score" DESC);

-- 뉴스-산업 영향력 (1차 분석: 뉴스 → 산업 매핑)
CREATE TABLE "news_industry_influence" (
    "id" BIGSERIAL PRIMARY KEY,
    "news_id" BIGINT NOT NULL,
    "industry_code" VARCHAR(10) NOT NULL,         -- industry_classification FK
    "relevance_score" DECIMAL(5,4),               -- 0.0 ~ 1.0 (관련도)
    "sentiment" VARCHAR(20),                      -- POSITIVE / NEGATIVE / NEUTRAL
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_news_industry_news" FOREIGN KEY ("news_id") REFERENCES "news_article"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_news_industry_code" FOREIGN KEY ("industry_code") REFERENCES "industry_classification"("code") ON DELETE CASCADE,
    CONSTRAINT "uk_news_industry" UNIQUE ("news_id", "industry_code")
);

CREATE INDEX "idx_news_industry_news" ON "news_industry_influence"("news_id");
CREATE INDEX "idx_news_industry_code" ON "news_industry_influence"("industry_code");

-- 관심 ETF (좋아요)
CREATE TABLE "user_favorite_etf" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "etf_id" BIGINT NOT NULL,                     -- ETF 테이블 FK
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_favorite_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_favorite_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE CASCADE,
    CONSTRAINT "uk_user_favorite_etf" UNIQUE ("user_id", "etf_id")
);

CREATE INDEX "idx_favorite_user" ON "user_favorite_etf"("user_id");

-- 마이데이터 보유 ETF
CREATE TABLE "user_holding_etf" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "etf_id" BIGINT NOT NULL,                     -- ETF 테이블 FK
    "quantity" INTEGER NOT NULL,                  -- 보유 수량
    "avg_price" DECIMAL(15,2),                    -- 평균 매입가
    "synced_at" TIMESTAMP,                        -- 마이데이터 동기화 시점
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_holding_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_holding_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE CASCADE,
    CONSTRAINT "uk_user_holding_etf" UNIQUE ("user_id", "etf_id")
);

CREATE INDEX "idx_holding_user" ON "user_holding_etf"("user_id");

-- ETF 알림
CREATE TABLE "etf_alert" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "etf_id" BIGINT,                              -- ETF 테이블 FK (NULL이면 전체 알림)
    "alert_type" VARCHAR(30) NOT NULL,            -- LISTING / DELISTING_SCHEDULED / DELISTING_COMPLETED / REBALANCING_SCHEDULED / REBALANCING_COMPLETED / RETURN_5PCT / RETURN_10PCT / NEWS_RELATED
    "title" VARCHAR(200) NOT NULL,
    "message" TEXT,
    "is_read" BOOLEAN DEFAULT FALSE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_alert_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_alert_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE SET NULL
);

CREATE INDEX "idx_alert_user" ON "etf_alert"("user_id");
CREATE INDEX "idx_alert_created" ON "etf_alert"("created_at" DESC);
CREATE INDEX "idx_alert_unread" ON "etf_alert"("user_id", "is_read") WHERE "is_read" = FALSE;

-- ETF 섹터 분포 (구성종목 산업별 집계)
CREATE TABLE "etf_sector_breakdown" (
    "id" BIGSERIAL PRIMARY KEY,
    "etf_id" BIGINT NOT NULL,
    "breakdown_type" VARCHAR(20) NOT NULL,        -- GROUP_CODE / INDUSTRY / SUB_SECTOR
    "industry_code" VARCHAR(10),                  -- KSIC 산업코드
    "industry_name" VARCHAR(100),                 -- 산업명
    "group_code" VARCHAR(20),                     -- 그룹코드 (13개)
    "group_name" VARCHAR(50),                     -- 그룹명
    "sub_sector" VARCHAR(100),                    -- 세부 섹터명 (테마ETF용)
    "weight_pct" DECIMAL(6,3) NOT NULL,           -- 비중 (%)
    "stock_count" INTEGER,                        -- 해당 섹터 종목 수
    -- 시각화 좌표 (UMAP)
    "pos_x" DECIMAL(10,6),                        -- 버블 X 좌표
    "pos_y" DECIMAL(10,6),                        -- 버블 Y 좌표
    "radius" DECIMAL(10,6),                       -- 버블 반지름
    "distance_to_center" DECIMAL(10,6),           -- ETF 중심까지 거리
    "base_date" DATE NOT NULL,                    -- 기준일
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_sector_breakdown_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE CASCADE
);

CREATE INDEX "idx_sector_breakdown_etf" ON "etf_sector_breakdown"("etf_id");
CREATE INDEX "idx_sector_breakdown_date" ON "etf_sector_breakdown"("etf_id", "base_date" DESC);

-- ETF 구성종목
CREATE TABLE "etf_compositions" (
    "id" BIGSERIAL PRIMARY KEY,
    "etf_id" BIGINT NOT NULL,                     -- ETF 테이블 FK
    "company_id" BIGINT,                          -- company_info FK (NULL = 현금/기타)
    "component_stock_code" VARCHAR(20),           -- 종목코드 또는 CASH/ETC 등
    "weight_pct" DECIMAL(6,3),                    -- 비중 (%)
    "base_date" DATE NOT NULL,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE("etf_id", "component_stock_code", "base_date"),
    CONSTRAINT "fk_composition_etf" FOREIGN KEY ("etf_id")
        REFERENCES "etf"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_composition_company" FOREIGN KEY ("company_id")
        REFERENCES "company_info"("id") ON DELETE SET NULL
);

CREATE INDEX "idx_etf_compositions_etf" ON "etf_compositions"("etf_id", "base_date" DESC);
CREATE INDEX "idx_etf_compositions_company" ON "etf_compositions"("company_id");

-- ETF 일별 시세 (클러스터링 + 백테스트용)
CREATE TABLE "etf_prices" (
    "id" BIGSERIAL PRIMARY KEY,
    "etf_id" BIGINT NOT NULL,                     -- ETF 테이블 FK
    "trade_date" DATE NOT NULL,
    "close" DECIMAL(14,2),                        -- 종가
    "nav" DECIMAL(14,2),                          -- 순자산가치
    "volume" BIGINT,
    "change_rate" DECIMAL(8,4),                   -- 등락률: (당일종가 - 전일종가) / 전일종가 * 100
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE("etf_id", "trade_date"),
    CONSTRAINT "fk_etf_prices_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE CASCADE
);

CREATE INDEX "idx_etf_prices_etf_date" ON "etf_prices"("etf_id", "trade_date" DESC);

-- 포트폴리오 내 ETF 구성 (포트폴리오당 최대 10개 ETF)
CREATE TABLE "portfolio_etfs" (
    "id" BIGSERIAL PRIMARY KEY,
    "portfolio_id" BIGINT NOT NULL,
    "etf_id" BIGINT NOT NULL,                     -- ETF 테이블 FK
    "weight_pct" DECIMAL(6,3) NOT NULL,           -- 비중 (%, 합 = 100)
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE("portfolio_id", "etf_id"),
    CONSTRAINT "fk_portfolio_etf_portfolio" FOREIGN KEY ("portfolio_id") REFERENCES "portfolios"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_portfolio_etf_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE CASCADE
);


