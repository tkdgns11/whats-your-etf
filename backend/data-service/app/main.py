"""What's Your ETF - Data Service (FastAPI)"""
import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI, Depends, HTTPException, Query
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import Session
from typing import List, Optional
from pydantic import BaseModel
from datetime import datetime

from app.database import get_db, SessionLocal, get_async_db
from app.models.news import NewsArticle
from app.models.etf_disclosure import EtfDisclosure
from app.models.etf import ETF, ETFSectorCluster
from app.scrapers.news_service import NewsCollectionService
from app.scrapers.krx_scraper import KrxDisclosureScraper
from app.schedulers.scheduler import start_scheduler, scheduler
from app.config import get_settings
from app.scrapers.keywords import NEWS_CATEGORIES
from app.database import AsyncSessionLocal
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
    category_name: str | None
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


# ==================== ETF Sector Cluster Models ====================

class SectorItem(BaseModel):
    """섹터 버블 정보"""
    group_code: str | None
    group_name: str | None
    weight_pct: float
    stock_count: int | None
    pos_x: float | None
    pos_y: float | None
    radius: float | None
    distance_to_center: float | None

    class Config:
        from_attributes = True


class CenterPoint(BaseModel):
    """클러스터 중심점"""
    x: float = 0.5
    y: float = 0.5


class SectorClusterResponse(BaseModel):
    """ETF 섹터 클러스터 응답"""
    etf_id: int
    etf_name: str
    cluster_type: str
    base_date: str | None
    center: CenterPoint
    sectors: List[SectorItem]




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
    category: Optional[str] = Query(None, description="카테고리 코드 (NEWS_SEMI, NEWS_IT 등)"),
    db: Session = Depends(get_db)
):
    """
    최신 뉴스 조회

    - limit: 조회 개수 (1~100)
    - offset: 시작 위치
    - keyword: 키워드 필터 (제목 검색)
    - source: 언론사 필터
    - category: 카테고리 필터 (NEWS_SEMI, NEWS_IT, NEWS_BIO 등)
    """
    query = db.query(NewsArticle)

    # 필터링
    if keyword:
        query = query.filter(NewsArticle.title.ilike(f"%{keyword}%"))
    if source:
        query = query.filter(NewsArticle.source.ilike(f"%{source}%"))
    if category:
        query = query.filter(NewsArticle.category == category)

    # 정렬 및 페이징
    news = query.order_by(NewsArticle.published_at.desc())\
        .offset(offset)\
        .limit(limit)\
        .all()

    return news


@app.get("/news/categories")
async def get_news_categories():
    """
    뉴스 카테고리 목록 조회

    Returns:
        카테고리 코드와 이름 목록
    """
    return [
        {"code": code, "name": name}
        for code, name in NEWS_CATEGORIES.items()
    ]


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
    category: Optional[str] = Query(None, description="카테고리 코드"),
    db: Session = Depends(get_db)
):
    """뉴스 검색 (제목 + 본문)"""
    query = db.query(NewsArticle).filter(
        (NewsArticle.title.ilike(f"%{q}%")) |
        (NewsArticle.content_summary.ilike(f"%{q}%"))
    )

    if category:
        query = query.filter(NewsArticle.category == category)

    news = query.order_by(NewsArticle.published_at.desc())\
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


# ==================== ETF Sector Cluster Endpoints ====================

@app.get("/etf/{etf_id}/sector-cluster", response_model=SectorClusterResponse)
async def get_etf_sector_cluster(
    etf_id: int,
    db: Session = Depends(get_db)
):
    """
    ETF 섹터 클러스터 조회 (버블 시각화용)

    - etf_id: ETF ID
    - 반환: 섹터별 비중 + 시각화 좌표 (pos_x, pos_y, radius, distance_to_center)
    """
    # ETF 존재 확인
    etf = db.query(ETF).filter(ETF.id == etf_id).first()
    if not etf:
        raise HTTPException(status_code=404, detail="ETF를 찾을 수 없습니다.")

    # 섹터 분포 조회 (비중 내림차순)
    clusters = db.query(ETFSectorCluster)\
        .filter(ETFSectorCluster.etf_id == etf_id)\
        .order_by(ETFSectorCluster.weight_pct.desc())\
        .all()

    if not clusters:
        raise HTTPException(status_code=404, detail="섹터 분포 데이터가 없습니다.")

    # 응답 생성
    sectors = []
    for sc in clusters:
        sectors.append(SectorItem(
            group_code=sc.group_code,
            group_name=sc.group_name,
            weight_pct=float(sc.weight_pct) if sc.weight_pct else 0,
            stock_count=sc.stock_count,
            pos_x=float(sc.pos_x) if sc.pos_x else None,
            pos_y=float(sc.pos_y) if sc.pos_y else None,
            radius=float(sc.radius) if sc.radius else None,
            distance_to_center=float(sc.distance_to_center) if sc.distance_to_center else None
        ))

    return SectorClusterResponse(
        etf_id=etf.id,
        etf_name=etf.name,
        cluster_type=clusters[0].cluster_type if clusters else "GROUP_CODE",
        base_date=str(clusters[0].base_date) if clusters and clusters[0].base_date else None,
        center=CenterPoint(),
        sectors=sectors
    )


