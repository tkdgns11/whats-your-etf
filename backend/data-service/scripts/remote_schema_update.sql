-- 원격 DB 스키마 업데이트 SQL
-- 실행 전 SSH 터널 필요: ssh -i J14D102T.pem -L 5433:localhost:5432 ubuntu@j14d102.p.ssafy.io

-- =============================================
-- 1. 테이블 생성
-- =============================================

-- alert_message_template
CREATE TABLE IF NOT EXISTS "alert_message_template" (
    "id" BIGSERIAL PRIMARY KEY,
    "alert_type_code" VARCHAR(30) NOT NULL,
    "version" VARCHAR(20) NOT NULL,
    "title_template" VARCHAR(200) NOT NULL,
    "message_template" TEXT NOT NULL,
    "variables" JSONB,
    "description" VARCHAR(200),
    "is_active" BOOLEAN DEFAULT FALSE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_alert_template_type" FOREIGN KEY ("alert_type_code") REFERENCES "alert_type"("code") ON DELETE CASCADE,
    CONSTRAINT "uk_alert_template_version" UNIQUE ("alert_type_code", "version")
);

-- etf_disclosure
CREATE TABLE IF NOT EXISTS "etf_disclosure" (
    "id" BIGSERIAL PRIMARY KEY,
    "etf_id" BIGINT,
    "etf_code" VARCHAR(20) NOT NULL,
    "etf_name" VARCHAR(200) NOT NULL,
    "disclosure_type" VARCHAR(50) NOT NULL,
    "disclosure_title" TEXT NOT NULL,
    "disclosure_content" TEXT,
    "disclosure_date" DATE NOT NULL,
    "effective_date" DATE,
    "source_url" TEXT,
    "is_notified" BOOLEAN DEFAULT FALSE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_disclosure_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE SET NULL
);

-- etf_sector_ai_history (AI 분석 결과 저장용)
CREATE TABLE IF NOT EXISTS "etf_sector_ai_history" (
    "id" BIGSERIAL PRIMARY KEY,
    "etf_id" BIGINT NOT NULL,
    "group_code" VARCHAR(20) NOT NULL,
    "group_name" VARCHAR(50),
    "weight_pct" DECIMAL(6,3),
    "stock_count" INTEGER,
    "top_stocks" JSONB,
    "ai_analysis" TEXT NOT NULL,
    "prompt_id" BIGINT,
    "base_date" DATE NOT NULL,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_sector_ai_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_sector_ai_prompt" FOREIGN KEY ("prompt_id") REFERENCES "ai_prompt"("id")
);

-- =============================================
-- 2. 컬럼 추가
-- =============================================

-- etf 테이블에 밸류에이션 컬럼 추가
ALTER TABLE "etf" ADD COLUMN IF NOT EXISTS "avg_per" DECIMAL(8,2);
ALTER TABLE "etf" ADD COLUMN IF NOT EXISTS "avg_pbr" DECIMAL(8,2);
ALTER TABLE "etf" ADD COLUMN IF NOT EXISTS "avg_roe" DECIMAL(8,2);

-- login_history 테이블에 created_at 추가
ALTER TABLE "login_history" ADD COLUMN IF NOT EXISTS "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- =============================================
-- 3. 인덱스 생성 (성능 최적화)
-- =============================================

-- etf_sector_ai_history 인덱스
CREATE INDEX IF NOT EXISTS "idx_sector_ai_etf_date" ON "etf_sector_ai_history"("etf_id", "base_date");
CREATE INDEX IF NOT EXISTS "idx_sector_ai_group" ON "etf_sector_ai_history"("group_code");

-- =============================================
-- 확인용 쿼리
-- =============================================
-- SELECT table_name FROM information_schema.tables WHERE table_name IN ('alert_message_template', 'etf_disclosure', 'etf_sector_ai_history');
-- SELECT column_name FROM information_schema.columns WHERE table_name = 'etf' AND column_name IN ('avg_per', 'avg_pbr', 'avg_roe');
