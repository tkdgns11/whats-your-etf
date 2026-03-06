"""
GPT-4o 뉴스 영향 분석 서비스

All-LLM 방식: GPT-4o가 회사/산업 목록에서 관련 항목 선택 (Constrained Selection)
- Hallucination 방지: DB 목록에서만 선택 가능
- 1:N 매핑: 하나의 뉴스가 여러 회사/산업에 영향
- 영향도: -1.0 (매우 부정) ~ +1.0 (매우 긍정)
"""
import json
import logging
import asyncio
from datetime import datetime, timedelta
from typing import Optional, List, Dict, Any
from dataclasses import dataclass
from sqlalchemy.orm import Session
from sqlalchemy import func, text

from app.models.news import NewsArticle
from app.models.news_impact import NewsImpact
from app.models.company import CompanyInfo, IndustryClassification
from app.services.llm_service import LLMService
from app.config import get_settings

logger = logging.getLogger(__name__)
settings = get_settings()


@dataclass
class AnalysisResult:
    """분석 결과"""
    success: bool
    impacts: List[Dict] = None
    summary: List[str] = None
    keywords: List[str] = None
    error: str = None


# 시스템 프롬프트 (Constrained Selection)
SYSTEM_PROMPT = """당신은 금융 뉴스 분석 전문가입니다.
뉴스 기사를 분석하여 관련된 회사와 산업을 아래 목록에서만 선택하고, 각각의 영향도를 평가합니다.

## 규칙
1. **반드시 제공된 목록에서만 선택** (목록에 없으면 선택 불가)
2. 직접 언급된 것 + 간접 영향 받는 것 모두 포함
3. 영향도 점수: -1.0(매우 부정) ~ +1.0(매우 긍정)
4. 관련 없으면 빈 배열 반환
5. 요약은 3개 이내 bullet point로

## 출력 형식 (JSON)
{
  "impacts": [
    {"target": "삼성전자", "type": "company", "score": 0.7, "reason": "AI 반도체 투자 확대"},
    {"target": "IT_SEMI", "type": "industry", "score": 0.5, "reason": "반도체 업황 개선 기대"}
  ],
  "summary": [
    "삼성전자가 AI 반도체에 10조원 투자 발표",
    "2025년까지 생산능력 2배 확대 계획"
  ],
  "keywords": ["AI반도체", "삼성전자", "투자확대"]
}"""


