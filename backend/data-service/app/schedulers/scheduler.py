"""네이버 증권 종목뉴스 크롤링, KRX 공시 스케줄러"""
import logging
from apscheduler.schedulers.asyncio import AsyncIOScheduler
from apscheduler.triggers.interval import IntervalTrigger
from apscheduler.triggers.cron import CronTrigger
from sqlalchemy import func

from app.database import SessionLocal
from app.scrapers.stock_news_scraper import StockNewsScraper
from app.scrapers.krx_scraper import KrxDisclosureScraper
from app.models.etf import ETF, ETFComposition
from app.models.company import CompanyInfo
from app.config import get_settings

logger = logging.getLogger(__name__)
settings = get_settings()

scheduler = AsyncIOScheduler()


async def scrape_stock_news_job():
    """ETF 구성종목 뉴스 크롤링 (30분마다)

    - 활성 ETF 상위 100개의 구성종목에서 뉴스 수집
    - 사용자 관심 ETF/포트폴리오 ETF 구성종목도 포함
    - 네이버 증권 종목뉴스 페이지에서 직접 크롤링
    - 100% 본문 추출 가능
    """
    logger.info("=== 종목뉴스 크롤링 시작 ===")

    db = SessionLocal()

    try:
        from sqlalchemy import text

        # 크롤링 대상 종목 조회 (중복 제거)
        # 1. 상위 100개 ETF의 구성종목
        # 2. 사용자 관심 ETF 구성종목
        # 3. 사용자 포트폴리오 ETF 구성종목
        query = text("""
            SELECT DISTINCT c.id, c.stock_code
            FROM company_info c
            JOIN etf_stock_composition esc ON esc.company_id = c.id
            WHERE c.is_active = true
              AND c.stock_code IS NOT NULL
              AND (
                -- 상위 100개 ETF 구성종목
                esc.etf_id IN (
                    SELECT id FROM etf
                    WHERE is_active = true
                    ORDER BY aum DESC NULLS LAST
                    LIMIT 100
                )
                -- 사용자 관심 ETF 구성종목
                OR esc.etf_id IN (SELECT etf_id FROM user_favorite_etf)
                -- 포트폴리오 ETF 구성종목
                OR esc.etf_id IN (SELECT etf_id FROM portfolio_items)
              )
            ORDER BY c.id
        """)

        result = db.execute(query)
        companies = result.fetchall()

        if not companies:
            logger.warning("크롤링 대상 종목 없음")
            return

        logger.info(f"크롤링 대상 종목: {len(companies)}개")
        total_stats = {"total": 0, "new": 0, "mapped": 0}

        async with StockNewsScraper(db) as scraper:
            for company in companies:
                try:
                    stats = await scraper.scrape_stock_news(
                        stock_code=company.stock_code,
                        max_articles=5
                    )
                    total_stats["total"] += stats["total"]
                    total_stats["new"] += stats["new"]
                    total_stats["mapped"] += stats["mapped"]
                except Exception as e:
                    logger.error(f"종목 크롤링 실패 [{company.stock_code}]: {e}")
                    continue

        logger.info(
            f"=== 종목뉴스 크롤링 완료 ===\n"
            f"  처리: {total_stats['total']}건\n"
            f"  신규: {total_stats['new']}건\n"
            f"  매핑추가: {total_stats['mapped']}건"
        )

    except Exception as e:
        logger.error(f"종목뉴스 크롤링 실패: {e}")
    finally:
        db.close()


async def krx_disclosure_job():
    """KRX KIND 공시 체크 (매일 09:00)"""
    logger.info("=== KRX KIND 공시 체크 시작 ===")

    db = SessionLocal()
    scraper = KrxDisclosureScraper(db)

    try:
        result = await scraper.scrape_disclosures(days_back=7)
        logger.info(
            f"=== KRX KIND 공시 체크 완료 ===\n"
            f"  총 수집: {result['total']}건\n"
            f"  신규: {result['new']}건"
        )

        pending = await scraper.get_pending_notifications()
        if pending:
            logger.info(f"알림 대기 중인 공시: {len(pending)}건")

    except Exception as e:
        logger.error(f"KRX KIND 공시 체크 실패: {e}")
    finally:
        await scraper.close()
        db.close()


def start_scheduler():
    """스케줄러 시작"""
    # ETF 구성종목 뉴스 크롤링 (30분마다)
    # - 상위 100개 ETF + 사용자 관심 ETF + 포트폴리오 ETF 구성종목
    scheduler.add_job(
        scrape_stock_news_job,
        trigger=IntervalTrigger(minutes=30),
        id="stock_news_scraping",
        name="ETF Stock News Scraping",
        replace_existing=True
    )

    # KRX KIND 공시 체크 (매일 09:00)
    scheduler.add_job(
        krx_disclosure_job,
        trigger=CronTrigger(hour=9, minute=0),
        id="krx_disclosure_check",
        name="KRX KIND Disclosure Check",
        replace_existing=True
    )

    scheduler.start()
    logger.info(
        f"스케줄러 시작:\n"
        f"  - ETF 구성종목 뉴스: 30분 간격\n"
        f"  - KRX 공시 체크: 매일 09:00"
    )
