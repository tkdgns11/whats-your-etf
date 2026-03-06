"""뉴스-ETF 매핑 서비스 (Step 2)

news_impact (GPT-4o 분석 결과) 기반으로
회사/산업 → ETF 매핑 및 영향도 계산
"""
import logging
import asyncio
from datetime import datetime, date, time, timedelta
from typing import Optional, List, Dict, Tuple
from decimal import Decimal
from sqlalchemy.orm import Session
from sqlalchemy import func, and_, or_

from app.models.news import NewsArticle
from app.models.news_impact import NewsImpact
from app.models.news_etf import NewsETFInfluence
from app.models.etf import ETF, ETFSectorCluster, ETFPrice, ETFComposition
from app.models.company import CompanyInfo
from app.services.llm_service import LLMService
from app.config import get_settings

logger = logging.getLogger(__name__)
settings = get_settings()

# 한국 공휴일 (간단 버전, 실제로는 holidays 패키지 사용 권장)
KR_HOLIDAYS_2025 = {
    date(2025, 1, 1),   # 신정
    date(2025, 1, 28),  # 설날
    date(2025, 1, 29),
    date(2025, 1, 30),
    date(2025, 3, 1),   # 삼일절
    date(2025, 5, 5),   # 어린이날
    date(2025, 5, 6),   # 석가탄신일
    date(2025, 6, 6),   # 현충일
    date(2025, 8, 15),  # 광복절
    date(2025, 10, 3),  # 개천절
    date(2025, 10, 6),  # 추석
    date(2025, 10, 7),
    date(2025, 10, 8),
    date(2025, 10, 9),  # 한글날
    date(2025, 12, 25), # 성탄절
}


