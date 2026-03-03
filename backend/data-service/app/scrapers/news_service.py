"""통합 뉴스 수집 서비스"""
import logging
import asyncio
from typing import List, Optional
from sqlalchemy.orm import Session

from app.scrapers.news_scraper import GoogleNewsScraper
from app.scrapers.naver_scraper import NaverNewsScraper
from app.scrapers.content_scraper import ContentScraper, enrich_news_content
from app.scrapers.keywords import get_all_keywords, get_priority_keywords
from app.models.news import NewsArticle
from app.config import get_settings

logger = logging.getLogger(__name__)
settings = get_settings()


class NewsCollectionService:
    """
    통합 뉴스 수집 서비스

    - Google News RSS + Naver News API 병합
    - 중복 제거
    - 주요 언론사 본문 크롤링
    """

    def __init__(self, db: Session):
        self.db = db
        self.google_scraper = GoogleNewsScraper(db)
        self.naver_scraper = NaverNewsScraper(db)
        self.content_scraper = ContentScraper()

    async def close(self):
        """모든 스크래퍼 종료"""
        await self.google_scraper.close()
        await self.naver_scraper.close()
        await self.content_scraper.close()

    async def collect_all(self, enrich_content: bool = True) -> dict:
        """
        전체 뉴스 수집 (스케줄러용)

        Args:
            enrich_content: 본문 크롤링 여부

        Returns:
            {
                "google_count": int,
                "naver_count": int,
                "content_enriched": int,
                "total": int
            }
        """
        result = {
            "google_count": 0,
            "naver_count": 0,
            "content_enriched": 0,
            "total": 0
        }

        keywords = get_priority_keywords()  # 우선순위 키워드만 (속도)
        logger.info(f"뉴스 수집 시작: {len(keywords)}개 키워드")

        # 1. Google News 수집
        try:
            result["google_count"] = await self.google_scraper.scrape_keywords(
                keywords, max_per_keyword=3
            )
        except Exception as e:
            logger.error(f"Google News 수집 실패: {e}")

        # 2. Naver News 수집
        if self.naver_scraper.is_configured():
            try:
                result["naver_count"] = await self.naver_scraper.scrape_keywords(
                    keywords, display_per_keyword=5
                )
            except Exception as e:
                logger.error(f"Naver News 수집 실패: {e}")
        else:
            logger.warning("Naver API 키 미설정 - Naver 수집 건너뜀")

        result["total"] = result["google_count"] + result["naver_count"]

        # 3. 본문 크롤링 (최근 수집된 뉴스 중 본문 없는 것)
        if enrich_content and result["total"] > 0:
            result["content_enriched"] = await self._enrich_recent_news()

        logger.info(
            f"뉴스 수집 완료: Google {result['google_count']}건, "
            f"Naver {result['naver_count']}건, "
            f"본문보강 {result['content_enriched']}건"
        )

        return result

    async def collect_by_keywords(self, keywords: List[str],
                                   enrich_content: bool = True) -> dict:
        """
        특정 키워드로 뉴스 수집

        Args:
            keywords: 수집할 키워드 리스트
            enrich_content: 본문 크롤링 여부
        """
        result = {
            "google_count": 0,
            "naver_count": 0,
            "content_enriched": 0,
            "total": 0
        }

        # Google
        try:
            result["google_count"] = await self.google_scraper.scrape_keywords(
                keywords, max_per_keyword=5
            )
        except Exception as e:
            logger.error(f"Google News 수집 실패: {e}")

        # Naver
        if self.naver_scraper.is_configured():
            try:
                result["naver_count"] = await self.naver_scraper.scrape_keywords(
                    keywords, display_per_keyword=5
                )
            except Exception as e:
                logger.error(f"Naver News 수집 실패: {e}")

        result["total"] = result["google_count"] + result["naver_count"]

        # 본문 보강
        if enrich_content and result["total"] > 0:
            result["content_enriched"] = await self._enrich_recent_news(limit=result["total"])

        return result

    async def _enrich_recent_news(self, limit: int = 50) -> int:
        """
        최근 수집된 뉴스 중 본문이 없는 것에 본문 추가

        Args:
            limit: 처리할 최대 건수

        Returns:
            본문 추가된 건수
        """
        from datetime import datetime, timedelta

        # 최근 1시간 내 수집된 뉴스 중 본문 없는 것
        cutoff = datetime.now() - timedelta(hours=1)

        news_to_enrich = self.db.query(NewsArticle).filter(
            NewsArticle.created_at >= cutoff,
            (NewsArticle.content_summary == None) |
            (NewsArticle.content_summary == "")
        ).limit(limit).all()

        enriched_count = 0

        for news in news_to_enrich:
            try:
                success = await enrich_news_content(news, self.content_scraper)
                if success:
                    self.db.commit()
                    enriched_count += 1
                    await asyncio.sleep(0.5)  # Rate limit
            except Exception as e:
                logger.error(f"본문 보강 실패 [{news.news_id}]: {e}")
                self.db.rollback()
                continue

        logger.info(f"본문 보강 완료: {enriched_count}/{len(news_to_enrich)}건")
        return enriched_count

    async def collect_full(self) -> dict:
        """
        전체 키워드로 뉴스 수집 (일일 배치용)
        주의: 시간이 오래 걸림 (5~10분)
        """
        keywords = get_all_keywords()
        logger.info(f"전체 뉴스 수집 시작: {len(keywords)}개 키워드")

        return await self.collect_by_keywords(keywords, enrich_content=True)


# 스케줄러에서 사용할 함수
async def scheduled_news_collection(db: Session) -> dict:
    """스케줄러에서 호출하는 뉴스 수집 함수"""
    service = NewsCollectionService(db)
    try:
        return await service.collect_all(enrich_content=True)
    finally:
        await service.close()
