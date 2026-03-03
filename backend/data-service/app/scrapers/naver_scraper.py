"""Naver News API 기반 뉴스 크롤러"""
import httpx
import logging
import asyncio
from datetime import datetime
from typing import Optional, List
from urllib.parse import quote
from sqlalchemy.orm import Session

from app.models.news import NewsArticle
from app.config import get_settings
from app.utils.dedup import DuplicateChecker

logger = logging.getLogger(__name__)
settings = get_settings()


class NaverNewsScraper:
    """
    Naver News Search API 기반 뉴스 크롤러

    API 문서: https://developers.naver.com/docs/serviceapi/search/news/news.md

    필요 환경변수:
    - NAVER_CLIENT_ID
    - NAVER_CLIENT_SECRET
    """

    BASE_URL = "https://openapi.naver.com/v1/search/news.json"
    USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
    TIMEOUT = 15

    def __init__(self, db: Session):
        self.db = db
        self.client_id = settings.naver_client_id
        self.client_secret = settings.naver_client_secret
        self.client = httpx.AsyncClient(
            headers={
                "User-Agent": self.USER_AGENT,
                "X-Naver-Client-Id": self.client_id,
                "X-Naver-Client-Secret": self.client_secret,
            },
            timeout=self.TIMEOUT
        )
        self.dedup_checker = DuplicateChecker(db, similarity_threshold=0.85)

    async def close(self):
        await self.client.aclose()
        self.dedup_checker.clear_cache()

    def is_configured(self) -> bool:
        """API 키가 설정되어 있는지 확인"""
        return bool(self.client_id and self.client_secret)

    async def scrape_by_keyword(self, keyword: str, display: int = 10) -> int:
        """
        특정 키워드로 뉴스 수집

        Args:
            keyword: 검색 키워드
            display: 검색 결과 개수 (최대 100)

        Returns:
            저장된 뉴스 수
        """
        if not self.is_configured():
            logger.warning("Naver API 키가 설정되지 않았습니다.")
            return 0

        params = {
            "query": keyword,
            "display": min(display, 100),
            "start": 1,
            "sort": "date",  # 최신순
        }

        try:
            response = await self.client.get(self.BASE_URL, params=params)
            response.raise_for_status()
            data = response.json()
        except httpx.HTTPError as e:
            logger.error(f"Naver API 요청 실패 [{keyword}]: {e}")
            return 0

        items = data.get("items", [])
        count = 0

        for item in items:
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

    async def scrape_keywords(self, keywords: List[str], display_per_keyword: int = 5) -> int:
        """
        여러 키워드로 뉴스 수집

        Args:
            keywords: 키워드 리스트
            display_per_keyword: 키워드당 수집 개수

        Returns:
            총 저장된 뉴스 수
        """
        total_count = 0

        for keyword in keywords:
            try:
                count = await self.scrape_by_keyword(keyword, display_per_keyword)
                total_count += count
                logger.info(f"[Naver][{keyword}] {count}건 수집")
                await asyncio.sleep(0.1)  # Rate limit (초당 10건)
            except Exception as e:
                logger.error(f"[Naver][{keyword}] 크롤링 실패: {e}")
                continue

        logger.info(f"[Naver] 총 {total_count}건 수집 완료")
        return total_count

    def _parse_item(self, item: dict, keyword: str) -> Optional[NewsArticle]:
        """API 응답 item을 NewsArticle로 변환"""
        title = item.get("title", "")
        link = item.get("link", "")
        original_link = item.get("originallink", "")
        description = item.get("description", "")
        pub_date = item.get("pubDate", "")

        if not title or not link:
            return None

        # HTML 태그 제거
        title = self._remove_html_tags(title)
        description = self._remove_html_tags(description)

        # 발행일 파싱
        published_at = self._parse_date(pub_date)

        # 실제 기사 URL 사용 (originallink가 있으면 우선)
        source_url = original_link if original_link else link

        # 출처 추출 (URL에서)
        source_name = self._extract_source(source_url)

        return NewsArticle(
            title=title,
            content_summary=description if len(description) > 50 else None,
            source=source_name,
            source_url=source_url,
            category="금융",
            keywords=[keyword],
            published_at=published_at
        )

    def _remove_html_tags(self, text: str) -> str:
        """HTML 태그 및 엔티티 제거"""
        import re
        # HTML 태그 제거
        text = re.sub(r'<[^>]+>', '', text)
        # HTML 엔티티 변환
        text = text.replace("&quot;", '"')
        text = text.replace("&amp;", "&")
        text = text.replace("&lt;", "<")
        text = text.replace("&gt;", ">")
        text = text.replace("&nbsp;", " ")
        text = text.replace("<b>", "").replace("</b>", "")
        return text.strip()

    def _parse_date(self, date_str: str) -> Optional[datetime]:
        """날짜 문자열 파싱"""
        if not date_str:
            return None

        try:
            # Naver API 날짜 형식: "Mon, 03 Mar 2025 10:30:00 +0900"
            from email.utils import parsedate_to_datetime
            return parsedate_to_datetime(date_str)
        except Exception:
            return datetime.now()

    def _extract_source(self, url: str) -> str:
        """URL에서 언론사명 추출"""
        source_map = {
            "hankyung.com": "한국경제",
            "mk.co.kr": "매일경제",
            "yna.co.kr": "연합뉴스",
            "yonhapnews": "연합뉴스",
            "chosun.com": "조선일보",
            "biz.chosun.com": "조선비즈",
            "donga.com": "동아일보",
            "joongang.co.kr": "중앙일보",
            "hani.co.kr": "한겨레",
            "khan.co.kr": "경향신문",
            "mt.co.kr": "머니투데이",
            "edaily.co.kr": "이데일리",
            "newsis.com": "뉴시스",
            "news1.kr": "뉴스1",
            "sedaily.com": "서울경제",
            "fnnews.com": "파이낸셜뉴스",
            "asiae.co.kr": "아시아경제",
            "heraldcorp.com": "헤럴드경제",
            "etnews.com": "전자신문",
            "zdnet.co.kr": "ZDNet Korea",
            "bloter.net": "블로터",
        }

        url_lower = url.lower()
        for domain, name in source_map.items():
            if domain in url_lower:
                return name

        return "기타"
