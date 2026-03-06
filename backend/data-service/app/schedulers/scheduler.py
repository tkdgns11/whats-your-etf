"""뉴스 크롤링, AI 분석, ETF 매핑, KRX 공시 스케줄러"""
import asyncio
import logging
from apscheduler.schedulers.asyncio import AsyncIOScheduler
from apscheduler.triggers.interval import IntervalTrigger
from apscheduler.triggers.cron import CronTrigger
from app.database import SessionLocal
from app.scrapers.news_service import NewsCollectionService
from app.scrapers.krx_scraper import KrxDisclosureScraper
from app.services.news_impact_analyzer import NewsImpactAnalyzer
from app.services.news_etf_mapper import NewsETFMapper
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

        # 뉴스 수집 후 AI 분석 트리거
        if result['total'] > 0:
            await analyze_news_job()

    except Exception as e:
        logger.error(f"뉴스 크롤링 실패: {e}")
    finally:
        await service.close()
        db.close()


async def analyze_news_job():
    """뉴스 AI 분석 작업 (수집 후 자동 실행 또는 15분마다)

    NewsImpactAnalyzer 사용 (Constrained LLM 방식)
    - 회사/산업 목록에서 관련 항목 선택
    - news_impact 테이블에 1:N 매핑 저장
    """
    logger.info("=== 뉴스 AI 분석 시작 ===")

    if not settings.openai_api_key:
        logger.warning("OpenAI API 키 미설정 - AI 분석 건너뜀")
        return

    db = SessionLocal()
    analyzer = NewsImpactAnalyzer(db)

    try:
        # 최근 1시간 내 미분석 뉴스 분석
        result = await analyzer.analyze_recent(hours=1, limit=30)
        logger.info(
            f"=== 뉴스 AI 분석 완료 ===\n"
            f"  대상: {result['total']}건\n"
            f"  성공: {result['success']}건\n"
            f"  영향없음: {result['no_impact']}건\n"
            f"  실패: {result['failed']}건"
        )

        # AI 분석 완료 후 ETF 매핑 트리거
        if result['success'] > 0:
            await map_news_etf_job()

    except Exception as e:
        logger.error(f"뉴스 AI 분석 실패: {e}")
    finally:
        await analyzer.close()
        db.close()


async def map_news_etf_job():
    """뉴스-ETF 매핑 작업 (Step 2a: AI 분석 후 자동 실행)"""
    logger.info("=== 뉴스-ETF 매핑 시작 ===")

    db = SessionLocal()
    mapper = NewsETFMapper(db)

    try:
        result = await mapper.map_recent_news(hours=1, limit=50)
        logger.info(
            f"=== 뉴스-ETF 매핑 완료 ===\n"
            f"  대상: {result['total']}건\n"
            f"  매핑: {result['mapped']}건"
        )
    except Exception as e:
        logger.error(f"뉴스-ETF 매핑 실패: {e}")
    finally:
        await mapper.close()
        db.close()


async def verify_news_etf_job():
    """뉴스-ETF 검증 작업 (Step 2b: 장 마감 후 16:00)"""
    logger.info("=== 뉴스-ETF 검증 시작 ===")

    db = SessionLocal()
    mapper = NewsETFMapper(db)

    try:
        result = await mapper.verify_news_etf_influences()
        logger.info(
            f"=== 뉴스-ETF 검증 완료 ===\n"
            f"  대상: {result['total']}건\n"
            f"  검증: {result['verified']}건\n"
            f"  스킵: {result['skipped']}건"
        )
    except Exception as e:
        logger.error(f"뉴스-ETF 검증 실패: {e}")
    finally:
        await mapper.close()
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
    # 크롤링 완료 후 자동으로 AI 분석 → ETF 매핑 실행
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

    # AI 분석 (15분마다) - 미분석 뉴스 처리
    # 크롤링과 별도로 실행하여 누락된 분석 처리
    # AI 분석 완료 후 자동으로 ETF 매핑 실행
    scheduler.add_job(
        analyze_news_job,
        trigger=IntervalTrigger(minutes=15),
        id="news_ai_analysis",
        name="News AI Analysis",
        replace_existing=True
    )

    # 뉴스-ETF 검증 (장 마감 후 16:00)
    # 실제 주가 데이터 기반 영향도 검증 + 타임라인 생성
    scheduler.add_job(
        verify_news_etf_job,
        trigger=CronTrigger(hour=16, minute=0),
        id="news_etf_verification",
        name="News-ETF Verification (After Market Close)",
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
        f"  - AI 분석 + ETF 매핑: 15분 간격\n"
        f"  - ETF 영향도 검증: 매일 16:00 (장 마감 후)\n"
        f"  - KRX 공시 체크: 매일 09:00"
    )
