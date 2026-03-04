-- =============================================
-- What's Your ETF ERD (DDL)
-- ERDCloud import용 / PostgreSQL
-- =============================================

-- =============================================
-- 1. 사용자/인증
-- =============================================

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

-- 이메일 인증 토큰
CREATE TABLE "email_verification_token" (
    "id" BIGSERIAL PRIMARY KEY,
    "email" VARCHAR(100) NOT NULL,
    "token" VARCHAR(255) NOT NULL UNIQUE,
    "expires_at" TIMESTAMP NOT NULL,
    "is_verified" BOOLEAN DEFAULT FALSE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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

-- =============================================
-- 2. 뉴스
-- =============================================

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

CREATE TABLE "news_article" (
    "id" BIGSERIAL PRIMARY KEY,
    "title" VARCHAR(500) NOT NULL,
    "content" TEXT,                               -- 뉴스 본문 전체
    "content_summary" JSONB,                      -- AI 핵심 요약 {"bullets": ["...", "...", "..."]}
    "source" VARCHAR(100),                        -- 언론사명
    "source_url" VARCHAR(1000) NOT NULL UNIQUE,   -- 원본 URL
    "thumbnail_url" VARCHAR(1000),
    "category" VARCHAR(50) DEFAULT '금융',         -- 금융 / ETF / 경제
    "keywords" JSONB,                             -- 키워드 태그 ["금리동결", "나스닥", "빅테크"]
    "published_at" TIMESTAMP,
    "view_count" INTEGER DEFAULT 0,
    "is_active" BOOLEAN DEFAULT TRUE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 3. AI 피드백
-- =============================================

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

-- =============================================
-- 4. 알림
-- =============================================

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

CREATE TABLE "etf_alert" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "etf_id" BIGINT,
    "alert_type" VARCHAR(30) NOT NULL,            -- LISTING / DELISTING_SCHEDULED / DELISTING_COMPLETED / REBALANCING_SCHEDULED / REBALANCING_COMPLETED / RETURN_5PCT / RETURN_10PCT / NEWS_RELATED
    "title" VARCHAR(200) NOT NULL,
    "message" TEXT,
    "is_read" BOOLEAN DEFAULT FALSE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_alert_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_alert_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE SET NULL
);

-- =============================================
-- 5. 산업분류 / 회사정보
-- =============================================

CREATE TABLE "industry_classification" (
    "code" VARCHAR(10) PRIMARY KEY,
    "name" VARCHAR(100) NOT NULL,
    "level" INTEGER NOT NULL,                     -- 1=대, 2=중, 3=소, 4=세분류
    "parent_code" VARCHAR(10),
    "group_code" VARCHAR(10),
    "group_name" VARCHAR(50),
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_industry_parent" FOREIGN KEY ("parent_code")
        REFERENCES "industry_classification"("code") ON DELETE SET NULL
);

CREATE TABLE "company_info" (
    "id" BIGSERIAL PRIMARY KEY,
    "stock_code" VARCHAR(20) NOT NULL UNIQUE,
    "stock_name" VARCHAR(100) NOT NULL,
    "market_type" VARCHAR(20),                    -- KOSPI / KOSDAQ / NYSE / NASDAQ 등
    "industry_code" VARCHAR(10),
    "industry_name" VARCHAR(100),
    "industry_group" VARCHAR(50),
    "description" TEXT,
    "listing_date" DATE,
    "fiscal_month" INTEGER,
    "ceo_name" VARCHAR(100),
    "homepage" VARCHAR(200),
    "region" VARCHAR(50),
    "face_value" INTEGER,
    "listed_shares" BIGINT,
    "is_active" BOOLEAN DEFAULT TRUE,
    "data_source" VARCHAR(50) DEFAULT 'KRX',
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_company_industry" FOREIGN KEY ("industry_code")
        REFERENCES "industry_classification"("code") ON DELETE SET NULL
);

CREATE TABLE "stock_prices" (
    "id" BIGSERIAL PRIMARY KEY,
    "company_id" BIGINT NOT NULL,
    "trade_date" DATE NOT NULL,
    "open" DECIMAL(14,2),
    "high" DECIMAL(14,2),
    "low" DECIMAL(14,2),
    "close" DECIMAL(14,2),
    "volume" BIGINT,
    "change_rate" DECIMAL(8,4),
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE("company_id", "trade_date"),
    CONSTRAINT "fk_stock_prices_company" FOREIGN KEY ("company_id") REFERENCES "company_info"("id") ON DELETE CASCADE
);

-- =============================================
-- 6. ETF 공시
-- =============================================

CREATE TABLE "etf_disclosure" (
    "id" BIGSERIAL PRIMARY KEY,
    "etf_code" VARCHAR(20) NOT NULL,
    "etf_name" VARCHAR(200) NOT NULL,
    "disclosure_type" VARCHAR(50) NOT NULL,       -- delisting / liquidation / caution / surveillance
    "disclosure_title" TEXT NOT NULL,
    "disclosure_content" TEXT,
    "disclosure_date" DATE NOT NULL,
    "effective_date" DATE,
    "source_url" TEXT,
    "is_notified" VARCHAR(1) DEFAULT 'N',
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 7. ETF
-- =============================================

CREATE TABLE "etf" (
    "id" BIGSERIAL PRIMARY KEY,
    "stock_code" VARCHAR(20) UNIQUE NOT NULL,
    "name" VARCHAR(200) NOT NULL,
    -- 분류
    "category" VARCHAR(50),                       -- 국내주식형/해외주식형/채권형/원자재형/통화형
    "strategy_type" VARCHAR(30),                  -- MARKET/THEME/DIVIDEND/BOND/DERIVATIVE
    "sector" VARCHAR(50),                         -- 반도체/2차전지/AI/배당 등
    "asset_class" VARCHAR(30),                    -- EQUITY/BOND/COMMODITY/MIXED
    "asset_manager" VARCHAR(50),                  -- KODEX/TIGER/KBSTAR 등
    -- 속성 플래그
    "is_leveraged" BOOLEAN DEFAULT FALSE,
    "is_inverse" BOOLEAN DEFAULT FALSE,
    "is_hedged" BOOLEAN,
    -- 비용/규모
    "expense_ratio" DECIMAL(6,4),
    "nav" DECIMAL(14,2),
    "aum" BIGINT,
    -- 배당
    "dividend_yield" DECIMAL(6,3),
    "dividend_freq" VARCHAR(10),                  -- MONTHLY/QUARTERLY/SEMI_ANNUAL/ANNUAL/NONE
    -- 밸류에이션
    "avg_per" DECIMAL(8,2),
    "avg_pbr" DECIMAL(8,2),
    "avg_roe" DECIMAL(8,2),
    -- 위험 지표
    "risk_grade" VARCHAR(20),                     -- HIGH_RISK/MODERATE/STABLE
    "volatility_1y" DECIMAL(8,4),                 -- 1년 변동성 (%, 연율화)
    -- 생애주기
    "listing_date" DATE,
    "delisted_date" DATE,
    "is_active" BOOLEAN DEFAULT TRUE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "etf_compositions" (
    "id" BIGSERIAL PRIMARY KEY,
    "etf_id" BIGINT NOT NULL,
    "company_id" BIGINT,                          -- company_info FK (NULL = 현금/기타)
    "component_stock_code" VARCHAR(20),
    "weight_pct" DECIMAL(6,3),
    "base_date" DATE NOT NULL,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE("etf_id", "component_stock_code", "base_date"),
    CONSTRAINT "fk_composition_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_composition_company" FOREIGN KEY ("company_id") REFERENCES "company_info"("id") ON DELETE SET NULL
);

CREATE TABLE "etf_prices" (
    "id" BIGSERIAL PRIMARY KEY,
    "etf_id" BIGINT NOT NULL,
    "trade_date" DATE NOT NULL,
    "close" DECIMAL(14,2),
    "nav" DECIMAL(14,2),
    "volume" BIGINT,
    "change_rate" DECIMAL(8,4),
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE("etf_id", "trade_date"),
    CONSTRAINT "fk_etf_prices_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE CASCADE
);

CREATE TABLE "etf_sector_breakdown" (
    "id" BIGSERIAL PRIMARY KEY,
    "etf_id" BIGINT NOT NULL,
    "breakdown_type" VARCHAR(20) NOT NULL,        -- GROUP_CODE / INDUSTRY / SUB_SECTOR
    "industry_code" VARCHAR(10),
    "industry_name" VARCHAR(100),
    "group_code" VARCHAR(20),
    "group_name" VARCHAR(50),
    "sub_sector" VARCHAR(100),
    "weight_pct" DECIMAL(6,3) NOT NULL,
    "stock_count" INTEGER,
    -- 시각화 좌표 (UMAP)
    "pos_x" DECIMAL(10,6),
    "pos_y" DECIMAL(10,6),
    "radius" DECIMAL(10,6),
    "distance_to_center" DECIMAL(10,6),
    "base_date" DATE NOT NULL,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_sector_breakdown_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE CASCADE
);

-- =============================================
-- 8. 뉴스-ETF 영향력
-- =============================================

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
    CONSTRAINT "fk_news_influence_news" FOREIGN KEY ("news_id") REFERENCES "news_article"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_news_influence_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE CASCADE,
    CONSTRAINT "uk_news_etf" UNIQUE ("news_id", "etf_id")
);

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

-- =============================================
-- 9. 사용자 ETF
-- =============================================

CREATE TABLE "user_favorite_etf" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "etf_id" BIGINT NOT NULL,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_favorite_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_favorite_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE CASCADE,
    CONSTRAINT "uk_user_favorite_etf" UNIQUE ("user_id", "etf_id")
);

CREATE TABLE "user_holding_etf" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "etf_id" BIGINT NOT NULL,
    "quantity" INTEGER NOT NULL,
    "avg_price" DECIMAL(15,2),
    "synced_at" TIMESTAMP,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_holding_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_holding_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE CASCADE,
    CONSTRAINT "uk_user_holding_etf" UNIQUE ("user_id", "etf_id")
);

