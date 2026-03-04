-- =============================================
-- 표준산업분류코드 테이블 생성 (PostgreSQL)
-- industry_classification_seed.sql 실행 전에 이 파일 먼저 실행
-- =============================================

CREATE TABLE "industry_classification" (
    "code" VARCHAR(10) PRIMARY KEY,
    "name" VARCHAR(100) NOT NULL,
    "level" INTEGER NOT NULL,
    "parent_code" VARCHAR(10),
    "group_code" VARCHAR(10),
    "group_name" VARCHAR(50),
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "fk_industry_parent" FOREIGN KEY ("parent_code")
        REFERENCES "industry_classification"("code") ON DELETE SET NULL
);

CREATE INDEX "idx_industry_parent" ON "industry_classification"("parent_code");
CREATE INDEX "idx_industry_level" ON "industry_classification"("level");
CREATE INDEX "idx_industry_group" ON "industry_classification"("group_code");
