"""뉴스 영향 분석 모델 (1:N 회사/산업 매핑)"""
from sqlalchemy import Column, BigInteger, String, DECIMAL, TIMESTAMP, ForeignKey, CheckConstraint, Index
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship

from app.database import Base


class NewsImpact(Base):
    """
    뉴스 영향 분석 테이블 (ERD: news_impact)

    1:N 매핑: 하나의 뉴스가 여러 회사/산업에 각각 다른 영향도로 매핑
    Constrained LLM: GPT-4o가 DB 목록에서만 선택
    """
    __tablename__ = "news_impact"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    news_id = Column(BigInteger, ForeignKey("news_article.id", ondelete="CASCADE"), nullable=False)

    # 영향 대상 (둘 중 하나만 값 있음)
    target_type = Column(String(20), nullable=False)  # 'COMPANY' | 'INDUSTRY'
    company_id = Column(BigInteger, ForeignKey("company_info.id", ondelete="CASCADE"))
    industry_code = Column(String(20), ForeignKey("industry_classification.code", ondelete="CASCADE"))

    # 영향도
    impact_score = Column(DECIMAL(3, 2), nullable=False)  # -1.00 ~ +1.00
    impact_reason = Column(String(200))  # "AI 반도체 투자 확대로 실적 개선 기대"

    created_at = Column(TIMESTAMP(timezone=True), server_default=func.now())

    # Constraints
    __table_args__ = (
        CheckConstraint(
            "(target_type = 'COMPANY' AND company_id IS NOT NULL AND industry_code IS NULL) OR "
            "(target_type = 'INDUSTRY' AND industry_code IS NOT NULL AND company_id IS NULL)",
            name="chk_news_impact_target"
        ),
        Index("idx_news_impact_news", "news_id"),
        Index("idx_news_impact_company", "company_id", postgresql_where="company_id IS NOT NULL"),
        Index("idx_news_impact_industry", "industry_code", postgresql_where="industry_code IS NOT NULL"),
        Index("idx_news_impact_score", "impact_score"),
    )

    def __repr__(self):
        target = f"company_id={self.company_id}" if self.target_type == "COMPANY" else f"industry={self.industry_code}"
        return f"<NewsImpact(news_id={self.news_id}, {target}, score={self.impact_score})>"

    @property
    def impact_type(self) -> str:
        """영향 유형 (POSITIVE/NEGATIVE/NEUTRAL)"""
        if self.impact_score >= 0.3:
            return "POSITIVE"
        elif self.impact_score <= -0.3:
            return "NEGATIVE"
        else:
            return "NEUTRAL"
