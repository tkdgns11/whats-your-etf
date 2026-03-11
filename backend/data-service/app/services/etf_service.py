import logging

from sqlalchemy.orm import Session

from app.config import get_settings
from app.database import get_db
from app.repositories.etf_repository import EtfRepository
from app.scrapers.dependencies import get_pykrx_client
logger = logging.getLogger(__name__)
settings = get_settings()

class EtfService:
    def __init__(self, db: Session):
        self.etf_repository = EtfRepository(db)
        self.pykrx_client = get_pykrx_client()

    def sync_etf_tickers(self):
        # 오늘 자 기준 상장된 etf ticker 리스트
        listed_etfs = self.pykrx_client.get_today_etf_list()

        # db 에 있는 etf tickers
        etf_tickers_in_db = set(self.etf_repository.get_etf_tickers())
        etfs = []
        for listed_etf in listed_etfs:
            if listed_etf.ticker in etf_tickers_in_db:
                continue
            etfs.append(listed_etf)
        self.etf_repository.save_initial_etf_infos(etfs)