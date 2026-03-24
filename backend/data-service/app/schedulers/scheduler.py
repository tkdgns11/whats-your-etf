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
from app.models.etf import ETF
from app.models.company import CompanyInfo
from app.config import get_settings

logger = logging.getLogger(__name__)
settings = get_settings()

scheduler = AsyncIOScheduler()


async def scrape_stock_news_job():
    """ETF 구성종목 뉴스 크롤링 (03:00, 12:00 KST - 하루 2회)

    뉴스 크롤링 후 AI 분석 자동 실행.
    종목당 최대 3개 뉴스 수집 (429 에러 방지)
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
                        max_articles=3  # 429 에러 방지 (네이버 rate limit)
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

        # 크롤링 완료 후 AI 분석 즉시 실행
        if total_stats['new'] > 0:
            logger.info("=== 크롤링 완료 후 AI 분석 시작 ===")
            await news_ai_analysis_job()

    except Exception as e:
        logger.error(f"종목뉴스 크롤링 실패: {e}")
    finally:
        db.close()


async def news_ai_analysis_job():
    """뉴스 AI 분석 (매 3시간마다 실행)

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

async def etf_metadata_sync_job():
    """ETF 상세 메타데이터 동기화 (AUM, NAV, 운용사, 배당주기 등) (매주 일요일 03:30 KST)"""
    logger.info("=== ETF 상세 메타데이터 동기화 시작 ===")
    from app.services.etf_service import EtfService
    from app.database import AsyncSessionLocal
    async with AsyncSessionLocal() as db:
        try:
            service = EtfService(db)
            await service.sync_etf_metadata()
            logger.info("=== ETF 상세 메타데이터 동기화 완료 ===")
        except Exception as e:
            logger.error(f"ETF 상세 메타데이터 동기화 실패: {e}")

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

async def sync_missing_stock_descriptions_job():
    """네이버 증권 기업개요 누락분 동기화 (주 1회 등 스케줄러용)"""
    logger.info("=== Stock Description 누락분 네이버 크롤링 동기화 시작 ===")
    from app.scrapers.naver_finance_scraper import crawl_stock_description
    from app.database import AsyncSessionLocal
    from sqlalchemy import select, update
    from app.models.stock import Stock
    
    async with AsyncSessionLocal() as db:
        try:
            # description이 없는 주식 최대 500개 우선 조회
            stmt = select(Stock).where(
                (Stock.description.is_(None)) | (Stock.description == "")
            ).limit(500)
            result = await db.execute(stmt)
            stocks = result.scalars().all()
            
            if not stocks:
                logger.info("=== 업데이트가 필요한 Stock Description이 없습니다 ===")
                return
                
            logger.info(f"총 {len(stocks)}개의 주식 description 크롤링을 시작합니다.")
            
            success_count = 0
            for stock in stocks:
                desc = await crawl_stock_description(stock.ticker)
                if desc:
                    stock.description = desc
                    success_count += 1
            
            await db.commit()
            logger.info(f"=== Stock Description 동기화 완료: {success_count}/{len(stocks)}건 업데이트 성공 ===")
            
        except Exception as e:
            logger.error(f"Stock Description 동기화 중 에러 발생: {e}")
            await db.rollback()

