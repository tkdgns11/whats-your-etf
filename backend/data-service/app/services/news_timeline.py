"""뉴스 타임라인 서비스 - ETF 영향력 타임라인 텍스트 생성"""
import logging
import asyncio
from typing import Optional, List, Dict, Any
from decimal import Decimal
from sqlalchemy.orm import Session
from sqlalchemy import func

from app.models.news import NewsArticle
from app.models.news_etf import NewsETFInfluence
from app.services.llm_service import LLMService
from app.config import get_settings

logger = logging.getLogger(__name__)
settings = get_settings()


class NewsTimelineService:
    """
    뉴스 타임라인 서비스

    - 뉴스-ETF 영향력 매핑에 타임라인 텍스트 생성
    - timeline_title, timeline_summary 생성
    """

    PROMPT_NAME = "news_timeline"

    def __init__(self, db: Session):
        self.db = db
        self.llm = LLMService(db)

    async def close(self):
        await self.llm.close()

    def _build_user_message(
        self,
        news: NewsArticle,
        etf_name: str,
        etf_sector: str,
        actual_change_rate: Optional[Decimal],
        sentiment: str
    ) -> str:
        """타임라인 생성용 프롬프트 입력 생성"""
        # content_summary에서 bullets 추출
        summary_text = ""
        if news.content_summary:
            bullets = news.content_summary.get("bullets", [])
            summary_text = " ".join(bullets)

        change_rate_str = f"{actual_change_rate}%" if actual_change_rate else "N/A"

        return f"""[뉴스 정보]
제목: {news.title}
요약: {summary_text}

[ETF 정보]
ETF명: {etf_name}
섹터: {etf_sector}

[실제 영향]
ETF 변동률: {change_rate_str}
감성: {sentiment}"""

    async def generate_timeline_text(
        self,
        news_etf_influence: NewsETFInfluence,
        news: NewsArticle,
        etf_name: str,
        etf_sector: str
    ) -> bool:
        """
        뉴스-ETF 영향력 레코드에 타임라인 텍스트 생성

        Args:
            news_etf_influence: NewsETFInfluence 객체
            news: NewsArticle 객체
            etf_name: ETF 이름
            etf_sector: ETF 섹터

        Returns:
            성공 여부
        """
        if not self.llm.is_configured():
            logger.warning("OpenAI API 키가 설정되지 않았습니다.")
            return False

        user_message = self._build_user_message(
            news=news,
            etf_name=etf_name,
            etf_sector=etf_sector,
            actual_change_rate=news_etf_influence.actual_change_rate,
            sentiment=news_etf_influence.influence_type or "NEUTRAL"
        )

        try:
            result = await self.llm.analyze_with_prompt(self.PROMPT_NAME, user_message)
            if not result:
                logger.error(f"타임라인 텍스트 생성 실패: news_etf_id={news_etf_influence.id}")
                return False

            # 결과 저장
            news_etf_influence.timeline_title = result.get("timeline_title")
            news_etf_influence.timeline_summary = result.get("timeline_summary")

            self.db.commit()
            logger.info(
                f"타임라인 텍스트 생성 완료: {news_etf_influence.timeline_title}"
            )
            return True

        except Exception as e:
            logger.error(f"타임라인 생성 중 오류: {e}")
            self.db.rollback()
            return False

    async def generate_timeline_for_influence(
        self,
        news_etf_id: int,
        etf_info: Dict[str, str]
    ) -> bool:
        """
        특정 news_etf_influence ID에 대해 타임라인 생성

        Args:
            news_etf_id: NewsETFInfluence ID
            etf_info: {"name": "ETF명", "sector": "섹터"}
        """
        influence = self.db.query(NewsETFInfluence).filter(
            NewsETFInfluence.id == news_etf_id
        ).first()

        if not influence:
            logger.error(f"NewsETFInfluence not found: {news_etf_id}")
            return False

        news = self.db.query(NewsArticle).filter(
            NewsArticle.id == influence.news_id
        ).first()

        if not news:
            logger.error(f"NewsArticle not found: {influence.news_id}")
            return False

        return await self.generate_timeline_text(
            news_etf_influence=influence,
            news=news,
            etf_name=etf_info.get("name", ""),
            etf_sector=etf_info.get("sector", "")
        )

    async def batch_generate_timelines(
        self,
        etf_info_map: Dict[int, Dict[str, str]],
        limit: int = 20
    ) -> Dict[str, int]:
        """
        타임라인 텍스트가 없는 영향력 레코드들에 대해 일괄 생성

        Args:
            etf_info_map: {etf_id: {"name": "ETF명", "sector": "섹터"}}
            limit: 최대 처리 건수

        Returns:
            {"total": int, "success": int, "failed": int}
        """
        # timeline_title이 없는 레코드 조회
        influences = self.db.query(NewsETFInfluence).filter(
            NewsETFInfluence.timeline_title == None
        ).limit(limit).all()

        result = {"total": len(influences), "success": 0, "failed": 0}

        for influence in influences:
            etf_info = etf_info_map.get(influence.etf_id, {})
            if not etf_info:
                logger.warning(f"ETF info not found: etf_id={influence.etf_id}")
                result["failed"] += 1
                continue

            news = self.db.query(NewsArticle).filter(
                NewsArticle.id == influence.news_id
            ).first()

            if not news:
                result["failed"] += 1
                continue

            success = await self.generate_timeline_text(
                news_etf_influence=influence,
                news=news,
                etf_name=etf_info.get("name", ""),
                etf_sector=etf_info.get("sector", "")
            )

            if success:
                result["success"] += 1
            else:
                result["failed"] += 1

            await asyncio.sleep(0.5)  # Rate limit

        logger.info(
            f"타임라인 일괄 생성 완료: 총 {result['total']}건 중 "
            f"성공 {result['success']}건"
        )
        return result


# 스케줄러/API에서 사용할 함수
async def generate_news_timeline(
    db: Session,
    news_etf_id: int,
    etf_info: Dict[str, str]
) -> bool:
    """단일 뉴스-ETF 영향력에 타임라인 텍스트 생성"""
    service = NewsTimelineService(db)
    try:
        return await service.generate_timeline_for_influence(news_etf_id, etf_info)
    finally:
        await service.close()
