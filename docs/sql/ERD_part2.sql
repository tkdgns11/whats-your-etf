-- =============================================
-- What's Your ETF ERD (DDL) - Part 2
-- ERDCloud import용 (간소화 버전)
-- ETF, 회사정보, 포트폴리오, 시뮬레이션 (10개 테이블)
-- =============================================

-- =============================================
-- 8. 상장 회사 정보
-- =============================================

CREATE TABLE "industry_classification" (
    "code" VARCHAR(10) PRIMARY KEY,
    "name" VARCHAR(100) NOT NULL,
    "group_code" VARCHAR(10),
    "group_name" VARCHAR(50),
    "created_at" TIMESTAMP
);

CREATE TABLE "company_info" (
    "id" BIGINT PRIMARY KEY,
    "stock_code" VARCHAR(20) NOT NULL UNIQUE,
    "stock_name" VARCHAR(100) NOT NULL,
    "market_type" VARCHAR(20),
    "industry_code" VARCHAR(10),
    "industry_name" VARCHAR(100),
    "industry_group" VARCHAR(50),
    "listing_date" DATE,
    "fiscal_month" INTEGER,
    "ceo_name" VARCHAR(100),
    "homepage" VARCHAR(200),
    "region" VARCHAR(50),
    "face_value" INTEGER,
    "listed_shares" BIGINT,
    "is_active" BOOLEAN,
    "data_source" VARCHAR(50),
    "created_at" TIMESTAMP,
    "updated_at" TIMESTAMP,
    CONSTRAINT "fk_company_industry" FOREIGN KEY ("industry_code") REFERENCES "industry_classification"("code")
);

-- =============================================
-- 9. ETF 공시 정보
-- =============================================

CREATE TABLE "etf_disclosure" (
    "id" BIGINT PRIMARY KEY,
    "etf_code" VARCHAR(20) NOT NULL,
    "etf_name" VARCHAR(200) NOT NULL,
    "disclosure_type" VARCHAR(50) NOT NULL,
    "disclosure_title" TEXT NOT NULL,
    "disclosure_content" TEXT,
    "disclosure_date" DATE NOT NULL,
    "effective_date" DATE,
    "source_url" TEXT,
    "is_notified" VARCHAR(1),
    "created_at" TIMESTAMP
);

-- =============================================
-- 10. ETF 목록
-- =============================================

CREATE TABLE "etf" (
    "id" BIGINT PRIMARY KEY,
    "ticker" VARCHAR(20) UNIQUE NOT NULL,
    "name" VARCHAR(200) NOT NULL,
    "category" VARCHAR(50),
    "strategy_type" VARCHAR(30),
    "sector" VARCHAR(50),
    "asset_class" VARCHAR(30),
    "asset_manager" VARCHAR(50),
    "is_leveraged" BOOLEAN,
    "is_inverse" BOOLEAN,
    "is_hedged" BOOLEAN,
    "expense_ratio" DECIMAL(6,4),
    "nav" DECIMAL(14,2),
    "aum" BIGINT,
    "dividend_yield" DECIMAL(6,3),
    "dividend_freq" VARCHAR(10),
    "avg_per" DECIMAL(8,2),
    "avg_pbr" DECIMAL(8,2),
    "avg_roe" DECIMAL(8,2),
    "risk_grade" VARCHAR(20),
    "volatility_1y" DECIMAL(8,4),
    "listing_date" DATE,
    "delisted_date" DATE,
    "is_active" BOOLEAN,
    "updated_at" TIMESTAMP
);

-- =============================================
-- 11. ETF 구성종목
-- =============================================

CREATE TABLE "etf_compositions" (
    "id" BIGINT PRIMARY KEY,
    "etf_id" BIGINT NOT NULL,
    "component_ticker" VARCHAR(20),
    "weight_pct" DECIMAL(6,3),
    "base_date" DATE NOT NULL,
    CONSTRAINT "fk_composition_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id"),
    CONSTRAINT "fk_composition_component" FOREIGN KEY ("component_ticker") REFERENCES "company_info"("stock_code")
);

-- =============================================
-- 12. ETF 일별 시세
-- =============================================

CREATE TABLE "etf_prices" (
    "id" BIGINT PRIMARY KEY,
    "etf_id" BIGINT NOT NULL,
    "trade_date" DATE NOT NULL,
    "close" DECIMAL(14,2),
    "nav" DECIMAL(14,2),
    "volume" BIGINT,
    "change_rate" DECIMAL(8,4),
    CONSTRAINT "fk_etf_prices_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id")
);

-- =============================================
-- 13. 사용자 포트폴리오
-- =============================================

CREATE TABLE "portfolios" (
    "id" BIGINT PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "name" VARCHAR(100) NOT NULL,
    "description" TEXT,
    "invest_amount" DECIMAL(15,2),
    "risk_level" VARCHAR(20),
    "snapshot_etfs" TEXT,
    "snapshot_metrics" TEXT,
    "is_tracking" BOOLEAN,
    "current_return" DECIMAL(8,4),
    "created_at" TIMESTAMP,
    "updated_at" TIMESTAMP
);

CREATE TABLE "portfolio_etfs" (
    "id" BIGINT PRIMARY KEY,
    "portfolio_id" BIGINT NOT NULL,
    "etf_id" BIGINT NOT NULL,
    "weight_pct" DECIMAL(6,3) NOT NULL,
    CONSTRAINT "fk_portfolio_etf_portfolio" FOREIGN KEY ("portfolio_id") REFERENCES "portfolios"("id"),
    CONSTRAINT "fk_portfolio_etf_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id")
);

-- =============================================
-- 14. 시뮬레이션
-- =============================================

CREATE TABLE "simulations" (
    "id" BIGINT PRIMARY KEY,
    "portfolio_id" BIGINT NOT NULL,
    "user_id" BIGINT NOT NULL,
    "start_date" DATE NOT NULL,
    "end_date" DATE NOT NULL,
    "initial_amount" DECIMAL(15,2) NOT NULL,
    "rebalance_period" VARCHAR(20),
    "final_amount" DECIMAL(15,2),
    "total_return" DECIMAL(8,4),
    "annualized_return" DECIMAL(8,4),
    "max_drawdown" DECIMAL(8,4),
    "sharpe_ratio" DECIMAL(8,4),
    "volatility" DECIMAL(8,4),
    "benchmark_ticker" VARCHAR(20),
    "benchmark_return" DECIMAL(8,4),
    "alpha" DECIMAL(8,4),
    "status" VARCHAR(20),
    "created_at" TIMESTAMP,
    "completed_at" TIMESTAMP,
    CONSTRAINT "fk_simulation_portfolio" FOREIGN KEY ("portfolio_id") REFERENCES "portfolios"("id")
);

CREATE TABLE "simulation_daily" (
    "id" BIGINT PRIMARY KEY,
    "simulation_id" BIGINT NOT NULL,
    "trade_date" DATE NOT NULL,
    "portfolio_value" DECIMAL(15,2),
    "benchmark_value" DECIMAL(15,2),
    "daily_return" DECIMAL(8,6),
    "cumulative_return" DECIMAL(8,4),
    "drawdown" DECIMAL(8,4),
    CONSTRAINT "fk_simulation_daily" FOREIGN KEY ("simulation_id") REFERENCES "simulations"("id")
);
