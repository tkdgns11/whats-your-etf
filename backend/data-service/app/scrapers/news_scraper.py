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
from app.utils.spam_filter import is_spam
from app.scrapers.keywords import get_category_by_keyword

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
            follow_redirects=False  # 리다이렉트 수동 처리
        )
        self.dedup_checker = DuplicateChecker(db, similarity_threshold=0.85)

    async def _resolve_google_redirect(self, google_url: str) -> Optional[str]:
        """Google News 리다이렉트 URL에서 실제 기사 URL 추출"""
        try:
            response = await self.client.get(google_url, follow_redirects=True)
            # 최종 URL 반환 (리다이렉트 따라감)
            return str(response.url)
        except Exception as e:
            logger.debug(f"URL 리다이렉트 실패: {e}")
            return google_url  # 실패 시 원본 반환

    async def close(self):
        await self.client.aclose()
        self.dedup_checker.clear_cache()

    async def scrape_by_keyword(self, keyword: str, max_items: int = 5) -> dict:
        """
        특정 키워드로 뉴스 수집

        Returns:
            {"saved": int, "spam": int, "duplicate": int}
        """
        url = f"{self.BASE_URL}?q={keyword}&hl=ko&gl=KR&ceid=KR:ko"
        stats = {"saved": 0, "spam": 0, "duplicate": 0}

        try:
            response = await self.client.get(url, follow_redirects=True)
            response.raise_for_status()
        except httpx.HTTPError as e:
            logger.error(f"HTTP 요청 실패: {e}")
            return stats

        soup = BeautifulSoup(response.text, "xml")
        items = soup.find_all("item")

        for item in items[:max_items]:
            try:
                news = await self._parse_item(item, keyword)
                if news is None:
                    # Spam filter에서 걸러짐
                    stats["spam"] += 1
                    continue

                # 중복 체크
                if self.dedup_checker.is_duplicate(news.title, news.source_url):
                    stats["duplicate"] += 1
                    continue

                self.db.add(news)
                self.db.commit()
                stats["saved"] += 1

            except Exception as e:
                logger.error(f"뉴스 파싱 실패: {e}")
                self.db.rollback()
                continue

        return stats

    async def scrape_keywords(self, keywords: list, max_per_keyword: int = 5) -> dict:
        """
        여러 키워드로 뉴스 수집

        Returns:
            {"saved": int, "spam": int, "duplicate": int}
        """
        total_stats = {"saved": 0, "spam": 0, "duplicate": 0}

        for keyword in keywords:
            try:
                stats = await self.scrape_by_keyword(keyword, max_per_keyword)
                total_stats["saved"] += stats["saved"]
                total_stats["spam"] += stats["spam"]
                total_stats["duplicate"] += stats["duplicate"]

                logger.info(f"[Google][{keyword}] 저장:{stats['saved']} 스팸:{stats['spam']} 중복:{stats['duplicate']}")
                await asyncio.sleep(1)  # Rate limit
            except Exception as e:
                logger.error(f"[Google][{keyword}] 크롤링 실패: {e}")
                continue

        logger.info(
            f"[Google] 총 저장:{total_stats['saved']} 스팸:{total_stats['spam']} 중복:{total_stats['duplicate']}"
        )
        return total_stats

    async def _parse_item(self, item, keyword: str) -> Optional[NewsArticle]:
        """RSS item을 NewsArticle로 변환"""
        title_tag = item.find("title")
        link_tag = item.find("link")
        desc_tag = item.find("description")
        pub_date_tag = item.find("pubDate")
        source_tag = item.find("source")

        if not title_tag or not link_tag:
            return None

        title = title_tag.get_text(strip=True)

        # description 먼저 추출 (스팸 필터용)
        description = ""
        if desc_tag:
            desc_soup = BeautifulSoup(desc_tag.get_text(), "html.parser")
            description = desc_soup.get_text(strip=True)
            if len(description) > 500:
                description = description[:497] + "..."

        # Spam Filter 체크
        spam_result = is_spam(title, description)
        if spam_result.is_spam:
            logger.debug(f"[Spam] {title[:50]}... ({spam_result.reason}: {spam_result.matched_keyword})")
            return None

        google_link = link_tag.get_text(strip=True)

        # Google 리다이렉트 URL에서 실제 기사 URL 추출
        actual_url = await self._resolve_google_redirect(google_link)
        logger.debug(f"URL 변환: {google_link[:50]}... → {actual_url[:50]}...")

        # 발행일 파싱
        published_at = None
        if pub_date_tag:
            try:
                published_at = parsedate_to_datetime(pub_date_tag.get_text())
            except Exception:
                published_at = datetime.now()

        # 출처 (RSS source 태그 또는 URL에서 추출)
        source_name = "Google News"
        if source_tag:
            source_name = source_tag.get_text(strip=True)
        else:
            # URL에서 도메인 추출하여 언론사명 매핑
            source_name = self._extract_source_from_url(actual_url)

        # 검색 키워드로 카테고리 자동 할당
        category = get_category_by_keyword(keyword)

        return NewsArticle(
            title=title,
            content=description if description else None,  # RSS snippet → content (본문 크롤링 시 덮어씀)
            # content_summary는 LLM이 생성 (JSONB bullets)
            # keywords는 LLM이 생성
            source=source_name,
            source_url=actual_url,  # 실제 기사 URL 저장
            category=category,
            published_at=published_at
        )

    def _extract_source_from_url(self, url: str) -> str:
        """URL에서 언론사명 추출"""
        source_map = {
            "hankyung.com": "한국경제",
            "mk.co.kr": "매일경제",
            "yna.co.kr": "연합뉴스",
            "chosun.com": "조선일보",
            "donga.com": "동아일보",
            "joongang.co.kr": "중앙일보",
            "hani.co.kr": "한겨레",
            "khan.co.kr": "경향신문",
            "mt.co.kr": "머니투데이",
            "mtn.co.kr": "MTN",
            "edaily.co.kr": "이데일리",
            "newsis.com": "뉴시스",
            "news1.kr": "뉴스1",
            "sedaily.com": "서울경제",
            "fnnews.com": "파이낸셜뉴스",
            "asiae.co.kr": "아시아경제",
            "heraldcorp.com": "헤럴드경제",
            "etnews.com": "전자신문",
            "infomax.co.kr": "연합인포맥스",
            "etoday.co.kr": "이투데이",
            "bizwatch.co.kr": "비즈워치",
            "daum.net": "다음",
            "naver.com": "네이버",
        }

        url_lower = url.lower()
        for domain, name in source_map.items():
            if domain in url_lower:
                return name

        return "기타"


# 하위 호환성을 위한 별칭
NewsScraper = GoogleNewsScraper
