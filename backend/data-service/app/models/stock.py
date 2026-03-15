from sqlalchemy import Column, BigInteger, String, DECIMAL, Boolean, Date, TIMESTAMP, Integer, ForeignKey
from sqlalchemy.sql import func
from app.database import Base

class Stock(Base):
    """주식 테이블 (ERD: stock)"""
    __tablename__ = "stock"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    # company_id = Column(BigInteger, ForeignKey("company_info.id")) # 주석 처리된 FK를 연결하려면 company_info 테이블이 필요하다.
    company_id = Column(BigInteger, ForeignKey("company_info.id", ondelete="SET NULL"), nullable=True) # User JPA Entity 참고
    ticker = Column(String(20), nullable=False, unique=True)
    close = Column(DECIMAL(14, 2))
    listing_date = Column(Date)
    face_value = Column(Integer)
    listed_shares = Column(BigInteger)
    market_type = Column(String(20)) # KOSPI / KOSDAQ / NYSE / NASDAQ 등
    is_active = Column(Boolean, default=True)
    created_at = Column(TIMESTAMP(timezone=True), server_default=func.now())

    def __repr__(self):
        return f"<Stock(ticker={self.ticker})>"
