"""주요 언론사 본문 크롤링"""
import httpx
import logging
import re
from bs4 import BeautifulSoup
from typing import Optional
from urllib.parse import urlparse

logger = logging.getLogger(__name__)


class ContentScraper:
    """
    주요 언론사 기사 본문 추출

    지원 언론사:
    - 한국경제 (hankyung.com)
    - 서울경제 (sedaily.com)
    - 이데일리 (edaily.co.kr)
    - 뉴스1 (news1.kr)
    - 이투데이 (etoday.co.kr)
    - 비즈워치 (bizwatch.co.kr)
    """

    USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
    TIMEOUT = 10

    def __init__(self):
        self.client = httpx.AsyncClient(
            headers={"User-Agent": self.USER_AGENT},
            timeout=self.TIMEOUT,
            follow_redirects=True
        )

    async def close(self):
        await self.client.aclose()

    async def extract_content(self, url: str) -> Optional[str]:
        """
        URL에서 기사 본문 추출

        Returns:
            본문 텍스트 (최대 2000자) 또는 None
        """
        if not url:
            return None

        domain = self._get_domain(url)
        extractor = self._get_extractor(domain)

        if not extractor:
            logger.debug(f"지원하지 않는 언론사: {domain}")
            return None

        try:
            response = await self.client.get(url)
            response.raise_for_status()
            html = response.text
            content = extractor(html)

            if content:
                # 정제 및 길이 제한
                content = self._clean_content(content)
                if len(content) > 2000:
                    content = content[:1997] + "..."

            return content

        except httpx.HTTPError as e:
            logger.error(f"본문 크롤링 실패 [{url}]: {e}")
            return None
        except Exception as e:
            logger.error(f"본문 파싱 실패 [{url}]: {e}")
            return None

    def _get_domain(self, url: str) -> str:
        """URL에서 도메인 추출"""
        parsed = urlparse(url)
        return parsed.netloc.lower()

    def _get_extractor(self, domain: str):
        """도메인에 맞는 추출 함수 반환"""
        extractors = {
            "www.hankyung.com": self._extract_hankyung,
            "hankyung.com": self._extract_hankyung,
            "www.sedaily.com": self._extract_sedaily,
            "sedaily.com": self._extract_sedaily,
            "www.edaily.co.kr": self._extract_edaily,
            "edaily.co.kr": self._extract_edaily,
            "www.news1.kr": self._extract_news1,
            "news1.kr": self._extract_news1,
            "www.etoday.co.kr": self._extract_etoday,
            "etoday.co.kr": self._extract_etoday,
            "www.bizwatch.co.kr": self._extract_bizwatch,
            "bizwatch.co.kr": self._extract_bizwatch,
        }

        for key, extractor in extractors.items():
            if key in domain:
                return extractor

        return None

    def _clean_content(self, content: str) -> str:
        """본문 정제"""
        if not content:
            return ""

        # 연속 공백/줄바꿈 정리
        content = re.sub(r'\s+', ' ', content)
        # 앞뒤 공백 제거
        content = content.strip()
        # 광고/관련기사 문구 제거
        patterns = [
            r'\[.*?기자\]',
            r'\(.*?기자\)',
            r'Copyright.*$',
            r'ⓒ.*$',
            r'무단.*금지.*$',
            r'관련기사.*$',
        ]
        for pattern in patterns:
            content = re.sub(pattern, '', content)

        return content.strip()

    # ==================== 언론사별 추출 함수 ====================

    def _extract_hankyung(self, html: str) -> Optional[str]:
        """한국경제 본문 추출"""
        soup = BeautifulSoup(html, 'html.parser')

        # 기사 본문 영역
        article = soup.find('div', {'id': 'articletxt'})
        if not article:
            article = soup.find('div', class_='article-body')
        if not article:
            article = soup.find('div', class_='txt-body')

        if article:
            # 불필요한 요소 제거
            for tag in article.find_all(['script', 'style', 'iframe', 'figure']):
                tag.decompose()
            return article.get_text(strip=True)

        return None

    def _extract_sedaily(self, html: str) -> Optional[str]:
        """서울경제 본문 추출"""
        soup = BeautifulSoup(html, 'html.parser')

        article = soup.find('div', class_='article_view')
        if not article:
            article = soup.find('div', id='v-article')

        if article:
            for tag in article.find_all(['script', 'style', 'iframe', 'figure']):
                tag.decompose()
            return article.get_text(strip=True)

        return None

    def _extract_edaily(self, html: str) -> Optional[str]:
        """이데일리 본문 추출"""
        soup = BeautifulSoup(html, 'html.parser')

        article = soup.find('div', class_='news_body')
        if not article:
            article = soup.find('div', id='news_body')

        if article:
            for tag in article.find_all(['script', 'style', 'iframe', 'figure']):
                tag.decompose()
            return article.get_text(strip=True)

        return None

    def _extract_news1(self, html: str) -> Optional[str]:
        """뉴스1 본문 추출"""
        soup = BeautifulSoup(html, 'html.parser')

        article = soup.find('div', class_='detail')
        if not article:
            article = soup.find('div', id='articles_detail')
        if not article:
            article = soup.find('article', class_='content')

        if article:
            for tag in article.find_all(['script', 'style', 'iframe', 'figure', 'aside']):
                tag.decompose()
            return article.get_text(strip=True)

        return None

    def _extract_etoday(self, html: str) -> Optional[str]:
        """이투데이 본문 추출"""
        soup = BeautifulSoup(html, 'html.parser')

        article = soup.find('div', class_='articleView')
        if not article:
            article = soup.find('div', id='newsContent')
        if not article:
            article = soup.find('div', class_='news_body_area')

        if article:
            for tag in article.find_all(['script', 'style', 'iframe', 'figure', 'aside']):
                tag.decompose()
            return article.get_text(strip=True)

        return None

    def _extract_bizwatch(self, html: str) -> Optional[str]:
        """비즈워치 본문 추출"""
        soup = BeautifulSoup(html, 'html.parser')

        article = soup.find('div', class_='article-body')
        if not article:
            article = soup.find('div', id='article-body')
        if not article:
            article = soup.find('div', class_='news-content')

        if article:
            for tag in article.find_all(['script', 'style', 'iframe', 'figure', 'aside']):
                tag.decompose()
            return article.get_text(strip=True)

        return None


async def enrich_news_content(news_article, content_scraper: ContentScraper) -> bool:
    """
    뉴스 기사에 본문 추가

    Args:
        news_article: NewsArticle 객체
        content_scraper: ContentScraper 인스턴스

    Returns:
        본문 추출 성공 여부
    """
    if not news_article.source_url:
        return False

    content = await content_scraper.extract_content(news_article.source_url)

    if content and len(content) > 100:  # 의미있는 본문인 경우만
        news_article.content = content  # content 필드에 본문 저장
        return True

    return False
