"""Google News RSS 기반 뉴스 크롤러"""
import httpx
from bs4 import BeautifulSoup
from datetime import datetime
from email.utils import parsedate_to_datetime
import logging
import asyncio
from typing import Optional
from sqlalchemy.orm import Session

from app.models.news import NewsArticle
from app.config import get_settings
from app.utils.dedup import DuplicateChecker

logger = logging.getLogger(__name__)
settings = get_settings()


class GoogleNewsScraper:
    """Google News RSS 기반 뉴스 크롤러"""

    BASE_URL = "https://news.google.com/rss/search"
    USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
    TIMEOUT = 15

    def __init__(self, db: Session):
        self.db = db
        self.client = httpx.AsyncClient(
            headers={"User-Agent": self.USER_AGENT},
            timeout=self.TIMEOUT,
            follow_redirects=True
        )
        self.dedup_checker = DuplicateChecker(db, similarity_threshold=0.85)

    async def close(self):
        await self.client.aclose()
        self.dedup_checker.clear_cache()

    async def scrape_by_keyword(self, keyword: str, max_items: int = 5) -> int:
        """특정 키워드로 뉴스 수집"""
        url = f"{self.BASE_URL}?q={keyword}&hl=ko&gl=KR&ceid=KR:ko"

        try:
            response = await self.client.get(url)
            response.raise_for_status()
        except httpx.HTTPError as e:
            logger.error(f"HTTP 요청 실패: {e}")
            return 0

        soup = BeautifulSoup(response.text, "xml")
        items = soup.find_all("item")

        count = 0
        for item in items[:max_items]:
            try:
                news = self._parse_item(item, keyword)
                if news:
                    # 중복 체크
                    if not self.dedup_checker.is_duplicate(news.title, news.source_url):
                        self.db.add(news)
                        self.db.commit()
                        count += 1
            except Exception as e:
                logger.error(f"뉴스 파싱 실패: {e}")
                self.db.rollback()
                continue

        return count

    async def scrape_keywords(self, keywords: list, max_per_keyword: int = 5) -> int:
        """여러 키워드로 뉴스 수집"""
        total_count = 0

        for keyword in keywords:
            try:
                count = await self.scrape_by_keyword(keyword, max_per_keyword)
                total_count += count
                logger.info(f"[Google][{keyword}] {count}건 수집")
                await asyncio.sleep(1)  # Rate limit
            except Exception as e:
                logger.error(f"[Google][{keyword}] 크롤링 실패: {e}")
                continue

        logger.info(f"[Google] 총 {total_count}건 수집 완료")
        return total_count

    def _parse_item(self, item, keyword: str) -> Optional[NewsArticle]:
        """RSS item을 NewsArticle로 변환"""
        title_tag = item.find("title")
        link_tag = item.find("link")
        desc_tag = item.find("description")
        pub_date_tag = item.find("pubDate")
        source_tag = item.find("source")

        if not title_tag or not link_tag:
            return None

        title = title_tag.get_text(strip=True)
        link = link_tag.get_text(strip=True)

        # description에서 HTML 태그 제거
        description = ""
        if desc_tag:
            desc_soup = BeautifulSoup(desc_tag.get_text(), "html.parser")
            description = desc_soup.get_text(strip=True)
            if len(description) > 500:
                description = description[:497] + "..."

        # 발행일 파싱
        published_at = None
        if pub_date_tag:
            try:
                published_at = parsedate_to_datetime(pub_date_tag.get_text())
            except Exception:
                published_at = datetime.now()

        # 출처
        source_name = "Google News"
        if source_tag:
            source_name = source_tag.get_text(strip=True)

        return NewsArticle(
            title=title,
            content_summary=description if description else None,
            source=source_name,
            source_url=link,
            category="금융",
            keywords=[keyword.replace("+", " ")],
            published_at=published_at
        )


# 하위 호환성을 위한 별칭
NewsScraper = GoogleNewsScraper
