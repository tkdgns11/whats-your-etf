-- =============================================
-- What's Your ETF ERD (DDL) - Part 1
-- ERDCloud import용 (간소화 버전)
-- 사용자/인증, 뉴스, AI, 알림, 태그/클러스터 (19개 테이블)
-- =============================================

-- =============================================
-- 1. 사용자/인증
-- =============================================

CREATE TABLE "user" (
    "id" BIGINT PRIMARY KEY,
    "email" VARCHAR(100) NOT NULL UNIQUE,
    "password" VARCHAR(255),
    "nickname" VARCHAR(50) UNIQUE,
    "role" VARCHAR(20),
    "is_active" BOOLEAN,
    "last_login_at" TIMESTAMP,
    "created_at" TIMESTAMP,
    "updated_at" TIMESTAMP
);

CREATE TABLE "user_social_account" (
    "id" BIGINT PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "provider" VARCHAR(20) NOT NULL,
    "provider_user_id" VARCHAR(100) NOT NULL,
    "email" VARCHAR(100),
    "is_primary" BOOLEAN,
    "linked_at" TIMESTAMP,
    "created_at" TIMESTAMP,
    CONSTRAINT "fk_social_user" FOREIGN KEY ("user_id") REFERENCES "user"("id")
);

CREATE TABLE "refresh_token" (
    "id" BIGINT PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "token" VARCHAR(500) NOT NULL UNIQUE,
    "expires_at" TIMESTAMP NOT NULL,
    "is_revoked" BOOLEAN,
    "created_at" TIMESTAMP,
    CONSTRAINT "fk_refresh_token_user" FOREIGN KEY ("user_id") REFERENCES "user"("id")
);

CREATE TABLE "password_reset_token" (
    "id" BIGINT PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "token" VARCHAR(255) NOT NULL UNIQUE,
    "expires_at" TIMESTAMP NOT NULL,
    "is_used" BOOLEAN,
    "created_at" TIMESTAMP,
    CONSTRAINT "fk_reset_token_user" FOREIGN KEY ("user_id") REFERENCES "user"("id")
);

CREATE TABLE "email_verification_token" (
    "id" BIGINT PRIMARY KEY,
    "email" VARCHAR(100) NOT NULL,
    "token" VARCHAR(255) NOT NULL UNIQUE,
    "expires_at" TIMESTAMP NOT NULL,
    "is_verified" BOOLEAN,
    "created_at" TIMESTAMP
);

CREATE TABLE "login_history" (
    "id" BIGINT PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "provider" VARCHAR(20) NOT NULL,
    "login_at" TIMESTAMP,
    "ip_address" VARCHAR(45),
    "device_info" VARCHAR(200),
    CONSTRAINT "fk_login_history_user" FOREIGN KEY ("user_id") REFERENCES "user"("id")
);

-- =============================================
-- 2. 뉴스
-- =============================================

CREATE TABLE "news_article" (
    "id" BIGINT PRIMARY KEY,
    "title" VARCHAR(500) NOT NULL,
    "content_summary" TEXT,
    "source" VARCHAR(100),
    "source_url" VARCHAR(1000) NOT NULL UNIQUE,
    "thumbnail_url" VARCHAR(1000),
    "category" VARCHAR(50),
    "keywords" TEXT,
    "published_at" TIMESTAMP,
    "view_count" INTEGER,
    "is_active" BOOLEAN,
    "created_at" TIMESTAMP
);

CREATE TABLE "news_etf_influence" (
    "id" BIGINT PRIMARY KEY,
    "news_id" BIGINT NOT NULL,
    "etf_id" BIGINT NOT NULL,
    "influence_score" DECIMAL(5,4),
    "influence_type" VARCHAR(20),
    "analysis_reason" TEXT,
    "created_at" TIMESTAMP,
    CONSTRAINT "fk_news_influence_news" FOREIGN KEY ("news_id") REFERENCES "news_article"("id"),
    CONSTRAINT "fk_news_influence_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id")
);

-- =============================================
-- 3. AI 피드백
-- =============================================

CREATE TABLE "portfolio_ai_feedback" (
    "id" BIGINT PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "portfolio_snapshot_id" BIGINT,
    "bull_review" TEXT,
    "bear_review" TEXT,
    "overall_score" DECIMAL(3,1),
    "risk_level" VARCHAR(20),
    "recommendation" TEXT,
    "llm_model" VARCHAR(50),
    "prompt_version" VARCHAR(20),
    "created_at" TIMESTAMP,
    CONSTRAINT "fk_ai_feedback_user" FOREIGN KEY ("user_id") REFERENCES "user"("id")
);

