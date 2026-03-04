from sqlalchemy import Column, BigInteger, Text, String, Integer, Boolean, TIMESTAMP, func
from sqlalchemy.dialects.postgresql import JSONB
from app.database import Base


class NewsArticle(Base):
    """뉴스 기사 테이블 (ERD: news_article)"""
    __tablename__ = "news_article"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    title = Column(String(500), nullable=False)
    content = Column(Text)                          # 뉴스 본문 전체
    content_summary = Column(JSONB)                 # AI 요약 {"bullets": ["...", "...", "..."]}
    source = Column(String(100))                    # 언론사명
    source_url = Column(String(1000), nullable=False, unique=True)  # 원본 URL
    thumbnail_url = Column(String(1000))            # 썸네일 이미지 URL
    category = Column(String(50), default="금융")   # 금융 / ETF / 경제
    keywords = Column(JSONB)                        # 키워드 배열 ["반도체", "ETF"]
    published_at = Column(TIMESTAMP(timezone=True))
    view_count = Column(Integer, default=0)
    is_active = Column(Boolean, default=True)
    created_at = Column(TIMESTAMP(timezone=True), server_default=func.now())
