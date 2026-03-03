-- =============================================
-- What's Your ETF ERD (DDL)
-- ERDCloud import용 / PostgreSQL
-- 담당: 윤상훈 (사용자/인증, 뉴스, AI 피드백)
-- =============================================

-- =============================================
-- 1. 사용자/인증
-- =============================================

CREATE TABLE "user" (
    "id" BIGSERIAL PRIMARY KEY,
    "email" VARCHAR(100) NOT NULL UNIQUE,
    "password" VARCHAR(255),                      -- 비밀번호 (nullable, 소셜만 사용 시 NULL)
    "nickname" VARCHAR(50) UNIQUE,                -- 닉네임 (신규 가입 시 이메일로 설정)
    "profile_image" VARCHAR(500),
    "role" VARCHAR(20) DEFAULT 'USER',            -- USER / ADMIN
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

-- 뉴스 기사
CREATE TABLE "news_article" (
    "id" BIGSERIAL PRIMARY KEY,
    "title" VARCHAR(500) NOT NULL,
    "content_summary" TEXT,                       -- 요약 (500자 이내)
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

-- 뉴스-ETF 영향력 매핑
CREATE TABLE "news_etf_influence" (
    "id" BIGSERIAL PRIMARY KEY,
    "news_id" BIGINT NOT NULL,
    "etf_ticker" VARCHAR(20) NOT NULL,            -- ETF 종목 코드
    "influence_score" DECIMAL(5,4),               -- 영향력 점수 (0.0000 ~ 1.0000)
    "influence_type" VARCHAR(20),                 -- POSITIVE / NEGATIVE / NEUTRAL
    "analysis_reason" TEXT,                       -- LLM 분석 사유
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_news_influence_news" FOREIGN KEY ("news_id") REFERENCES "news_article"("id") ON DELETE CASCADE,
    CONSTRAINT "uk_news_etf" UNIQUE ("news_id", "etf_ticker")
);

CREATE INDEX "idx_news_etf_ticker" ON "news_etf_influence"("etf_ticker");
CREATE INDEX "idx_news_etf_influence_score" ON "news_etf_influence"("influence_score" DESC);

-- =============================================
-- 3. AI 피드백 (포트폴리오 LLM 리뷰)
-- =============================================

-- 포트폴리오 AI 피드백
CREATE TABLE "portfolio_ai_feedback" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "portfolio_snapshot_id" BIGINT,               -- 포트폴리오 스냅샷 ID (택민 담당 테이블 참조)
    "bull_review" TEXT,                           -- 강세 관점 리뷰
    "bear_review" TEXT,                           -- 약세 관점 리뷰
    "overall_score" DECIMAL(3,1),                 -- 종합 점수 (0.0 ~ 10.0)
    "risk_level" VARCHAR(20),                     -- LOW / MEDIUM / HIGH
    "recommendation" TEXT,                        -- 추천 사항
    "llm_model" VARCHAR(50),                      -- 사용된 LLM 모델
    "prompt_version" VARCHAR(20),                 -- 프롬프트 버전
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_ai_feedback_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE
);

CREATE INDEX "idx_ai_feedback_user" ON "portfolio_ai_feedback"("user_id");
CREATE INDEX "idx_ai_feedback_created" ON "portfolio_ai_feedback"("created_at" DESC);

-- AI 피드백 사용자 평가
CREATE TABLE "ai_feedback_rating" (
    "id" BIGSERIAL PRIMARY KEY,
    "feedback_id" BIGINT NOT NULL,
    "user_id" BIGINT NOT NULL,
    "rating" VARCHAR(20) NOT NULL,                -- HELPFUL / NOT_HELPFUL
    "comment" VARCHAR(500),
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_rating_feedback" FOREIGN KEY ("feedback_id") REFERENCES "portfolio_ai_feedback"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_rating_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE,
    CONSTRAINT "uk_feedback_user_rating" UNIQUE ("feedback_id", "user_id")
);

-- =============================================
-- 4. 사용자 ETF 관련 (마이페이지)
-- =============================================

-- 관심 ETF (좋아요)
CREATE TABLE "user_favorite_etf" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "etf_ticker" VARCHAR(20) NOT NULL,            -- ETF 종목 코드
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_favorite_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE,
    CONSTRAINT "uk_user_favorite_etf" UNIQUE ("user_id", "etf_ticker")
);

CREATE INDEX "idx_favorite_user" ON "user_favorite_etf"("user_id");

-- 마이데이터 보유 ETF
CREATE TABLE "user_holding_etf" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "etf_ticker" VARCHAR(20) NOT NULL,            -- ETF 종목 코드
    "quantity" INTEGER NOT NULL,                  -- 보유 수량
    "avg_price" DECIMAL(15,2),                    -- 평균 매입가
    "synced_at" TIMESTAMP,                        -- 마이데이터 동기화 시점
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_holding_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE,
    CONSTRAINT "uk_user_holding_etf" UNIQUE ("user_id", "etf_ticker")
);

CREATE INDEX "idx_holding_user" ON "user_holding_etf"("user_id");

-- =============================================
-- 5. 알림
-- =============================================

-- ETF 알림
CREATE TABLE "etf_alert" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "etf_ticker" VARCHAR(20),                     -- ETF 종목 코드 (NULL이면 전체 알림)
    "alert_type" VARCHAR(30) NOT NULL,            -- LISTING / DELISTING / PRICE_CHANGE
    "title" VARCHAR(200) NOT NULL,
    "message" TEXT,
    "is_read" BOOLEAN DEFAULT FALSE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_alert_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE
);