class NewsETFMapper:
    """
    뉴스-ETF 매핑 서비스 (Step 2)

    - news_impact (GPT-4o 분석) → ETF 매핑
    - 회사 영향 → etf_compositions 통해 ETF 매핑
    - 산업 영향 → etf_sector_cluster 통해 ETF 매핑
    - 영향도 계산 및 검증
    - 타임라인 텍스트 생성
    """

    PROMPT_NAME = "news_timeline"

    def __init__(self, db: Session):
        self.db = db
        self.llm = LLMService(db)

    async def close(self):
        await self.llm.close()

    # ========================================
    # 거래일 계산 유틸리티
    # ========================================

    def is_trading_day(self, d: date) -> bool:
        """거래일 여부 확인"""
        if d.weekday() >= 5:  # 주말
            return False
        if d in KR_HOLIDAYS_2025:
            return False
        return True

    def get_next_trading_day(self, d: date) -> date:
        """다음 거래일 반환"""
        while not self.is_trading_day(d):
            d += timedelta(days=1)
        return d

    def get_prev_trading_day(self, d: date) -> date:
        """이전 거래일 반환"""
        d -= timedelta(days=1)
        while not self.is_trading_day(d):
            d -= timedelta(days=1)
        return d

    def get_verification_trade_dates(self, news_published_at: datetime) -> Tuple[date, date]:
        """
        뉴스 발행 시점 기준 검증에 사용할 거래일 반환

        Returns:
            (trade_date, prev_trade_date): 종가 비교할 두 거래일
        """
        market_close = time(15, 30)

        if news_published_at.time() <= market_close:
            # 장중 발행 → 당일이 기준
            target_date = news_published_at.date()
        else:
            # 장 마감 후 발행 → 다음날이 기준
            target_date = news_published_at.date() + timedelta(days=1)

        trade_date = self.get_next_trading_day(target_date)
        prev_trade_date = self.get_prev_trading_day(trade_date)

        return trade_date, prev_trade_date

    # ========================================
    # ETF 매핑
    # ========================================

    def get_etfs_by_industry(self, industry_code: str) -> List[Dict]:
        """
        산업 코드로 관련 ETF 조회

        Args:
            industry_code: group_code (IT_SEMI, BIO 등)

        Returns:
            관련 ETF 목록 [{etf_id, name, sector, weight_pct}, ...]
        """
        # 1. etf_sector_cluster에서 group_code 매칭
        breakdowns = self.db.query(
            ETFSectorCluster.etf_id,
            ETFSectorCluster.weight_pct,
            ETF.name,
            ETF.sector,
            ETF.stock_code
        ).join(
            ETF, ETF.id == ETFSectorCluster.etf_id
        ).filter(
            ETFSectorCluster.group_code == industry_code,
            ETF.is_active == True
        ).all()

        # 2. ETF별로 그룹핑 (같은 ETF에 여러 breakdown이 있을 수 있음)
        etf_map = {}
        for b in breakdowns:
            if b.etf_id not in etf_map:
                etf_map[b.etf_id] = {
                    "etf_id": b.etf_id,
                    "name": b.name,
                    "sector": b.sector,
                    "stock_code": b.stock_code,
                    "weight_pct": float(b.weight_pct)
                }
            else:
                # 같은 산업 비중 합산
                etf_map[b.etf_id]["weight_pct"] += float(b.weight_pct)

        return list(etf_map.values())

    def get_etfs_by_company(self, company_id: int) -> List[Dict]:
        """
        회사 ID로 관련 ETF 조회 (etf_compositions 기반)

        Args:
            company_id: company_info.id

        Returns:
            관련 ETF 목록 [{etf_id, name, sector, weight_pct}, ...]
        """
        compositions = self.db.query(
            ETFComposition.etf_id,
            ETFComposition.weight_pct,
            ETF.name,
            ETF.sector,
            ETF.stock_code
        ).join(
            ETF, ETF.id == ETFComposition.etf_id
        ).filter(
            ETFComposition.company_id == company_id,
            ETF.is_active == True
        ).all()

        etf_list = []
        for c in compositions:
            etf_list.append({
                "etf_id": c.etf_id,
                "name": c.name,
                "sector": c.sector,
                "stock_code": c.stock_code,
                "weight_pct": float(c.weight_pct) if c.weight_pct else 0
            })

        return etf_list

    def get_etf_change_rate(
        self,
        etf_id: int,
        trade_date: date,
        prev_trade_date: date
    ) -> Optional[float]:
        """
        ETF 변동률 조회

        Returns:
            변동률 (%), None if data not available
        """
        # 당일 종가
        current_price = self.db.query(ETFPrice).filter(
            ETFPrice.etf_id == etf_id,
            ETFPrice.trade_date == trade_date
        ).first()

        # 전일 종가
        prev_price = self.db.query(ETFPrice).filter(
            ETFPrice.etf_id == etf_id,
            ETFPrice.trade_date == prev_trade_date
        ).first()

        if not current_price or not prev_price:
            return None

        if not current_price.close or not prev_price.close:
            return None

        if prev_price.close == 0:
            return None

        change_rate = (
            (float(current_price.close) - float(prev_price.close))
            / float(prev_price.close)
            * 100
        )
        return round(change_rate, 4)

    # ========================================
    # 영향도 계산
    # ========================================

    def calculate_influence_score(
        self,
        relevance: float,
        weight_pct: float,
        sentiment: str,
        actual_change: Optional[float]
    ) -> float:
        """
        영향도 점수 계산

        영향도 = (산업 관련도 × 0.4) + (ETF 내 비중 × 0.3) + (변동률 기여도 × 0.3)
        """
        # 비중 기여도 (0~1 정규화, 최대 50% 기준)
        weight_factor = min(weight_pct / 50.0, 1.0)

        # 변동률 기여도
        change_factor = 0.5  # 기본값 (데이터 없을 때)
        if actual_change is not None:
            # 절대값 기준, 최대 5% = 1.0
            change_factor = min(abs(actual_change) / 5.0, 1.0)

            # sentiment와 변동 방향 일치 시 보너스
            if sentiment == "POSITIVE" and actual_change > 0:
                change_factor *= 1.2
            elif sentiment == "NEGATIVE" and actual_change < 0:
                change_factor *= 1.2

        influence = (
            relevance * 0.4 +
            weight_factor * 0.3 +
            change_factor * 0.3
        )

        return min(round(influence, 4), 1.0)

    def determine_influence_type(
        self,
        sentiment: str,
        actual_change: Optional[float]
    ) -> str:
        """영향 유형 결정"""
        if actual_change is not None:
            # 실제 변동률 기준
            if actual_change >= 1.0:
                return "POSITIVE"
            elif actual_change <= -1.0:
                return "NEGATIVE"
            else:
                return "NEUTRAL"
        else:
            # 변동률 없으면 sentiment 사용
            return sentiment

    # ========================================
    # 즉시 매핑 (Step 2a: 뉴스 상세용)
    # ========================================

    def _score_to_sentiment(self, impact_score: float) -> str:
        """impact_score를 sentiment로 변환"""
        if impact_score >= 0.3:
            return "POSITIVE"
        elif impact_score <= -0.3:
            return "NEGATIVE"
        else:
            return "NEUTRAL"

    async def map_news_to_etfs(self, news_id: int) -> List[Dict]:
        """
        뉴스에 대한 관련 ETF 매핑 (즉시 실행)

        news_impact 테이블 기반:
        - COMPANY 타입 → etf_compositions 통해 ETF 매핑
        - INDUSTRY 타입 → etf_sector_cluster 통해 ETF 매핑

        Returns:
            관련 ETF 목록
        """
        # 1. 해당 뉴스의 영향 분석 조회 (news_impact)
        impacts = self.db.query(NewsImpact).filter(
            NewsImpact.news_id == news_id
        ).all()

        if not impacts:
            logger.debug(f"영향 분석 없음: news_id={news_id}")
            return []

        mapped_etfs = []
        processed_etf_ids = set()

        for impact in impacts:
            related_etfs = []

            # 2. 타입별 ETF 조회
            if impact.target_type == "COMPANY" and impact.company_id:
                related_etfs = self.get_etfs_by_company(impact.company_id)
            elif impact.target_type == "INDUSTRY" and impact.industry_code:
                related_etfs = self.get_etfs_by_industry(impact.industry_code)

            sentiment = self._score_to_sentiment(float(impact.impact_score or 0))

            for etf in related_etfs:
                if etf["etf_id"] in processed_etf_ids:
                    continue

                processed_etf_ids.add(etf["etf_id"])

                # 3. 영향도 계산 (변동률 없이)
                # impact_score 절대값을 relevance로 사용
                relevance = abs(float(impact.impact_score or 0))
                influence_score = self.calculate_influence_score(
                    relevance=relevance,
                    weight_pct=etf["weight_pct"],
                    sentiment=sentiment,
                    actual_change=None
                )

                # 4. 최소 영향도 이상만 저장
                if influence_score < 0.3:
                    continue

                # 5. 기존 레코드 확인
                existing = self.db.query(NewsETFInfluence).filter(
                    NewsETFInfluence.news_id == news_id,
                    NewsETFInfluence.etf_id == etf["etf_id"]
                ).first()

                if existing:
                    continue

                # 6. 저장
                news_etf = NewsETFInfluence(
                    news_id=news_id,
                    etf_id=etf["etf_id"],
                    influence_score=influence_score,
                    influence_type=sentiment,
                    is_verified=False
                )
                self.db.add(news_etf)

                mapped_etfs.append({
                    "etf_id": etf["etf_id"],
                    "name": etf["name"],
                    "sector": etf["sector"],
                    "stock_code": etf["stock_code"],
                    "influence_score": influence_score,
                    "influence_type": sentiment
                })

        self.db.commit()
        logger.info(f"뉴스-ETF 매핑 완료: news_id={news_id}, ETF {len(mapped_etfs)}개")

        return mapped_etfs

    # ========================================
    # 검증 배치 (Step 2b: 장 마감 후)
    # ========================================

    async def verify_news_etf_influences(self) -> Dict[str, int]:
        """
        장 마감 후 실제 데이터 기반 ETF 영향도 검증

        Returns:
            {"total": int, "verified": int, "skipped": int}
        """
        today = date.today()
        result = {"total": 0, "verified": 0, "skipped": 0}

        # 1. 미검증 뉴스 영향 분석 조회 (news_impact)
        unverified = self.db.query(NewsImpact).join(
            NewsArticle, NewsArticle.id == NewsImpact.news_id
        ).filter(
            ~NewsImpact.news_id.in_(
                self.db.query(NewsETFInfluence.news_id).filter(
                    NewsETFInfluence.is_verified == True
                ).distinct()
            )
        ).all()

        result["total"] = len(unverified)
        processed_news_ids = set()

        for impact in unverified:
            if impact.news_id in processed_news_ids:
                continue

            # 2. 뉴스 정보 조회
            news = self.db.query(NewsArticle).filter(
                NewsArticle.id == impact.news_id
            ).first()

            if not news or not news.published_at:
                result["skipped"] += 1
                continue

            # 3. 검증 가능 여부 확인
            trade_date, prev_trade_date = self.get_verification_trade_dates(
                news.published_at
            )

            if trade_date > today:
                # 아직 검증 불가
                result["skipped"] += 1
                continue

            processed_news_ids.add(impact.news_id)

            # 4. 해당 뉴스의 모든 영향 분석 조회
            all_impacts = self.db.query(NewsImpact).filter(
                NewsImpact.news_id == impact.news_id
            ).all()

            for imp in all_impacts:
                await self._verify_single_impact(
                    news=news,
                    impact=imp,
                    trade_date=trade_date,
                    prev_trade_date=prev_trade_date
                )

            result["verified"] += 1

        logger.info(
            f"뉴스-ETF 검증 완료: 총 {result['total']}건 중 "
            f"검증 {result['verified']}건, 스킵 {result['skipped']}건"
        )

        return result

    async def _verify_single_impact(
        self,
        news: NewsArticle,
        impact: NewsImpact,
        trade_date: date,
        prev_trade_date: date
    ):
        """단일 영향 분석에 대한 ETF 검증"""
        related_etfs = []

        # 타입별 ETF 조회
        if impact.target_type == "COMPANY" and impact.company_id:
            related_etfs = self.get_etfs_by_company(impact.company_id)
        elif impact.target_type == "INDUSTRY" and impact.industry_code:
            related_etfs = self.get_etfs_by_industry(impact.industry_code)

        sentiment = self._score_to_sentiment(float(impact.impact_score or 0))

        for etf in related_etfs:
            # 실제 변동률 조회
            actual_change = self.get_etf_change_rate(
                etf_id=etf["etf_id"],
                trade_date=trade_date,
                prev_trade_date=prev_trade_date
            )

            if actual_change is None:
                continue

            # 영향도 계산
            relevance = abs(float(impact.impact_score or 0))
            influence_score = self.calculate_influence_score(
                relevance=relevance,
                weight_pct=etf["weight_pct"],
                sentiment=sentiment,
                actual_change=actual_change
            )

            if influence_score < 0.3:
                continue

            influence_type = self.determine_influence_type(
                sentiment=sentiment,
                actual_change=actual_change
            )

            # 기존 레코드 확인/업데이트
            existing = self.db.query(NewsETFInfluence).filter(
                NewsETFInfluence.news_id == news.id,
                NewsETFInfluence.etf_id == etf["etf_id"]
            ).first()

            if existing:
                # 업데이트
                existing.influence_score = influence_score
                existing.influence_type = influence_type
                existing.actual_change_rate = actual_change
                existing.is_verified = True
                existing.verified_at = datetime.now()
            else:
                # 새로 생성
                news_etf = NewsETFInfluence(
                    news_id=news.id,
                    etf_id=etf["etf_id"],
                    influence_score=influence_score,
                    influence_type=influence_type,
                    actual_change_rate=actual_change,
                    is_verified=True,
                    verified_at=datetime.now()
                )
                self.db.add(news_etf)

            # 타임라인 텍스트 생성
            await self._generate_timeline(
                news=news,
                etf_name=etf["name"],
                etf_sector=etf["sector"],
                actual_change=actual_change,
                sentiment=influence_type
            )

        self.db.commit()

    async def _generate_timeline(
        self,
        news: NewsArticle,
        etf_name: str,
        etf_sector: str,
        actual_change: float,
        sentiment: str
    ):
        """타임라인 텍스트 생성"""
        if not self.llm.is_configured():
            return

        # content_summary에서 bullets 추출
        summary_text = ""
        if news.content_summary:
            bullets = news.content_summary.get("bullets", [])
            summary_text = " ".join(bullets)

        user_message = f"""[뉴스 정보]
제목: {news.title}
요약: {summary_text}

[ETF 정보]
ETF명: {etf_name}
섹터: {etf_sector}

[실제 영향]
ETF 변동률: {actual_change}%
감성: {sentiment}"""

        try:
            result = await self.llm.analyze_with_prompt(self.PROMPT_NAME, user_message)
            if result:
                # 해당 news_etf_influence 업데이트
                news_etf = self.db.query(NewsETFInfluence).filter(
                    NewsETFInfluence.news_id == news.id
                ).first()

                if news_etf:
                    news_etf.timeline_title = result.get("timeline_title")
                    news_etf.timeline_summary = result.get("timeline_summary")

        except Exception as e:
            logger.error(f"타임라인 생성 실패: {e}")

    # ========================================
    # 일괄 처리
    # ========================================

    async def map_recent_news(self, hours: int = 1, limit: int = 50) -> Dict[str, int]:
        """
        최근 N시간 내 분석된 뉴스에 대해 ETF 매핑

        Returns:
            {"total": int, "mapped": int}
        """
        cutoff = datetime.now() - timedelta(hours=hours)

        # news_impact가 있지만 ETF 매핑 안된 뉴스
        news_ids = self.db.query(NewsImpact.news_id).filter(
            NewsImpact.created_at >= cutoff
        ).distinct().all()

        news_ids = [n[0] for n in news_ids]

        # 이미 매핑된 뉴스 제외
        mapped_ids = self.db.query(NewsETFInfluence.news_id).filter(
            NewsETFInfluence.news_id.in_(news_ids)
        ).distinct().all()
        mapped_ids = {m[0] for m in mapped_ids}

        unmapped_ids = [nid for nid in news_ids if nid not in mapped_ids][:limit]

        result = {"total": len(unmapped_ids), "mapped": 0}

        for news_id in unmapped_ids:
            try:
                mapped = await self.map_news_to_etfs(news_id)
                if mapped:
                    result["mapped"] += 1
                await asyncio.sleep(0.1)
            except Exception as e:
                logger.error(f"ETF 매핑 실패 [news_id={news_id}]: {e}")
                continue

        logger.info(
            f"뉴스-ETF 매핑 완료: 총 {result['total']}건 중 {result['mapped']}건 매핑"
        )

        return result


# 스케줄러에서 사용할 함수
async def scheduled_news_etf_mapping(db: Session) -> Dict[str, int]:
    """스케줄러에서 호출하는 뉴스-ETF 매핑 함수"""
    mapper = NewsETFMapper(db)
    try:
        return await mapper.map_recent_news(hours=1, limit=50)
    finally:
        await mapper.close()


async def scheduled_news_etf_verification(db: Session) -> Dict[str, int]:
    """스케줄러에서 호출하는 뉴스-ETF 검증 함수 (장 마감 후)"""
    mapper = NewsETFMapper(db)
    try:
        return await mapper.verify_news_etf_influences()
    finally:
        await mapper.close()
