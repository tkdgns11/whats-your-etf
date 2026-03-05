"""뉴스 분석 서비스 - LLM 기반"""
import logging
import asyncio
from datetime import datetime, timedelta
from typing import Optional, List
from sqlalchemy.orm import Session
from sqlalchemy import func

from app.models.news import NewsArticle
from app.models.news_industry import NewsIndustryInfluence
from app.services.llm_service import LLMService
from app.config import get_settings

logger = logging.getLogger(__name__)
settings = get_settings()


class NewsAnalyzer:
    """
    뉴스 분석 서비스

    - LLM을 사용하여 뉴스 기사 분석
    - keywords, content_summary 추출
    - industry_influence 산업 영향력 매핑
    """

    PROMPT_NAME = "news_analysis"  # DB에서 불러올 프롬프트 이름

    def __init__(self, db: Session):
        self.db = db
        self.llm = LLMService(db)

    async def close(self):
        await self.llm.close()

    def _build_user_message(self, news: NewsArticle) -> str:
        """뉴스 정보를 프롬프트 입력 형식으로 변환"""
        content = news.content or ""
        if len(content) > 2000:
            content = content[:2000] + "..."

        return f"""[뉴스 정보]
제목: {news.title}
본문: {content}
출처: {news.source}
발행일: {news.published_at}"""

    async def analyze_news(self, news: NewsArticle) -> bool:
        """
        단일 뉴스 기사 분석

        Args:
            news: 분석할 NewsArticle

        Returns:
            성공 여부
        """
        if not self.llm.is_configured():
            logger.warning("OpenAI API 키가 설정되지 않았습니다.")
            return False

        # 본문이 너무 짧으면 분석 스킵
        content_len = len(news.content) if news.content else 0
        if content_len < 100:
            logger.debug(f"본문이 너무 짧아 분석 스킵: {news.id} ({content_len}자)")
            return False

        user_message = self._build_user_message(news)

        try:
            result = await self.llm.analyze_with_prompt(self.PROMPT_NAME, user_message)
            if not result:
                logger.error(f"뉴스 분석 실패: {news.id}")
                return False

            # 결과 저장
            self._save_analysis_result(news, result)
            return True

        except Exception as e:
            logger.error(f"뉴스 분석 중 오류 [{news.id}]: {e}")
            return False

    def _save_analysis_result(self, news: NewsArticle, result: dict):
        """
        분석 결과를 DB에 저장

        Args:
            news: NewsArticle 객체
            result: LLM 분석 결과
                {
                    "keywords": ["키워드1", "키워드2", ...],
                    "content_summary": {"bullets": ["...", "...", "..."]},
                    "industry_influence": [
                        {"group_code": "IT_SEMI", "relevance": 0.85, "sentiment": "POSITIVE"},
                        ...
                    ]
                }
        """
        # 1. keywords 저장
        keywords = result.get("keywords", [])
        if keywords:
            news.keywords = keywords
            logger.debug(f"Keywords 저장: {keywords}")

        # 2. content_summary 저장
        content_summary = result.get("content_summary")
        if content_summary:
            news.content_summary = content_summary
            logger.debug(f"Summary 저장: {content_summary}")

        # 3. industry_influence 저장
        industry_influences = result.get("industry_influence", [])
        for influence in industry_influences:
            group_code = influence.get("group_code")
            relevance = influence.get("relevance", 0)
            sentiment = influence.get("sentiment", "NEUTRAL")

            # relevance 0.3 미만은 저장하지 않음
            if relevance < 0.3:
                continue

            # 중복 체크 후 저장
            existing = self.db.query(NewsIndustryInfluence).filter(
                NewsIndustryInfluence.news_id == news.id,
                NewsIndustryInfluence.industry_code == group_code
            ).first()

            if not existing:
                news_industry = NewsIndustryInfluence(
                    news_id=news.id,
                    industry_code=group_code,
                    relevance_score=relevance,
                    sentiment=sentiment
                )
                self.db.add(news_industry)
                logger.debug(f"Industry 영향력 저장: {group_code} ({relevance}, {sentiment})")

        self.db.commit()
        logger.info(f"뉴스 분석 완료: {news.id} - {news.title[:30]}...")

    async def analyze_unprocessed_news(self, limit: int = 20) -> dict:
        """
        미분석 뉴스 일괄 분석

        Args:
            limit: 최대 분석 건수

        Returns:
            {"total": int, "success": int, "failed": int}
        """
        # keywords가 없는 뉴스 조회 (미분석)
        news_list = self.db.query(NewsArticle).filter(
            NewsArticle.keywords == None,
            NewsArticle.content != None,
            func.length(NewsArticle.content) >= 100
        ).order_by(NewsArticle.created_at.desc()).limit(limit).all()

        result = {"total": len(news_list), "success": 0, "failed": 0}

        for news in news_list:
            try:
                success = await self.analyze_news(news)
                if success:
                    result["success"] += 1
                else:
                    result["failed"] += 1
                await asyncio.sleep(0.5)  # Rate limit
            except Exception as e:
                logger.error(f"분석 실패 [{news.id}]: {e}")
                result["failed"] += 1
                continue

        logger.info(
            f"뉴스 분석 완료: 총 {result['total']}건 중 "
            f"성공 {result['success']}건, 실패 {result['failed']}건"
        )
        return result

    async def analyze_recent_news(self, hours: int = 1, limit: int = 50) -> dict:
        """
        최근 N시간 내 수집된 미분석 뉴스 분석

        Args:
            hours: 조회 시간 범위
            limit: 최대 분석 건수

        Returns:
            {"total": int, "success": int, "failed": int}
        """
        cutoff = datetime.now() - timedelta(hours=hours)

        news_list = self.db.query(NewsArticle).filter(
            NewsArticle.created_at >= cutoff,
            NewsArticle.keywords == None,
            NewsArticle.content != None,
            func.length(NewsArticle.content) >= 100
        ).order_by(NewsArticle.created_at.desc()).limit(limit).all()

        result = {"total": len(news_list), "success": 0, "failed": 0}

        for news in news_list:
            try:
                success = await self.analyze_news(news)
                if success:
                    result["success"] += 1
                else:
                    result["failed"] += 1
                await asyncio.sleep(0.5)
            except Exception as e:
                logger.error(f"분석 실패 [{news.id}]: {e}")
                result["failed"] += 1
                continue

        logger.info(
            f"최근 {hours}시간 뉴스 분석: 총 {result['total']}건 중 "
            f"성공 {result['success']}건"
        )
        return result


# 스케줄러에서 사용할 함수
async def scheduled_news_analysis(db: Session) -> dict:
    """스케줄러에서 호출하는 뉴스 분석 함수"""
    analyzer = NewsAnalyzer(db)
    try:
        return await analyzer.analyze_recent_news(hours=1, limit=30)
    finally:
        await analyzer.close()