CREATE INDEX "idx_alert_user" ON "etf_alert"("user_id");
CREATE INDEX "idx_alert_created" ON "etf_alert"("created_at" DESC);
CREATE INDEX "idx_alert_unread" ON "etf_alert"("user_id", "is_read") WHERE "is_read" = FALSE;

-- 알림 설정
CREATE TABLE "notification_setting" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL UNIQUE,
    "etf_listing_alert" BOOLEAN DEFAULT TRUE,     -- ETF 상장 알림
    "etf_delisting_alert" BOOLEAN DEFAULT TRUE,   -- ETF 상장폐지 알림
    "portfolio_alert" BOOLEAN DEFAULT TRUE,       -- 포트폴리오 알림
    "news_alert" BOOLEAN DEFAULT TRUE,            -- 뉴스 알림
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

-- =============================================
-- 6. ETF 태그 (클러스터링 관련)
-- =============================================

-- ETF 태그 정의
CREATE TABLE "etf_tag" (
    "id" BIGSERIAL PRIMARY KEY,
    "name" VARCHAR(50) NOT NULL UNIQUE,           -- 태그명 (공격형, 안전형, 배당 등)
    "category" VARCHAR(30),                       -- RISK / SECTOR / THEME
    "color" VARCHAR(7),                           -- HEX 색상 코드
    "description" VARCHAR(200),
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ETF-태그 매핑
CREATE TABLE "etf_tag_mapping" (
    "id" BIGSERIAL PRIMARY KEY,
    "etf_ticker" VARCHAR(20) NOT NULL,
    "tag_id" BIGINT NOT NULL,
    "confidence" DECIMAL(3,2) DEFAULT 1.00,       -- 태그 신뢰도 (0.00 ~ 1.00)
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_tag_mapping_tag" FOREIGN KEY ("tag_id") REFERENCES "etf_tag"("id") ON DELETE CASCADE,
    CONSTRAINT "uk_etf_tag" UNIQUE ("etf_ticker", "tag_id")
);

CREATE INDEX "idx_etf_tag_ticker" ON "etf_tag_mapping"("etf_ticker");
CREATE INDEX "idx_etf_tag_tag" ON "etf_tag_mapping"("tag_id");

-- =============================================
-- 7. ETF 클러스터 (맵 렌더링용)
-- =============================================

-- ETF 클러스터 정보
CREATE TABLE "etf_cluster" (
    "id" BIGSERIAL PRIMARY KEY,
    "cluster_name" VARCHAR(100),                  -- 클러스터 이름 (AI 생성)
    "cluster_label" INTEGER NOT NULL,             -- HDBSCAN 클러스터 라벨
    "center_x" DECIMAL(10,6),                     -- 2D 좌표 X (UMAP)
    "center_y" DECIMAL(10,6),                     -- 2D 좌표 Y (UMAP)
    "etf_count" INTEGER DEFAULT 0,
    "avg_return_1m" DECIMAL(8,4),                 -- 1개월 평균 수익률
    "avg_volatility" DECIMAL(8,4),                -- 평균 변동성
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ETF 클러스터 매핑
CREATE TABLE "etf_cluster_mapping" (
    "id" BIGSERIAL PRIMARY KEY,
    "etf_ticker" VARCHAR(20) NOT NULL UNIQUE,
    "cluster_id" BIGINT NOT NULL,
    "pos_x" DECIMAL(10,6),                        -- UMAP 좌표 X
    "pos_y" DECIMAL(10,6),                        -- UMAP 좌표 Y
    "distance_to_center" DECIMAL(10,6),           -- 클러스터 중심과의 거리
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_cluster_mapping" FOREIGN KEY ("cluster_id") REFERENCES "etf_cluster"("id") ON DELETE CASCADE
);

CREATE INDEX "idx_cluster_mapping_cluster" ON "etf_cluster_mapping"("cluster_id");

-- =============================================
-- 8. 상장 회사 정보 (산업분류 포함)
-- =============================================

-- 표준산업소분류 코드 테이블 (참조용, 약 200개)
CREATE TABLE "industry_classification" (
    "code" VARCHAR(10) PRIMARY KEY,              -- 표준산업소분류코드 (6자리, 예: 032601)
    "name" VARCHAR(100) NOT NULL,                -- 소분류명 (예: 반도체 제조업)
    "group_code" VARCHAR(10),                    -- 대분류 그룹 코드 (자체 정의, 예: IT_SEMI)
    "group_name" VARCHAR(50),                    -- 대분류 그룹명 (예: 반도체)
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE "industry_classification" IS '표준산업소분류코드 (클러스터링/필터링용)';

-- 대분류 그룹 코드 (20개)
-- IT_SEMI: 반도체
-- IT_ELEC: 전자/IT
-- IT_SW: 소프트웨어
-- BIO: 바이오/의약
-- AUTO: 자동차
-- CHEM: 화학/소재
-- STEEL: 철강/금속
-- ENERGY: 에너지/유틸리티
-- FINANCE: 금융
-- INSURANCE: 보험
-- CONSTRUCT: 건설
-- RETAIL: 유통/소매
-- FOOD: 식품/음료
-- TELECOM: 통신/미디어
-- CONSUMER: 소비재
-- TRANSPORT: 운송
-- MACHINERY: 기계
-- SHIPBUILD: 조선
-- DEFENSE: 방산
-- ETC: 기타

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

-- =============================================
-- 9. ETF 공시 정보 (상장폐지 알림용)
-- =============================================

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