@app.get("/etf/sector-clusters", response_model=List[SectorClusterResponse])
async def get_all_sector_clusters(
    limit: int = Query(10, ge=1, le=50),
    db: Session = Depends(get_db)
):
    """
    모든 ETF의 섹터 클러스터 조회

    - limit: 조회할 ETF 수 (기본 10, 최대 50)
    """
    # 활성 ETF 조회
    etfs = db.query(ETF)\
        .filter(ETF.is_active == True)\
        .limit(limit)\
        .all()

    results = []
    for etf in etfs:
        clusters = db.query(ETFSectorCluster)\
            .filter(ETFSectorCluster.etf_id == etf.id)\
            .order_by(ETFSectorCluster.weight_pct.desc())\
            .all()

        if not clusters:
            continue

        sectors = []
        for sc in clusters:
            sectors.append(SectorItem(
                group_code=sc.group_code,
                group_name=sc.group_name,
                weight_pct=float(sc.weight_pct) if sc.weight_pct else 0,
                stock_count=sc.stock_count,
                pos_x=float(sc.pos_x) if sc.pos_x else None,
                pos_y=float(sc.pos_y) if sc.pos_y else None,
                radius=float(sc.radius) if sc.radius else None,
                distance_to_center=float(sc.distance_to_center) if sc.distance_to_center else None
            ))

        results.append(SectorClusterResponse(
            etf_id=etf.id,
            etf_name=etf.name,
            cluster_type=clusters[0].cluster_type,
            base_date=str(clusters[0].base_date) if clusters[0].base_date else None,
            center=CenterPoint(),
            sectors=sectors
        ))

    return results


# ==================== ETF Sync Test Endpoints (remove after validation) ====================

@app.post("/dev/etf/sync/tickers")
async def dev_sync_etf_tickers():
    """[TEST] ETF 티커 동기화 — DB에 없는 신규 ETF를 기본 정보와 함께 저장합니다."""
    logger.info("[DEV] ETF 티커 동기화 수동 트리거")
    from app.services.etf_service import EtfService
    async with AsyncSessionLocal() as db:
        try:
            service = EtfService(db)
            await service.sync_etf_tickers()
            return {"status": "ok", "job": "sync_etf_tickers"}
        except Exception as e:
            logger.error(f"[DEV] ETF 티커 동기화 실패: {e}")
            raise HTTPException(status_code=500, detail=str(e))


@app.post("/dev/etf/sync/active-status")
async def dev_sync_etf_active_status():
    """[TEST] ETF 활성 상태 검사 — PDF 구성종목 기반으로 is_krx_only 플래그를 업데이트합니다."""
    logger.info("[DEV] ETF 활성 상태 검사 수동 트리거")
    from app.services.etf_service import EtfService
    async with AsyncSessionLocal() as db:
        try:
            service = EtfService(db)
            await service.update_etfs_active_status()
            return {"status": "ok", "job": "update_etfs_active_status"}
        except Exception as e:
            logger.error(f"[DEV] ETF 활성 상태 검사 실패: {e}")
            raise HTTPException(status_code=500, detail=str(e))


@app.post("/dev/etf/sync/metadata")
async def dev_sync_etf_metadata():
    """[TEST] ETF 메타데이터 동기화 — KIS API로 AUM, NAV, 운용사, 배당주기, expense_ratio, dividend_yield 등을 업데이트합니다."""
    logger.info("[DEV] ETF 메타데이터 동기화 수동 트리거")
    from app.services.etf_service import EtfService
    async with AsyncSessionLocal() as db:
        try:
            service = EtfService(db)
            await service.sync_etf_metadata()
            return {"status": "ok", "job": "sync_etf_metadata"}
        except Exception as e:
            logger.error(f"[DEV] ETF 메타데이터 동기화 실패: {e}")
            raise HTTPException(status_code=500, detail=str(e))


