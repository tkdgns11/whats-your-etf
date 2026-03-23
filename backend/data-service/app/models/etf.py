"""ETF 관련 모델"""
from sqlalchemy import Column, BigInteger, String, DECIMAL, Boolean, Date, TIMESTAMP, Integer, ForeignKey
from sqlalchemy.sql import func

from app.database import Base


class ETF(Base):
    """ETF 테이블 (ERD: etf)"""
    __tablename__ = "etf"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    stock_code = Column(String(20), unique=True, nullable=False)
    name = Column(String(200), nullable=False)
    english_name = Column(String(200))

    # 분류
    category = Column(String(50))  # 국내주식형/해외주식형/채권형 등
    strategy_type = Column(String(30))  # MARKET/THEME/DIVIDEND/BOND/DERIVATIVE
    sector = Column(String(50))  # 반도체/2차전지/AI/배당 등
    asset_class = Column(String(30))  # EQUITY/BOND/COMMODITY/MIXED
    asset_manager = Column(String(50))  # KODEX/TIGER/KBSTAR 등

    # 속성 플래그
    is_leveraged = Column(Boolean, default=False)
    is_inverse = Column(Boolean, default=False)
    is_derivatives = Column(Boolean, default=False)
    is_krx_only = Column(Boolean, default=None)

    # 비용/규모
    expense_ratio = Column(DECIMAL(6, 4))
    nav = Column(DECIMAL(14, 2))
    aum = Column(BigInteger)

    # 배당
    dividend_yield = Column(DECIMAL(6, 3))
    dividend_freq = Column(String(10))  # MONTHLY/QUARTERLY/SEMI_ANNUAL/ANNUAL/NONE

<<<<<<< Updated upstream
    # 밸류에이션 (구성종목 비중 가중평균)
    per = Column(DECIMAL(8, 2))
    pbr = Column(DECIMAL(8, 2))
    roe = Column(DECIMAL(8, 2))

    # 위험 유형 (CONSERVATIVE/STABLE/MODERATE/ACTIVE/AGGRESSIVE)
    risk_type = Column(String(20))

    # 생애주기
    listing_date = Column(Date)
    delisted_date = Column(Date)
    is_active = Column(Boolean, default=True)

    created_at = Column(TIMESTAMP(timezone=True), server_default=func.now())
    updated_at = Column(TIMESTAMP(timezone=True), server_default=func.now(), onupdate=func.now())

    def __repr__(self):
        return f"<ETF(code={self.stock_code}, name={self.name})>"


class ETFSectorCluster(Base):
    """ETF 섹터 분포 테이블 (ERD: etf_sector_cluster)"""
    __tablename__ = "etf_sector_cluster"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    etf_id = Column(BigInteger, ForeignKey("etf.id", ondelete="CASCADE"), nullable=False)

    cluster_type = Column(String(20), nullable=False)  # GROUP_CODE / INDUSTRY / SUB_SECTOR
    industry_code = Column(String(10))
    industry_name = Column(String(100))
    group_code = Column(String(20))  # IT_SEMI, BIO 등
    group_name = Column(String(50))
    sub_sector = Column(String(100))

    weight_pct = Column(DECIMAL(6, 3), nullable=False)
    stock_count = Column(Integer)

    # 시각화 좌표
    pos_x = Column(DECIMAL(10, 6))
    pos_y = Column(DECIMAL(10, 6))
    radius = Column(DECIMAL(10, 6))
    distance_to_center = Column(DECIMAL(10, 6))

    base_date = Column(Date, nullable=False)
    created_at = Column(TIMESTAMP(timezone=True), server_default=func.now())

    def __repr__(self):
        return f"<ETFSectorCluster(etf_id={self.etf_id}, group={self.group_code}, weight={self.weight_pct})>"


class ETFComposition(Base):
    """ETF 구성종목 테이블 (ERD: etf_compositions)"""
    __tablename__ = "etf_compositions"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    etf_id = Column(BigInteger, ForeignKey("etf.id", ondelete="CASCADE"), nullable=False)
    company_id = Column(BigInteger, ForeignKey("company_info.id", ondelete="SET NULL"))  # NULL = 현금/기타
    component_stock_code = Column(String(20))
    weight_pct = Column(DECIMAL(6, 3))
    base_date = Column(Date, nullable=False)
    created_at = Column(TIMESTAMP(timezone=True), server_default=func.now())

    def __repr__(self):
        return f"<ETFComposition(etf_id={self.etf_id}, company_id={self.company_id}, weight={self.weight_pct})>"


class ETFPrice(Base):
    """ETF 일별 시세 테이블 (ERD: etf_prices)"""
    __tablename__ = "etf_prices"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    etf_id = Column(BigInteger, ForeignKey("etf.id", ondelete="CASCADE"), nullable=False)
    trade_date = Column(Date, nullable=False)
    close = Column(DECIMAL(14, 2))
    nav = Column(DECIMAL(14, 2))
    volume = Column(BigInteger)
    change_rate = Column(DECIMAL(8, 4))  # 등락률
    created_at = Column(TIMESTAMP(timezone=True), server_default=func.now())

    def __repr__(self):
        return f"<ETFPrice(etf_id={self.etf_id}, date={self.trade_date}, change={self.change_rate})>"
