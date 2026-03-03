"""What's Your ETF - Data Service (FastAPI)"""
import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import List, Optional
from pydantic import BaseModel
from datetime import datetime

from app.database import get_db, SessionLocal
from app.models.news import NewsArticle
from app.models.etf_disclosure import EtfDisclosure
from app.scrapers.news_service import NewsCollectionService
from app.scrapers.krx_scraper import KrxDisclosureScraper
from app.schedulers.scheduler import start_scheduler, scheduler
from app.config import get_settings

# 로깅 설정
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)

settings = get_settings()


@asynccontextmanager
async def lifespan(app: FastAPI):
    """앱 시작/종료 시 실행"""
    # Startup
    logger.info("FastAPI 시작")
    start_scheduler()

    yield

    # Shutdown
    scheduler.shutdown()
    logger.info("FastAPI 종료")


app = FastAPI(
    title="What's your ETF - Data Service",
    description="뉴스 크롤링 및 데이터 수집 서비스",
    version="0.2.0",
    lifespan=lifespan
)


# ==================== Response Models ====================

class NewsResponse(BaseModel):
    news_id: int
    title: str
    content_summary: str | None
    source: str | None
    source_url: str | None
    category: str | None
    keywords: list | None
    published_at: datetime | None
    created_at: datetime | None

    class Config:
        from_attributes = True


class ScrapeResult(BaseModel):
    google_count: int
    naver_count: int
    content_enriched: int
    total: int


class DisclosureResponse(BaseModel):
    disclosure_id: int
    etf_code: str
    etf_name: str
    disclosure_type: str
    disclosure_title: str
    disclosure_content: str | None
    disclosure_date: datetime | None
    effective_date: datetime | None
    source_url: str | None
    is_notified: str
    created_at: datetime | None

    class Config:
        from_attributes = True


class DisclosureScrapeResult(BaseModel):
    total: int
    new: int


# ==================== API Endpoints ====================

@app.get("/")
async def root():
    return {
        "service": "What's your ETF - Data Service",
        "version": "0.2.0",
        "status": "running",
        "scheduler": "active" if scheduler.running else "stopped"
    }


@app.get("/health")
async def health_check():
    return {"status": "healthy"}


@app.get("/news", response_model=List[NewsResponse])
async def get_news(
    limit: int = Query(20, ge=1, le=100),
    offset: int = Query(0, ge=0),
    keyword: Optional[str] = None,
    source: Optional[str] = None,
    db: Session = Depends(get_db)
):
    """
    최신 뉴스 조회

    - limit: 조회 개수 (1~100)
    - offset: 시작 위치
    - keyword: 키워드 필터 (제목 검색)
    - source: 언론사 필터
    """
    query = db.query(NewsArticle)

    # 필터링
    if keyword:
        query = query.filter(NewsArticle.title.ilike(f"%{keyword}%"))
    if source:
        query = query.filter(NewsArticle.source.ilike(f"%{source}%"))

    # 정렬 및 페이징
    news = query.order_by(NewsArticle.published_at.desc())\
        .offset(offset)\
        .limit(limit)\
        .all()

    return news


@app.get("/news/{news_id}", response_model=NewsResponse)
async def get_news_detail(
    news_id: int,
    db: Session = Depends(get_db)
):
    """뉴스 상세 조회"""
    news = db.query(NewsArticle).filter(NewsArticle.news_id == news_id).first()
    if not news:
        raise HTTPException(status_code=404, detail="뉴스를 찾을 수 없습니다.")
    return news


@app.get("/news/search/", response_model=List[NewsResponse])
async def search_news(
    q: str = Query(..., min_length=1, description="검색어"),
    limit: int = Query(20, ge=1, le=100),
    db: Session = Depends(get_db)
):
    """뉴스 검색 (제목 + 본문)"""
    news = db.query(NewsArticle)\
        .filter(
            (NewsArticle.title.ilike(f"%{q}%")) |
            (NewsArticle.content_summary.ilike(f"%{q}%"))
        )\
        .order_by(NewsArticle.published_at.desc())\
        .limit(limit)\
        .all()
    return news


@app.post("/news/scrape", response_model=ScrapeResult)
async def trigger_scrape(
    full: bool = Query(False, description="전체 키워드 크롤링 여부")
):
    """
    수동으로 뉴스 크롤링 실행

    - full=false: 우선순위 키워드만 (빠름)
    - full=true: 전체 키워드 (느림, 5~10분)
    """
    logger.info(f"수동 크롤링 트리거 (full={full})")

    db = SessionLocal()
    service = NewsCollectionService(db)

    try:
        if full:
            result = await service.collect_full()
        else:
            result = await service.collect_all(enrich_content=True)

        return ScrapeResult(**result)
    except Exception as e:
        logger.error(f"크롤링 실패: {e}")
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        await service.close()
        db.close()


@app.post("/news/scrape/keywords")
async def scrape_by_keywords(
    keywords: List[str] = Query(..., description="크롤링할 키워드 목록")
):
    """특정 키워드로 뉴스 크롤링"""
    if not keywords:
        raise HTTPException(status_code=400, detail="키워드를 입력해주세요.")

    if len(keywords) > 20:
        raise HTTPException(status_code=400, detail="키워드는 최대 20개까지 가능합니다.")

    logger.info(f"키워드 크롤링: {keywords}")

    db = SessionLocal()
    service = NewsCollectionService(db)

    try:
        result = await service.collect_by_keywords(keywords, enrich_content=True)
        return ScrapeResult(**result)
    except Exception as e:
        logger.error(f"크롤링 실패: {e}")
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        await service.close()
        db.close()


