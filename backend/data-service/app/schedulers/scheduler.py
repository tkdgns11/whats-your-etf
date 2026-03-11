"""네이버 증권 종목뉴스 크롤링, AI 분석, KRX 공시 스케줄러"""
import logging
from apscheduler.schedulers.asyncio import AsyncIOScheduler
from apscheduler.triggers.interval import IntervalTrigger
from apscheduler.triggers.cron import CronTrigger
from sqlalchemy import func

from app.database import SessionLocal
from app.scrapers.stock_news_scraper import StockNewsScraper
from app.scrapers.krx_scraper import KrxDisclosureScraper
from app.services.news_analyzer import analyze_unprocessed_news
from app.models.etf import ETF, ETFComposition
from app.models.company import CompanyInfo
from app.config import get_settings

logger = logging.getLogger(__name__)
settings = get_settings()

scheduler = AsyncIOScheduler()


async def scrape_stock_news_job():
    """ETF 구성종목 뉴스 크롤링 + AI 분석 (30분마다)

    1. 뉴스 크롤링: 상위 100개 ETF + 사용자 관심 ETF 구성종목
    2. AI 분석: 미분석 뉴스 자동 처리 (요약, 키워드, ETF 추천)
    """
    logger.info("=== 종목뉴스 크롤링 시작 ===")

    db = SessionLocal()

    try:
        from sqlalchemy import text

        # 크롤링 대상 종목 조회 (중복 제거)
        # stock.ticker를 사용 (company_info에는 stock_code 없음)
        query = text("""
            SELECT DISTINCT s.id, s.ticker
            FROM stock s
            JOIN etf_stock_composition esc ON esc.stock_id = s.id
            WHERE s.ticker IS NOT NULL
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
                OR esc.etf_id IN (SELECT etf_id FROM portfolio_etf)
              )
            ORDER BY s.id
        """)

        result = db.execute(query)
        companies = result.fetchall()

        if not companies:
            logger.warning("크롤링 대상 종목 없음")
            return

        logger.info(f"크롤링 대상 종목: {len(companies)}개")
        total_stats = {"total": 0, "new": 0, "mapped": 0}

        async with StockNewsScraper(db) as scraper:
            for stock in companies:
                try:
                    stats = await scraper.scrape_stock_news(
                        stock_code=stock.ticker,
                        max_articles=5
                    )
                    total_stats["total"] += stats["total"]
                    total_stats["new"] += stats["new"]
                    total_stats["mapped"] += stats["mapped"]
                except Exception as e:
                    logger.error(f"종목 크롤링 실패 [{stock.ticker}]: {e}")
                    continue

        logger.info(
            f"=== 종목뉴스 크롤링 완료 ===\n"
            f"  처리: {total_stats['total']}건\n"
            f"  신규: {total_stats['new']}건\n"
            f"  매핑추가: {total_stats['mapped']}건"
        )

        # AI 분석 자동 실행 (신규 뉴스가 있을 때만)
        if total_stats["new"] > 0:
            logger.info("=== AI 뉴스 분석 시작 ===")
            analyzed = await analyze_unprocessed_news(db, limit=total_stats["new"] + 10)
            logger.info(f"=== AI 분석 완료: {analyzed}건 처리 ===")

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

    # KRX KIND 공시 체크 - 비활성화 (크롤러 문제 해결 후 활성화)
    # scheduler.add_job(
    #     krx_disclosure_job,
    #     trigger=CronTrigger(hour=9, minute=0, timezone='Asia/Seoul'),
    #     id="krx_disclosure_check",
    #     name="KRX KIND Disclosure Check",
    #     replace_existing=True
    # )

    scheduler.start()
    logger.info(
        f"스케줄러 시작:\n"
        f"  - ETF 구성종목 뉴스: 30분 간격"
    )
