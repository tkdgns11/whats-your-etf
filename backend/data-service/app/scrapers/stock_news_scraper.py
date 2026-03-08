"""네이버 증권 종목뉴스 스크래퍼

네이버 증권에서 종목별 뉴스를 크롤링합니다.
- 네이버가 이미 뉴스-종목 매핑을 해놓음
- 100% 본문 추출 가능 (n.news.naver.com)
- LLM 분석 불필요
"""
import re
import random
import logging
import asyncio
from datetime import datetime
from typing import Optional, List, Dict, Any
from dataclasses import dataclass

import httpx
from bs4 import BeautifulSoup
from sqlalchemy.orm import Session

from app.models.news import NewsArticle
from app.models.news_stock import NewsStockMapping
from app.models.company import CompanyInfo

logger = logging.getLogger(__name__)


@dataclass
class ScrapedNews:
    """크롤링된 뉴스 데이터"""
    title: str
    content: str
    source: str
    source_url: str
    thumbnail_url: Optional[str]
    published_at: Optional[datetime]


class StockNewsScraper:
    """
    네이버 증권 종목뉴스 스크래퍼

    Flow:
    1. 종목 메인 페이지에서 뉴스 링크 추출
    2. news_read.naver → redirect URL 추출
    3. n.news.naver.com에서 본문 추출
    """

    BASE_URL = "https://finance.naver.com"
    HEADERS = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
        "Accept-Language": "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7",
    }

    def __init__(self, db: Session):
        self.db = db
        self.client: Optional[httpx.AsyncClient] = None

    async def __aenter__(self):
        self.client = httpx.AsyncClient(headers=self.HEADERS, timeout=30.0)
        return self

    async def __aexit__(self, *args):
        if self.client:
            await self.client.aclose()

    async def _get_with_retry(self, url: str, max_retries: int = 3) -> Optional[str]:
        """재시도 로직이 포함된 GET 요청"""
        for attempt in range(max_retries):
            try:
                await asyncio.sleep(random.uniform(0.5, 1.5))
                response = await self.client.get(url)
                response.raise_for_status()
                return response.text
            except Exception as e:
                logger.warning(f"요청 실패 (시도 {attempt + 1}/{max_retries}): {url} - {e}")
                if attempt < max_retries - 1:
                    await asyncio.sleep(2 * (attempt + 1))
        return None

    async def get_news_links(self, stock_code: str) -> List[str]:
        """종목 메인 페이지에서 뉴스 링크 추출"""
        url = f"{self.BASE_URL}/item/main.naver?code={stock_code}"
        html = await self._get_with_retry(url)

        if not html:
            return []

        soup = BeautifulSoup(html, "lxml")
        links = []

        # news_read.naver 링크 추출
        for a_tag in soup.select("a[href*='news_read.naver']"):
            href = a_tag.get("href", "")
            if href and href not in links:
                full_url = self.BASE_URL + href if href.startswith("/") else href
                links.append(full_url)

        return links

    async def extract_redirect_url(self, news_read_url: str) -> Optional[str]:
        """news_read.naver에서 실제 뉴스 URL 추출"""
        html = await self._get_with_retry(news_read_url)

        if not html:
            return None

        # script에서 redirect URL 추출
        match = re.search(r"top\.location\.href='([^']+)'", html)
        if match:
            return match.group(1)

        return None

    async def scrape_article(self, news_url: str) -> Optional[ScrapedNews]:
        """n.news.naver.com에서 기사 본문 추출"""
        html = await self._get_with_retry(news_url)

        if not html:
            return None

        soup = BeautifulSoup(html, "lxml")

        # 제목
        title_el = soup.select_one("h2#title_area span") or soup.select_one("h2.media_end_head_headline")
        title = title_el.get_text(strip=True) if title_el else ""

        if not title:
            logger.warning(f"제목 추출 실패: {news_url}")
            return None

        # 본문
        content_el = soup.select_one("#dic_area") or soup.select_one("#newsct_article")
        content = content_el.get_text(strip=True) if content_el else ""

        # 언론사
        source_el = soup.select_one("a.media_end_head_top_logo img")
        source = source_el.get("alt", "") if source_el else ""
        if not source:
            source_el = soup.select_one(".media_end_head_top_logo_text")
            source = source_el.get_text(strip=True) if source_el else "네이버뉴스"

        # 썸네일
        thumb_el = soup.select_one("meta[property='og:image']")
        thumbnail_url = thumb_el.get("content") if thumb_el else None

        # 발행일
        time_el = soup.select_one("span.media_end_head_info_datestamp_time")
        published_at = None
        if time_el:
            date_str = time_el.get("data-date-time") or time_el.get_text(strip=True)
            try:
                published_at = datetime.fromisoformat(date_str.replace(".", "-").replace(" ", "T")[:19])
            except:
                pass

        return ScrapedNews(
            title=title,
            content=content,
            source=source,
            source_url=news_url,
            thumbnail_url=thumbnail_url,
            published_at=published_at
        )

    def _get_category_from_industry(self, industry_group: Optional[str]) -> str:
        """산업 그룹에서 뉴스 카테고리 추출"""
        mapping = {
            "IT_SEMI": "NEWS_SEMI",
            "IT_SW": "NEWS_IT",
            "IT_HW": "NEWS_IT",
            "BIO": "NEWS_BIO",
            "AUTO": "NEWS_AUTO",
            "CHEM": "NEWS_CHEM",
            "ENERGY": "NEWS_ENERGY",
            "FINANCE": "NEWS_FINANCE",
            "CONSTRUCT": "NEWS_CONSTRUCT",
            "CONSUMER": "NEWS_CONSUMER",
            "TELECOM": "NEWS_TELECOM",
            "TRANSPORT": "NEWS_TRANSPORT",
            "INDUSTRY": "NEWS_INDUSTRY",
        }
        return mapping.get(industry_group, "NEWS_ETC")

    async def scrape_stock_news(
        self,
        stock_code: str,
        max_articles: int = 10
    ) -> Dict[str, int]:
        """
        특정 종목의 뉴스 크롤링 및 저장

        Args:
            stock_code: 종목 코드 (6자리)
            max_articles: 최대 기사 수

        Returns:
            {"total": int, "new": int, "mapped": int}
        """
        # 회사 정보 조회
        company = self.db.query(CompanyInfo).filter(
            CompanyInfo.stock_code == stock_code,
            CompanyInfo.is_active == True
        ).first()

        if not company:
            logger.warning(f"회사 정보 없음: {stock_code}")
            return {"total": 0, "new": 0, "mapped": 0}

        category = self._get_category_from_industry(company.industry_group)
        stats = {"total": 0, "new": 0, "mapped": 0}

        # 뉴스 링크 추출 (메인 페이지에서)
        news_links = await self.get_news_links(stock_code)

        if not news_links:
            logger.info(f"뉴스 없음: {stock_code}")
            return stats

        for link in news_links[:max_articles]:
            stats["total"] += 1

            # redirect URL 추출
            real_url = await self.extract_redirect_url(link)
            if not real_url:
                continue

            # 중복 체크
            existing = self.db.query(NewsArticle).filter(
                NewsArticle.source_url == real_url
            ).first()

            if existing:
                # 이미 있는 뉴스면 매핑만 추가
                self._add_stock_mapping(existing.id, company.id)
                stats["mapped"] += 1
                continue

            # 기사 크롤링
            article = await self.scrape_article(real_url)
            if not article:
                continue

            # DB 저장
            news = NewsArticle(
                title=article.title,
                content=article.content,
                source=article.source,
                source_url=article.source_url,
                thumbnail_url=article.thumbnail_url,
                category=category,
                published_at=article.published_at
            )
            self.db.add(news)
            self.db.flush()  # ID 생성

            # 종목 매핑
            self._add_stock_mapping(news.id, company.id)

            stats["new"] += 1
            logger.info(f"뉴스 저장: [{stock_code}] {article.title[:30]}...")

        self.db.commit()
        logger.info(f"종목뉴스 크롤링 완료: {stock_code} - 총 {stats['total']}건, 신규 {stats['new']}건")

        return stats

    def _add_stock_mapping(self, news_id: int, company_id: int):
        """뉴스-종목 매핑 추가 (중복 무시)"""
        existing = self.db.query(NewsStockMapping).filter(
            NewsStockMapping.news_id == news_id,
            NewsStockMapping.company_id == company_id
        ).first()

        if not existing:
            mapping = NewsStockMapping(
                news_id=news_id,
                company_id=company_id
            )
            self.db.add(mapping)

    async def scrape_etf_related_news(
        self,
        etf_id: int,
        top_n: int = 10,
        max_articles_per_stock: int = 5
    ) -> Dict[str, int]:
        """
        ETF 구성종목들의 뉴스 크롤링

        Args:
            etf_id: ETF ID
            top_n: 상위 N개 종목만 크롤링
            max_articles_per_stock: 종목당 최대 기사 수
        """
        from app.models.etf import ETFComposition

        # ETF 상위 구성종목 조회
        compositions = self.db.query(ETFComposition).filter(
            ETFComposition.etf_id == etf_id,
            ETFComposition.company_id != None
        ).order_by(ETFComposition.weight_pct.desc()).limit(top_n).all()

        total_stats = {"total": 0, "new": 0, "mapped": 0}

        for comp in compositions:
            company = self.db.query(CompanyInfo).filter(
                CompanyInfo.id == comp.company_id
            ).first()

            if not company:
                continue

            stats = await self.scrape_stock_news(
                stock_code=company.stock_code,
                max_pages=1,
                max_articles=max_articles_per_stock
            )

            total_stats["total"] += stats["total"]
            total_stats["new"] += stats["new"]
            total_stats["mapped"] += stats["mapped"]

            await asyncio.sleep(1)  # Rate limit

        return total_stats


# 스케줄러/API용 함수
async def scrape_stock_news(db: Session, stock_code: str, max_articles: int = 10) -> Dict[str, int]:
    """단일 종목 뉴스 크롤링"""
    async with StockNewsScraper(db) as scraper:
        return await scraper.scrape_stock_news(stock_code, max_articles=max_articles)


async def scrape_etf_news(db: Session, etf_id: int, top_n: int = 10) -> Dict[str, int]:
    """ETF 구성종목 뉴스 크롤링"""
    async with StockNewsScraper(db) as scraper:
        return await scraper.scrape_etf_related_news(etf_id, top_n=top_n)