async def sync_etf_stock_cache_job():
    """정규 장 시간(09:00 ~ 15:30) 동안 ETF 및 구성종목 캐시를 Redis에 업데이트"""
    from datetime import datetime, timezone, timedelta
    now = datetime.now(timezone(timedelta(hours=9)))
    
    # 오전 9시 이전, 또는 15시 30분 이후이면 스킵
    if now.hour < 9 or (now.hour == 15 and now.minute > 30):
        return

    logger.info("=== ETF 및 구성종목(Stock) 실시간 캐시 업데이트 시작 ===")
    
    from app.database import AsyncSessionLocal
    from sqlalchemy import select
    from app.models.etf import ETF
    from app.services.cache_service import RedisCacheService
    
    async with AsyncSessionLocal() as db:
        try:
            # 활성화된 ETF 목록만 가져옴
            stmt = select(ETF).where(ETF.is_active == True)
            result = await db.execute(stmt)
            etfs = result.scalars().all()
            
            if not etfs:
                logger.warning("활성화된 ETF가 없어 캐시 업데이트를 종료합니다.")
                return
                
            cache_service = RedisCacheService()
            # 비동기로 모든 ETF의 캐시 업데이트 실행 (KISClient 내부에서 18/s 동시성 제어됨)
            import asyncio
            tasks = [cache_service.publish_etf_cache(etf.stock_code) for etf in etfs if etf.stock_code]
            await asyncio.gather(*tasks, return_exceptions=True)
            
            await cache_service.close()
            logger.info(f"=== 총 {len(etfs)}개 ETF 캐시 업데이트 성공 ===")
            
        except Exception as e:
            logger.error(f"실시간 캐시 업데이트 스케줄러 실패: {e}")

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
    
    # ETF 상세 메타데이터 동기화 (AUM, NAV, 운용사 등) (매주 일요일 03:30 KST)
    scheduler.add_job(
        etf_metadata_sync_job,
        trigger=CronTrigger(day_of_week='sun', hour=3, minute=30, timezone='Asia/Seoul'),
        id="etf_metadata_sync_job",
        name="ETF Metadata Sync",
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
    
    # 주식 기업개요(설명) 누락분 네이버 크롤링 동기화 (매주 토요일 02:00 KST)
    scheduler.add_job(
        sync_missing_stock_descriptions_job,
        trigger=CronTrigger(day_of_week='sat', hour=2, minute=0, timezone='Asia/Seoul'),
        id="sync_missing_stock_descriptions_job",
        name="Sync Missing Stock Descriptions",
        replace_existing=True
    )

    # ETF/Stock 실시간 캐시 업데이트 (평일 09:00~15:30 매 1분마다)
    scheduler.add_job(
        sync_etf_stock_cache_job,
        trigger=CronTrigger(day_of_week='mon-fri', hour='9-15', minute='*', timezone='Asia/Seoul'),
        id="sync_etf_stock_cache_job",
        name="ETF and Stock Realtime Cache Sync",
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

    # ETF 구성종목 뉴스 크롤링 (03:00, 12:00 KST - 하루 2회)
    # - 상위 100개 ETF + 사용자 관심 ETF + 포트폴리오 ETF 구성종목
    for hour in [3, 12]:
        scheduler.add_job(
            scrape_stock_news_job,
            trigger=CronTrigger(hour=hour, minute=0, timezone='Asia/Seoul'),
            id=f"stock_news_scraping_{hour}",
            name=f"ETF Stock News Scraping ({hour}:00)",
            replace_existing=True
        )

    # AI 뉴스 분석 (매 3시간마다: 00:00, 04:00, 06:00, 09:00, 12:00, 15:00, 18:00, 21:00 KST)
    # 크롤링 완료 후 즉시 실행 + 04:00 백업 + 이후 매 3시간
    for hour in [0, 6, 9, 12, 15, 18, 21]:
        scheduler.add_job(
            news_ai_analysis_job,
            trigger=CronTrigger(hour=hour, minute=0, timezone='Asia/Seoul'),
            id=f"news_ai_analysis_{hour}",
            name=f"News AI Analysis ({hour}:00)",
            replace_existing=True
        )
    # 크롤링(03:00, 12:00) 완료 후 백업 실행 (04:00, 13:00)
    for hour in [4, 13]:
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
        f"  - ETF 구성종목 뉴스: 03:00, 12:00 KST (하루 2회)\n"
        f"  - ETF 동기화: 매일 05:00 KST\n"
        f"  - AI 뉴스 분석: 크롤링 직후 + 백업(04:00, 13:00) + 매 3시간"
    )
