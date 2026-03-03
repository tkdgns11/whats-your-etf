"""뉴스 크롤링 및 KRX 공시 스케줄러"""
import asyncio
import logging
from apscheduler.schedulers.asyncio import AsyncIOScheduler
from apscheduler.triggers.interval import IntervalTrigger
from apscheduler.triggers.cron import CronTrigger
from app.database import SessionLocal
from app.scrapers.news_service import NewsCollectionService
from app.scrapers.krx_scraper import KrxDisclosureScraper
from app.config import get_settings

logger = logging.getLogger(__name__)
settings = get_settings()

scheduler = AsyncIOScheduler()


async def scrape_news_job():
    """뉴스 크롤링 스케줄 작업 (10분마다)"""
    logger.info("=== 정기 뉴스 크롤링 시작 ===")

    db = SessionLocal()
    service = NewsCollectionService(db)

    try:
        result = await service.collect_all(enrich_content=True)
        logger.info(
            f"=== 정기 뉴스 크롤링 완료 ===\n"
            f"  Google: {result['google_count']}건\n"
            f"  Naver: {result['naver_count']}건\n"
            f"  본문보강: {result['content_enriched']}건\n"
            f"  총: {result['total']}건"
        )
    except Exception as e:
        logger.error(f"뉴스 크롤링 실패: {e}")
    finally:
        await service.close()
        db.close()


async def full_scrape_job():
    """전체 키워드 뉴스 크롤링 (1시간마다)"""
    logger.info("=== 전체 뉴스 크롤링 시작 ===")

    db = SessionLocal()
    service = NewsCollectionService(db)

    try:
        result = await service.collect_full()
        logger.info(
            f"=== 전체 뉴스 크롤링 완료 ===\n"
            f"  Google: {result['google_count']}건\n"
            f"  Naver: {result['naver_count']}건\n"
            f"  본문보강: {result['content_enriched']}건\n"
            f"  총: {result['total']}건"
        )
    except Exception as e:
        logger.error(f"전체 뉴스 크롤링 실패: {e}")
    finally:
        await service.close()
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

        # 알림 대기 중인 공시 확인
        pending = await scraper.get_pending_notifications()
        if pending:
            logger.info(f"알림 대기 중인 공시: {len(pending)}건")
            # TODO: 사용자 포트폴리오 매칭 후 알림 발송

    except Exception as e:
        logger.error(f"KRX KIND 공시 체크 실패: {e}")
    finally:
        await scraper.close()
        db.close()


def start_scheduler():
    """스케줄러 시작"""
    # 정기 크롤링 (10분마다) - 우선순위 키워드만
    scheduler.add_job(
        scrape_news_job,
        trigger=IntervalTrigger(minutes=settings.news_scrape_interval_minutes),
        id="news_scraping",
        name="Regular News Scraping (Priority Keywords)",
        replace_existing=True
    )

    # 전체 크롤링 (1시간마다) - 모든 키워드
    scheduler.add_job(
        full_scrape_job,
        trigger=IntervalTrigger(hours=1),
        id="full_news_scraping",
        name="Full News Scraping (All Keywords)",
        replace_existing=True
    )

    # KRX KIND 공시 체크 (매일 09:00)
    scheduler.add_job(
        krx_disclosure_job,
        trigger=CronTrigger(hour=9, minute=0),
        id="krx_disclosure_check",
        name="KRX KIND Disclosure Check (Daily)",
        replace_existing=True
    )

    scheduler.start()
    logger.info(
        f"스케줄러 시작:\n"
        f"  - 정기 크롤링: {settings.news_scrape_interval_minutes}분 간격\n"
        f"  - 전체 크롤링: 1시간 간격\n"
        f"  - KRX 공시 체크: 매일 09:00"
    )
