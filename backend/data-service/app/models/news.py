from sqlalchemy import Column, BigInteger, Text, String, DECIMAL, TIMESTAMP, func
from sqlalchemy.dialects.postgresql import JSONB
from app.database import Base


class NewsArticle(Base):
    __tablename__ = "news_articles"

    news_id = Column(BigInteger, primary_key=True, autoincrement=True)
    title = Column(Text, nullable=False)
    content_summary = Column(Text)
    source = Column(String(100))
    source_url = Column(Text)
    category = Column(String(50))
    keywords = Column(JSONB)
    sentiment = Column(DECIMAL(3, 2))
    affected_clusters = Column(JSONB)
    affected_etfs = Column(JSONB)
    published_at = Column(TIMESTAMP(timezone=True))
    created_at = Column(TIMESTAMP(timezone=True), server_default=func.now())