@app.post("/dev/etf/sync/stock-fundamentals")
async def dev_sync_stock_fundamentals():
    """[TEST] 종목 재무지표 동기화 — pykrx로 KOSPI/KOSDAQ 전 종목 PER/PBR/ROE를 일괄 업데이트합니다."""
    logger.info("[DEV] 종목 재무지표 동기화 수동 트리거")
    from app.services.etf_service import EtfService
    async with AsyncSessionLocal() as db:
        try:
            service = EtfService(db)
            await service.sync_stock_fundamentals()
            return {"status": "ok", "job": "sync_stock_fundamentals"}
        except Exception as e:
            logger.error(f"[DEV] 종목 재무지표 동기화 실패: {e}")
            raise HTTPException(status_code=500, detail=str(e))


@app.post("/dev/etf/sync/etf-fundamentals")
async def dev_sync_etf_fundamentals():
    """[TEST] ETF 재무지표 계산 — 구성종목 비중 가중평균으로 ETF per/pbr/roe를 계산합니다."""
    logger.info("[DEV] ETF 재무지표 계산 수동 트리거")
    from app.services.etf_service import EtfService
    async with AsyncSessionLocal() as db:
        try:
            service = EtfService(db)
            await service.sync_etf_fundamentals()
            return {"status": "ok", "job": "sync_etf_fundamentals"}
        except Exception as e:
            logger.error(f"[DEV] ETF 재무지표 계산 실패: {e}")
            raise HTTPException(status_code=500, detail=str(e))


@app.post("/dev/etf/sync/risk-type")
async def dev_sync_etf_risk_type():
    """[TEST] ETF 위험유형 계산 — ETF 속성 기반으로 risk_type을 업데이트합니다."""
    logger.info("[DEV] ETF 위험유형 계산 수동 트리거")
    from app.services.etf_service import EtfService
    async with AsyncSessionLocal() as db:
        try:
            service = EtfService(db)
            await service.sync_etf_risk_type()
            return {"status": "ok", "job": "sync_etf_risk_type"}
        except Exception as e:
            logger.error(f"[DEV] ETF 위험유형 계산 실패: {e}")
            raise HTTPException(status_code=500, detail=str(e))


@app.post("/dev/etf/sync/all")
async def dev_sync_all():
    """[TEST] 전체 ETF 데이터 파이프라인 (병렬 실행)

    실행 순서:
    1. sync_etf_tickers (신규 ETF 저장)
    2. update_etfs_active_status + sync_etf_metadata (병렬)
    3. sync_stock_fundamentals (재무지표)
    4. sync_etf_fundamentals + sync_etf_risk_type (병렬)
    """
    logger.info("[DEV] 전체 ETF 파이프라인 수동 트리거 (병렬 처리)")
    results = {}
    from app.services.etf_service import EtfService

    async def run_step(name: str, method_name: str):
        try:
            logger.info(f"[DEV] 단계 시작: {name}")
            async with AsyncSessionLocal() as db:
                service = EtfService(db)
                fn = getattr(service, method_name)
                await fn()
            results[name] = "ok"
            logger.info(f"[DEV] 단계 완료: {name}")
            return True
        except Exception as e:
            logger.error(f"[DEV] 단계 실패 [{name}]: {e}", exc_info=True)
            results[name] = f"error: {str(e)[:100]}"
            return False

    # 1단계: 신규 ETF 저장
    if not await run_step("sync_etf_tickers", "sync_etf_tickers"):
        return {"status": "failed at step 1", "results": results}

    # 2단계: 상태 확인 + 메타데이터 (병렬)
    await asyncio.gather(
        run_step("update_etfs_active_status", "update_etfs_active_status"),
        run_step("sync_etf_metadata", "sync_etf_metadata"),
        return_exceptions=True
    )

    # 3단계: 재무지표
    if not await run_step("sync_stock_fundamentals", "sync_stock_fundamentals"):
        logger.warning("[DEV] 재무지표 동기화 실패, 계속 진행...")

    # 4단계: ETF 가중평균 + 위험유형 (병렬)
    await asyncio.gather(
        run_step("sync_etf_fundamentals", "sync_etf_fundamentals"),
        run_step("sync_etf_risk_type", "sync_etf_risk_type"),
        return_exceptions=True
    )

    return {"status": "done", "results": results}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