class NewsImpactAnalyzer:
    """
    GPT-4o 기반 뉴스 영향 분석기

    Constrained LLM 방식:
    - DB에서 회사/산업 목록 로드
    - GPT-4o에 목록 전달하여 "선택"만 하도록 유도
    - news_impact 테이블에 1:N 결과 저장
    """

    def __init__(self, db: Session):
        self.db = db
        self.llm = LLMService(db)
        self._companies_cache = None
        self._industries_cache = None

    async def close(self):
        await self.llm.close()

    def _load_companies(self) -> List[Dict]:
        """회사 목록 로드 (캐싱)"""
        if self._companies_cache is None:
            companies = self.db.query(CompanyInfo).filter(
                CompanyInfo.is_active == True
            ).all()

            self._companies_cache = [
                {"id": c.id, "name": c.stock_name, "code": c.stock_code, "group": c.industry_group}
                for c in companies
            ]
            logger.debug(f"회사 목록 로드: {len(self._companies_cache)}개")

        return self._companies_cache

    def _load_industries(self) -> List[Dict]:
        """산업 목록 로드 (캐싱)"""
        if self._industries_cache is None:
            industries = self.db.query(IndustryClassification).filter(
                IndustryClassification.group_code != None
            ).all()

            # group_code 기준 중복 제거
            seen = set()
            self._industries_cache = []
            for ind in industries:
                if ind.group_code not in seen:
                    seen.add(ind.group_code)
                    self._industries_cache.append({
                        "code": ind.group_code,
                        "name": ind.group_name or ind.name
                    })

            logger.debug(f"산업 목록 로드: {len(self._industries_cache)}개")

        return self._industries_cache

    def _build_user_message(self, news: NewsArticle) -> str:
        """프롬프트 입력 메시지 생성"""
        companies = self._load_companies()
        industries = self._load_industries()

        # 본문 (없으면 description 사용)
        content = news.content or ""
        if len(content) > 1000:
            content = content[:1000] + "..."

        # 회사 목록 포맷
        company_list = "\n".join([
            f"- {c['name']} ({c['code']}, {c['group'] or '기타'})"
            for c in companies[:100]  # 상위 100개만
        ])

        # 산업 목록 포맷
        industry_list = "\n".join([
            f"- {ind['code']}: {ind['name']}"
            for ind in industries
        ])

        return f"""[뉴스]
제목: {news.title}
본문: {content}
출처: {news.source}
발행일: {news.published_at}

[회사 목록] - 이 중에서만 선택
{company_list}

[산업 목록] - 이 중에서만 선택 (group_code 사용)
{industry_list}

위 뉴스를 분석하여 관련 회사/산업과 영향도를 JSON으로 출력하세요."""

    async def analyze_news(self, news: NewsArticle) -> AnalysisResult:
        """
        단일 뉴스 분석

        Returns:
            AnalysisResult: 분석 결과 (impacts, summary, keywords)
        """
        if not self.llm.is_configured():
            return AnalysisResult(success=False, error="OpenAI API 키가 설정되지 않았습니다.")

        user_message = self._build_user_message(news)

        try:
            response = await self.llm.call_json(SYSTEM_PROMPT, user_message)
            if not response:
                return AnalysisResult(success=False, error="LLM 응답 없음")

            impacts = response.get("impacts", [])
            summary = response.get("summary", [])
            keywords = response.get("keywords", [])

            return AnalysisResult(
                success=True,
                impacts=impacts,
                summary=summary,
                keywords=keywords
            )

        except Exception as e:
            logger.error(f"뉴스 분석 실패 [{news.id}]: {e}")
            return AnalysisResult(success=False, error=str(e))

    def _save_impacts(self, news: NewsArticle, result: AnalysisResult):
        """분석 결과를 DB에 저장"""
        companies = {c["name"]: c["id"] for c in self._load_companies()}
        industries = {ind["code"]: ind["code"] for ind in self._load_industries()}

        saved_count = 0

        for impact in result.impacts or []:
            target = impact.get("target")
            target_type = impact.get("type", "").upper()
            score = impact.get("score", 0)
            reason = impact.get("reason", "")

            # score 범위 검증
            score = max(-1.0, min(1.0, float(score)))

            # 타입별 처리
            if target_type == "COMPANY" and target in companies:
                news_impact = NewsImpact(
                    news_id=news.id,
                    target_type="COMPANY",
                    company_id=companies[target],
                    impact_score=score,
                    impact_reason=reason[:200] if reason else None
                )
                self.db.add(news_impact)
                saved_count += 1
                logger.debug(f"회사 영향 저장: {target} ({score:+.2f})")

            elif target_type == "INDUSTRY" and target in industries:
                news_impact = NewsImpact(
                    news_id=news.id,
                    target_type="INDUSTRY",
                    industry_code=target,
                    impact_score=score,
                    impact_reason=reason[:200] if reason else None
                )
                self.db.add(news_impact)
                saved_count += 1
                logger.debug(f"산업 영향 저장: {target} ({score:+.2f})")

            else:
                logger.warning(f"알 수 없는 타겟: {target} ({target_type})")

        # keywords, summary 저장
        if result.keywords:
            news.keywords = result.keywords

        if result.summary:
            news.content_summary = {"bullets": result.summary}

        self.db.commit()
        logger.info(f"뉴스 분석 저장 완료: {news.id} - {saved_count}건 영향")
        return saved_count

    async def analyze_and_save(self, news: NewsArticle) -> bool:
        """뉴스 분석 후 DB 저장"""
        result = await self.analyze_news(news)

        if not result.success:
            logger.error(f"분석 실패 [{news.id}]: {result.error}")
            return False

        self._save_impacts(news, result)
        return True

    async def analyze_unprocessed(self, limit: int = 20) -> Dict[str, int]:
        """
        미분석 뉴스 일괄 처리

        Returns:
            {"total": int, "success": int, "failed": int, "no_impact": int}
        """
        # news_impact가 없는 뉴스 조회
        news_list = self.db.query(NewsArticle).filter(
            ~NewsArticle.id.in_(
                self.db.query(NewsImpact.news_id).distinct()
            )
        ).order_by(NewsArticle.created_at.desc()).limit(limit).all()

        stats = {"total": len(news_list), "success": 0, "failed": 0, "no_impact": 0}

        for news in news_list:
            try:
                result = await self.analyze_news(news)

                if not result.success:
                    stats["failed"] += 1
                    continue

                if not result.impacts:
                    stats["no_impact"] += 1
                    # keywords/summary만 저장
                    if result.keywords:
                        news.keywords = result.keywords
                    if result.summary:
                        news.content_summary = {"bullets": result.summary}
                    self.db.commit()
                else:
                    self._save_impacts(news, result)
                    stats["success"] += 1

                await asyncio.sleep(0.5)  # Rate limit

            except Exception as e:
                logger.error(f"분석 오류 [{news.id}]: {e}")
                stats["failed"] += 1
                continue

        logger.info(
            f"뉴스 분석 완료: 총 {stats['total']}건 - "
            f"성공:{stats['success']} 영향없음:{stats['no_impact']} 실패:{stats['failed']}"
        )
        return stats

    async def analyze_recent(self, hours: int = 1, limit: int = 50) -> Dict[str, int]:
        """최근 N시간 내 뉴스 분석"""
        cutoff = datetime.now() - timedelta(hours=hours)

        news_list = self.db.query(NewsArticle).filter(
            NewsArticle.created_at >= cutoff,
            ~NewsArticle.id.in_(
                self.db.query(NewsImpact.news_id).distinct()
            )
        ).order_by(NewsArticle.created_at.desc()).limit(limit).all()

        stats = {"total": len(news_list), "success": 0, "failed": 0, "no_impact": 0}

        for news in news_list:
            try:
                result = await self.analyze_news(news)

                if not result.success:
                    stats["failed"] += 1
                    continue

                if not result.impacts:
                    stats["no_impact"] += 1
                else:
                    self._save_impacts(news, result)
                    stats["success"] += 1

                await asyncio.sleep(0.5)

            except Exception as e:
                logger.error(f"분석 오류 [{news.id}]: {e}")
                stats["failed"] += 1
                continue

        logger.info(f"최근 {hours}시간 뉴스 분석: {stats}")
        return stats


# 스케줄러용 함수
async def scheduled_news_impact_analysis(db: Session) -> Dict[str, int]:
    """스케줄러에서 호출하는 뉴스 영향 분석"""
    analyzer = NewsImpactAnalyzer(db)
    try:
        return await analyzer.analyze_recent(hours=1, limit=30)
    finally:
        await analyzer.close()
