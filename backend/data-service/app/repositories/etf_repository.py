from fastapi import Depends
from sqlalchemy.orm import Session
from sqlalchemy import select, insert

from app.database import get_db
from app.models import ETF
from app.scrapers.pykrx_client import EtfInfo

class EtfRepository:
    def __init__(self, db: Session):
        self.db = db


    # db에 적재 중인 etf 목록
    def get_etf_tickers(self) -> list[str]:
        stmt = select(ETF.stock_code)
        return list(self.db.execute(stmt).scalars().all())

    def save_initial_etf_infos(self, etf_infos: list[EtfInfo]):
        if not etf_infos:
            return

        rows = [
            {
                "stock_code" : etf.ticker,
                "name" : etf.etf_name,
                "asset_manager" : etf.etf_manager
            }
            for etf in etf_infos
        ]

        self.db.execute(insert(ETF), rows)
        self.db.commit()