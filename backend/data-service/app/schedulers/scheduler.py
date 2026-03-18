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
    """ETF 구성종목 뉴스 크롤링 (매일 03:00 KST)

    뉴스 크롤링만 수행. AI 분석은 별도 스케줄러에서 실행.
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
                    db.rollback()  # 트랜잭션 롤백하여 다음 종목 처리 가능하게
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


async def news_ai_analysis_job():
    """뉴스 AI 분석 (매일 07:00, 12:00, 17:00 KST)

    미분석 뉴스를 AI로 분석 (요약, 키워드, ETF 추천)
    크롤링(03:00)과 분리하여 독립적으로 실행
    """
    logger.info("=== AI 뉴스 분석 시작 ===")

    db = SessionLocal()

    try:
        analyzed = await analyze_unprocessed_news(db, limit=200)
        logger.info(f"=== AI 분석 완료: {analyzed}건 처리 ===")
    except Exception as e:
        logger.error(f"AI 뉴스 분석 실패: {e}")
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


async def etf_sync_job():
    """ETF 티커 동기화 (기본 정보 저장) (매일 05:00 KST)"""
    logger.info("=== ETF 동기화 시작 ===")
    from app.services.etf_service import EtfService
    from app.database import AsyncSessionLocal
    async with AsyncSessionLocal() as db:
        try:
            service = EtfService(db)
            await service.sync_etf_tickers()
            logger.info("=== ETF 동기화 완료 ===")
        except Exception as e:
            logger.error(f"ETF 동기화 실패: {e}")

async def etf_active_status_job():
    """ETF 상태 검사 (PDF) 및 활성화 (매일 05:30 KST)"""
    logger.info("=== ETF 상태 검사 및 활성화 시작 ===")
    from app.services.etf_service import EtfService
    from app.database import AsyncSessionLocal
    async with AsyncSessionLocal() as db:
        try:
            service = EtfService(db)
            await service.update_etfs_active_status()
            logger.info("=== ETF 상태 검사 완료 ===")
        except Exception as e:
            logger.error(f"ETF 상태 검사 실패: {e}")

async def company_info_sync_job():
    """회사 정보 누락 주식 동기화 (매일 06:00 KST)"""
    logger.info("=== 회사 정보 누락 주식 동기화 시작 ===")
    from app.services.etf_service import EtfService
    from app.database import AsyncSessionLocal
    async with AsyncSessionLocal() as db:
        try:
            service = EtfService(db)
            await service.update_empty_company_infos()
            logger.info("=== 회사 정보 동기화 완료 ===")
        except Exception as e:
            logger.error(f"회사 정보 동기화 실패: {e}")

async def etf_price_sync_job():
    """ETF 가격 이력 동기화 (매일 00:00 KST)"""
    logger.info("=== ETF 가격 이력 동기화 시작 ===")
    from app.services.etf_service import EtfService
    from app.database import AsyncSessionLocal
    async with AsyncSessionLocal() as db:
        try:
            service = EtfService(db)
            await service.sync_etf_prices()
            logger.info("=== ETF 가격 이력 동기화 완료 ===")
        except Exception as e:
            logger.error(f"ETF 가격 이력 동기화 실패: {e}")

async def kospi_index_sync_job():
    """KOSPI 벤치마크 지수 동기화 (매일 00:30 KST)"""
    logger.info("=== KOSPI 지수 동기화 시작 ===")
    from app.services.benchmark_service import BenchmarkService
    from app.database import AsyncSessionLocal
    async with AsyncSessionLocal() as db:
        try:
            service = BenchmarkService(db)
            await service.sync_kospi_index()
            logger.info("=== KOSPI 지수 동기화 완료 ===")
        except Exception as e:
            logger.error(f"KOSPI 지수 동기화 실패: {e}")

def start_scheduler():
    """스케줄러 시작"""
    # ETF 티커 동기화 (기본 정보) (매일 05:00 KST)
    scheduler.add_job(
        etf_sync_job,
        trigger=CronTrigger(hour=5, minute=0, timezone='Asia/Seoul'),
        id="etf_sync_job",
        name="ETF Daily Sync",
        replace_existing=True
    )
    
    # ETF 활성 상태 (PDF 검사) 업데이트 (매일 05:30 KST)
    scheduler.add_job(
        etf_active_status_job,
        trigger=CronTrigger(hour=5, minute=30, timezone='Asia/Seoul'),
        id="etf_active_status_job",
        name="ETF Active Status Check",
        replace_existing=True
    )
    
    # 회사 정보(ceo_name 등) 업데이트 (매일 06:00 KST)
    scheduler.add_job(
        company_info_sync_job,
        trigger=CronTrigger(hour=6, minute=0, timezone='Asia/Seoul'),
        id="company_info_sync_job",
        name="Company Info Sync",
        replace_existing=True
    )

    # ETF 가격 이력 최신화 (매일 00:00 KST)
    scheduler.add_job(
        etf_price_sync_job,
        trigger=CronTrigger(hour=0, minute=0, timezone='Asia/Seoul'),
        id="etf_price_sync_job",
        name="ETF Price History Sync",
        replace_existing=True
    )
    
    # KOSPI 벤치마크 지수 최신화 (매일 00:30 KST)
    scheduler.add_job(
        kospi_index_sync_job,
        trigger=CronTrigger(hour=0, minute=30, timezone='Asia/Seoul'),
        id="kospi_index_sync_job",
        name="KOSPI Index Sync",
        replace_existing=True
    )

    # ETF 구성종목 뉴스 크롤링 (매일 03:00 KST)
    # - 상위 100개 ETF + 사용자 관심 ETF + 포트폴리오 ETF 구성종목
    scheduler.add_job(
        scrape_stock_news_job,
        trigger=CronTrigger(hour=3, minute=0, timezone='Asia/Seoul'),
        id="stock_news_scraping",
        name="ETF Stock News Scraping",
        replace_existing=True
    )

    # AI 뉴스 분석 (매일 07:00, 12:00, 17:00 KST)
    # 크롤링(03:00)과 분리하여 독립적으로 실행
    for hour in [7, 12, 17]:
        scheduler.add_job(
            news_ai_analysis_job,
            trigger=CronTrigger(hour=hour, minute=0, timezone='Asia/Seoul'),
            id=f"news_ai_analysis_{hour}",
            name=f"News AI Analysis ({hour}:00)",
            replace_existing=True
        )

    # KRX KIND 공시 체크 - 비활성화 (크롤러 문제 해결 후 활성화)
    # scheduler.add_job(
    #     krx_disclosure_job,
    #     trigger=CronTrigger(hour=7, minute=0, timezone='Asia/Seoul'),
    #     id="krx_disclosure_check",
    #     name="KRX KIND Disclosure Check",
    #     replace_existing=True
    # )

    scheduler.start()
    logger.info(
        f"스케줄러 시작:\n"
        f"  - ETF 구성종목 뉴스: 매일 03:00 KST\n"
        f"  - ETF 동기화: 매일 05:00 KST\n"
        f"  - AI 뉴스 분석: 매일 07:00, 12:00, 17:00 KST"
    )