@app.get("/scheduler/status")
async def scheduler_status():
    """스케줄러 상태 조회"""
    jobs = []
    for job in scheduler.get_jobs():
        jobs.append({
            "id": job.id,
            "name": job.name,
            "next_run": str(job.next_run_time) if job.next_run_time else None
        })

    return {
        "running": scheduler.running,
        "jobs": jobs
    }


@app.get("/stats")
async def get_stats(db: Session = Depends(get_db)):
    """뉴스 통계 조회"""
    from sqlalchemy import func
    from datetime import timedelta

    total_count = db.query(func.count(NewsArticle.news_id)).scalar()

    # 오늘 수집된 뉴스
    today = datetime.now().replace(hour=0, minute=0, second=0, microsecond=0)
    today_count = db.query(func.count(NewsArticle.news_id))\
        .filter(NewsArticle.created_at >= today).scalar()

    # 본문 있는 뉴스 비율
    with_content = db.query(func.count(NewsArticle.news_id))\
        .filter(NewsArticle.content_summary != None)\
        .filter(NewsArticle.content_summary != "")\
        .scalar()

    # 언론사별 통계
    sources = db.query(
        NewsArticle.source,
        func.count(NewsArticle.news_id).label('count')
    ).group_by(NewsArticle.source)\
     .order_by(func.count(NewsArticle.news_id).desc())\
     .limit(10).all()

    return {
        "total_news": total_count,
        "today_news": today_count,
        "with_content": with_content,
        "content_ratio": round(with_content / total_count * 100, 1) if total_count > 0 else 0,
        "top_sources": [{"source": s[0], "count": s[1]} for s in sources]
    }


# ==================== KRX Disclosure Endpoints ====================

@app.get("/disclosures", response_model=List[DisclosureResponse])
async def get_disclosures(
    limit: int = Query(20, ge=1, le=100),
    offset: int = Query(0, ge=0),
    etf_code: Optional[str] = None,
    disclosure_type: Optional[str] = None,
    db: Session = Depends(get_db)
):
    """
    ETF 공시 목록 조회

    - limit: 조회 개수 (1~100)
    - offset: 시작 위치
    - etf_code: ETF 종목코드 필터
    - disclosure_type: 공시 유형 필터 (delisting, liquidation, caution, surveillance)
    """
    query = db.query(EtfDisclosure)

    if etf_code:
        query = query.filter(EtfDisclosure.etf_code == etf_code)
    if disclosure_type:
        query = query.filter(EtfDisclosure.disclosure_type == disclosure_type)

    disclosures = query.order_by(EtfDisclosure.disclosure_date.desc())\
        .offset(offset)\
        .limit(limit)\
        .all()

    return disclosures


@app.get("/disclosures/pending", response_model=List[DisclosureResponse])
async def get_pending_disclosures(db: Session = Depends(get_db)):
    """알림 미발송 공시 조회 (포트폴리오 매칭용)"""
    disclosures = db.query(EtfDisclosure)\
        .filter(EtfDisclosure.is_notified == "N")\
        .order_by(EtfDisclosure.disclosure_date.desc())\
        .all()
    return disclosures


@app.get("/disclosures/etf/{etf_code}", response_model=List[DisclosureResponse])
async def get_etf_disclosures(
    etf_code: str,
    db: Session = Depends(get_db)
):
    """특정 ETF의 공시 이력 조회"""
    disclosures = db.query(EtfDisclosure)\
        .filter(EtfDisclosure.etf_code == etf_code)\
        .order_by(EtfDisclosure.disclosure_date.desc())\
        .all()
    return disclosures


@app.post("/disclosures/scrape", response_model=DisclosureScrapeResult)
async def trigger_disclosure_scrape(
    days_back: int = Query(7, ge=1, le=30, description="조회 기간 (일)")
):
    """
    수동으로 KRX KIND 공시 수집 실행

    - days_back: 몇 일 전까지 조회할지 (기본 7일, 최대 30일)
    """
    logger.info(f"수동 KRX 공시 수집 트리거 (days_back={days_back})")

    db = SessionLocal()
    scraper = KrxDisclosureScraper(db)

    try:
        result = await scraper.scrape_disclosures(days_back=days_back)
        return DisclosureScrapeResult(**result)
    except Exception as e:
        logger.error(f"KRX 공시 수집 실패: {e}")
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        await scraper.close()
        db.close()


@app.patch("/disclosures/{disclosure_id}/notified")
async def mark_disclosure_notified(
    disclosure_id: int,
    db: Session = Depends(get_db)
):
    """공시 알림 발송 완료 처리"""
    disclosure = db.query(EtfDisclosure)\
        .filter(EtfDisclosure.disclosure_id == disclosure_id)\
        .first()

    if not disclosure:
        raise HTTPException(status_code=404, detail="공시를 찾을 수 없습니다.")

    disclosure.is_notified = "Y"
    db.commit()

    return {"message": "알림 발송 완료 처리됨", "disclosure_id": disclosure_id}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