-- =============================================
-- 10. 꾸러미 (시스템 제공 포트폴리오)
-- =============================================

CREATE TABLE "preset_portfolios" (
    "id" BIGSERIAL PRIMARY KEY,
    "name" VARCHAR(100) NOT NULL,
    "short_description" VARCHAR(200),
    "description" TEXT,
    "category" VARCHAR(50),                       -- DIVIDEND/GROWTH/STABLE/THEME
    "display_order" INTEGER DEFAULT 0,
    "is_active" BOOLEAN DEFAULT TRUE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "preset_portfolio_etfs" (
    "id" BIGSERIAL PRIMARY KEY,
    "preset_portfolio_id" BIGINT NOT NULL,
    "etf_id" BIGINT NOT NULL,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE("preset_portfolio_id", "etf_id"),
    CONSTRAINT "fk_preset_portfolio" FOREIGN KEY ("preset_portfolio_id") REFERENCES "preset_portfolios"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_preset_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE CASCADE
);

-- =============================================
-- 11. 사용자 포트폴리오
-- =============================================

CREATE TABLE "portfolios" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "name" VARCHAR(100) NOT NULL,
    "description" TEXT,
    "invest_amount" DECIMAL(18,2),
    "snapshot_etfs" JSONB,
    "snapshot_metrics" JSONB,
    "is_alert_enabled" BOOLEAN DEFAULT FALSE,
    "current_return" DECIMAL(8,4),
    "prev_close_value" DECIMAL(18,2),
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_portfolio_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE
);

CREATE TABLE "portfolio_etfs" (
    "id" BIGSERIAL PRIMARY KEY,
    "portfolio_id" BIGINT NOT NULL,
    "etf_id" BIGINT NOT NULL,
    "weight_pct" DECIMAL(6,3) NOT NULL,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE("portfolio_id", "etf_id"),
    CONSTRAINT "fk_portfolio_etf_portfolio" FOREIGN KEY ("portfolio_id") REFERENCES "portfolios"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_portfolio_etf_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE CASCADE
);
