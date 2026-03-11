"""뉴스 AI 분석 서비스

뉴스 기사 분석:
1. AI 요약 (3줄 bullet points)
2. 키워드 추출
3. 관련 ETF 추천
"""
import logging
from typing import Optional, List, Dict, Any
from dataclasses import dataclass
from decimal import Decimal

from sqlalchemy import text
from sqlalchemy.orm import Session

from app.models.news import NewsArticle
from app.models.news_etf import NewsETFInfluence
from app.services.llm_service import LLMService

logger = logging.getLogger(__name__)


@dataclass
class NewsAnalysisResult:
    """뉴스 분석 결과"""
    summary: List[str]  # 3줄 요약
    keywords: List[str]  # 키워드 목록
    sentiment: str  # POSITIVE, NEGATIVE, NEUTRAL
    industries: List[Dict[str, str]]  # [{"code": "IT_SEMI", "impact": "POSITIVE"}]


@dataclass
class ETFRecommendation:
    """ETF 추천 결과"""
    etf_id: int
    stock_code: str
    name: str
    influence_score: float
    influence_type: str  # POSITIVE, NEGATIVE, NEUTRAL
    reason: str


class NewsAnalyzer:
    """뉴스 AI 분석기"""

    def __init__(self, db: Session):
        self.db = db
        # 뉴스 분석은 Haiku(저렴한 모델) 사용
        self.llm = LLMService(db, use_light_model=True)

    async def close(self):
        await self.llm.close()

    async def analyze_article(self, article: NewsArticle) -> Optional[NewsAnalysisResult]:
        """
        뉴스 기사 AI 분석

        Args:
            article: 분석할 뉴스 기사

        Returns:
            NewsAnalysisResult or None
        """
        if not self.llm.is_configured():
            logger.warning("OpenAI API 키가 설정되지 않았습니다.")
            return None

        # 본문이 너무 길면 앞부분만 사용
        content = article.content or ""
        if len(content) > 3000:
            content = content[:3000] + "..."

        user_message = f"""## 뉴스 제목
{article.title}

## 뉴스 본문
{content}

## 언론사
{article.source or '알 수 없음'}
"""

        result = await self.llm.analyze_with_prompt("news_analysis", user_message)

        if not result:
            return None

        try:
            return NewsAnalysisResult(
                summary=result.get("summary", []),
                keywords=result.get("keywords", []),
                sentiment=result.get("sentiment", "NEUTRAL"),
                industries=result.get("industries", [])
            )
        except Exception as e:
            logger.error(f"분석 결과 파싱 실패: {e}")
            return None

    def recommend_etfs(
        self,
        analysis: NewsAnalysisResult,
        limit: int = 5
    ) -> List[ETFRecommendation]:
        """
        뉴스 분석 결과 기반 ETF 추천

        산업 영향력 기반으로 관련 ETF 추천:
        1. 영향받는 산업(industries)의 ETF 조회
        2. 해당 산업 비중이 높은 ETF 우선 추천
        3. 영향 방향(POSITIVE/NEGATIVE)에 따라 점수 조정
        """
        if not analysis.industries:
            return []

        recommendations = []

        for industry in analysis.industries:
            industry_code = industry.get("code")
            impact = industry.get("impact", "NEUTRAL")

            if not industry_code:
                continue

            # 해당 산업 비중이 높은 ETF 조회
            result = self.db.execute(text("""
                SELECT
                    e.id,
                    e.stock_code,
                    e.name,
                    esc.weight_pct,
                    esc.group_name
                FROM etf e
                JOIN etf_sector_cluster esc ON esc.etf_id = e.id
                WHERE esc.group_code = :industry_code
                  AND e.is_active = true
                ORDER BY esc.weight_pct DESC
                LIMIT 10
            """), {"industry_code": industry_code})

            for row in result:
                # 이미 추천된 ETF 제외
                if any(r.etf_id == row[0] for r in recommendations):
                    continue

                # 영향력 점수 계산 (비중 기반)
                weight = float(row[3]) if row[3] else 0
                score = min(weight / 100, 1.0)  # 0~1 범위로 정규화

                recommendations.append(ETFRecommendation(
                    etf_id=row[0],
                    stock_code=row[1],
                    name=row[2],
                    influence_score=score,
                    influence_type=impact,
                    reason=f"{row[4]} 섹터 비중 {weight:.1f}%"
                ))

        # 점수 높은 순 정렬 후 상위 N개 반환
        recommendations.sort(key=lambda x: x.influence_score, reverse=True)
        return recommendations[:limit]

    async def process_article(
        self,
        article: NewsArticle,
        save_to_db: bool = True
    ) -> Optional[Dict[str, Any]]:
        """
        뉴스 기사 분석 및 ETF 추천 전체 프로세스

        Args:
            article: 분석할 뉴스
            save_to_db: DB에 결과 저장 여부

        Returns:
            {
                "analysis": NewsAnalysisResult,
                "etf_recommendations": List[ETFRecommendation]
            }
        """
        # 1. AI 분석
        analysis = await self.analyze_article(article)
        if not analysis:
            logger.warning(f"뉴스 분석 실패: {article.id}")
            return None

        # 2. ETF 추천
        etf_recs = self.recommend_etfs(analysis)

        # 3. DB 저장
        if save_to_db:
            # 뉴스 기사 업데이트 (요약, 키워드)
            article.content_summary = {"bullets": analysis.summary}
            article.keywords = analysis.keywords
            self.db.add(article)

            # ETF 영향력 저장
            for rec in etf_recs:
                influence = NewsETFInfluence(
                    news_id=article.id,
                    etf_id=rec.etf_id,
                    influence_score=Decimal(str(rec.influence_score)),
                    influence_type=rec.influence_type,
                    timeline_title=article.title[:100] if article.title else "",
                    timeline_summary=analysis.summary[0] if analysis.summary else "",
                    analysis_reason=rec.reason
                )
                self.db.add(influence)

            self.db.commit()
            logger.info(f"뉴스 분석 저장 완료: {article.id} - ETF {len(etf_recs)}개 추천")

        return {
            "analysis": analysis,
            "etf_recommendations": etf_recs
        }


async def analyze_news(db: Session, news_id: int) -> Optional[Dict[str, Any]]:
    """단일 뉴스 분석 (API용)"""
    article = db.query(NewsArticle).filter(NewsArticle.id == news_id).first()
    if not article:
        return None

    analyzer = NewsAnalyzer(db)
    try:
        return await analyzer.process_article(article)
    finally:
        await analyzer.close()


async def analyze_unprocessed_news(db: Session, limit: int = 50) -> int:
    """미분석 뉴스 일괄 처리"""
    # content_summary가 NULL인 뉴스 조회
    articles = db.query(NewsArticle).filter(
        NewsArticle.content_summary == None,
        NewsArticle.content != None,
        NewsArticle.is_active == True
    ).limit(limit).all()

    if not articles:
        logger.info("분석할 뉴스가 없습니다.")
        return 0

    analyzer = NewsAnalyzer(db)
    processed = 0

    try:
        for article in articles:
            result = await analyzer.process_article(article)
            if result:
                processed += 1
                logger.info(f"[{processed}/{len(articles)}] 분석 완료: {article.title[:30]}...")
    finally:
        await analyzer.close()

    return processed