-- =============================================
-- 4. 사용자 ETF 관련
-- =============================================

CREATE TABLE "user_favorite_etf" (
    "id" BIGINT PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "etf_id" BIGINT NOT NULL,
    "created_at" TIMESTAMP,
    CONSTRAINT "fk_favorite_user" FOREIGN KEY ("user_id") REFERENCES "user"("id"),
    CONSTRAINT "fk_favorite_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id")
);

CREATE TABLE "user_holding_etf" (
    "id" BIGINT PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "etf_id" BIGINT NOT NULL,
    "quantity" INTEGER NOT NULL,
    "avg_price" DECIMAL(15,2),
    "synced_at" TIMESTAMP,
    "created_at" TIMESTAMP,
    "updated_at" TIMESTAMP,
    CONSTRAINT "fk_holding_user" FOREIGN KEY ("user_id") REFERENCES "user"("id"),
    CONSTRAINT "fk_holding_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id")
);

-- =============================================
-- 5. 알림
-- =============================================

CREATE TABLE "etf_alert" (
    "id" BIGINT PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "etf_id" BIGINT,
    "alert_type" VARCHAR(30) NOT NULL,
    "title" VARCHAR(200) NOT NULL,
    "message" TEXT,
    "is_read" BOOLEAN,
    "created_at" TIMESTAMP,
    CONSTRAINT "fk_alert_user" FOREIGN KEY ("user_id") REFERENCES "user"("id"),
    CONSTRAINT "fk_alert_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id")
);

CREATE TABLE "notification_setting" (
    "id" BIGINT PRIMARY KEY,
    "user_id" BIGINT NOT NULL UNIQUE,
    "etf_listing_alert" BOOLEAN,
    "etf_delisting_alert" BOOLEAN,
    "portfolio_alert" BOOLEAN,
    "news_alert" BOOLEAN,
    "created_at" TIMESTAMP,
    "updated_at" TIMESTAMP,
    CONSTRAINT "fk_notification_user" FOREIGN KEY ("user_id") REFERENCES "user"("id")
);

CREATE TABLE "fcm_token" (
    "id" BIGINT PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "token" VARCHAR(500) NOT NULL,
    "device_type" VARCHAR(20),
    "is_active" BOOLEAN,
    "created_at" TIMESTAMP,
    "updated_at" TIMESTAMP,
    CONSTRAINT "fk_fcm_user" FOREIGN KEY ("user_id") REFERENCES "user"("id")
);

-- =============================================
-- 6. ETF 태그
-- =============================================

CREATE TABLE "etf_tag" (
    "id" BIGINT PRIMARY KEY,
    "name" VARCHAR(50) NOT NULL UNIQUE,
    "category" VARCHAR(30),
    "color" VARCHAR(7),
    "description" VARCHAR(200),
    "created_at" TIMESTAMP
);

CREATE TABLE "etf_tag_mapping" (
    "id" BIGINT PRIMARY KEY,
    "etf_id" BIGINT NOT NULL,
    "tag_id" BIGINT NOT NULL,
    "confidence" DECIMAL(3,2),
    "created_at" TIMESTAMP,
    CONSTRAINT "fk_tag_mapping_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id"),
    CONSTRAINT "fk_tag_mapping_tag" FOREIGN KEY ("tag_id") REFERENCES "etf_tag"("id")
);

-- =============================================
-- 7. ETF 클러스터
-- =============================================

CREATE TABLE "etf_cluster" (
    "id" BIGINT PRIMARY KEY,
    "cluster_name" VARCHAR(100),
    "cluster_label" INTEGER NOT NULL,
    "center_x" DECIMAL(10,6),
    "center_y" DECIMAL(10,6),
    "etf_count" INTEGER,
    "avg_return_1m" DECIMAL(8,4),
    "avg_volatility" DECIMAL(8,4),
    "created_at" TIMESTAMP,
    "updated_at" TIMESTAMP
);

CREATE TABLE "etf_cluster_mapping" (
    "id" BIGINT PRIMARY KEY,
    "etf_id" BIGINT NOT NULL UNIQUE,
    "cluster_id" BIGINT NOT NULL,
    "pos_x" DECIMAL(10,6),
    "pos_y" DECIMAL(10,6),
    "distance_to_center" DECIMAL(10,6),
    "created_at" TIMESTAMP,
    "updated_at" TIMESTAMP,
    CONSTRAINT "fk_cluster_mapping_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id"),
    CONSTRAINT "fk_cluster_mapping" FOREIGN KEY ("cluster_id") REFERENCES "etf_cluster"("id")
);
