"""뉴스-산업 영향력 모델"""
from sqlalchemy import Column, BigInteger, String, DECIMAL, TIMESTAMP, ForeignKey
from sqlalchemy.sql import func

from app.database import Base


class NewsIndustryInfluence(Base):
    """뉴스-산업 영향력 테이블 (ERD: news_industry_influence)

    1차 분석 결과: 뉴스 → 산업 매핑
    LLM이 뉴스를 분석하여 관련 산업과 감성을 추출
    """
    __tablename__ = "news_industry_influence"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    news_id = Column(BigInteger, ForeignKey("news_article.id", ondelete="CASCADE"), nullable=False)
    industry_code = Column(String(10), nullable=False)  # group_code (IT_SEMI, BIO 등)
    relevance_score = Column(DECIMAL(5, 4))  # 0.0 ~ 1.0 관련도
    sentiment = Column(String(20))  # POSITIVE / NEGATIVE / NEUTRAL
    created_at = Column(TIMESTAMP(timezone=True), server_default=func.now())

    def __repr__(self):
        return f"<NewsIndustryInfluence(news_id={self.news_id}, industry={self.industry_code}, sentiment={self.sentiment})>"
