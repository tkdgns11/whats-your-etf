import asyncio
import logging

from sqlalchemy.ext.asyncio import AsyncSession

from app.config import get_settings
from app.repositories.etf_repository import EtfRepository, EtfPriceRepository
from app.scrapers.dependencies import get_pykrx_client

logger = logging.getLogger(__name__)
settings = get_settings()

class EtfService:
    def __init__(self, db: AsyncSession):
        self.etf_repository = EtfRepository(db)
        self.etf_price_repository = EtfPriceRepository(db)
        self.pykrx_client = get_pykrx_client()
        self.semaphore = asyncio.Semaphore(5)

    async def sync_etf_tickers(self):
        # 오늘 자 기준 상장된 etf ticker 리스트
        listed_etfs = await self.pykrx_client.get_today_etf_list()

        # db 에 있는 etf tickers
        etf_tickers_in_db = set(await self.etf_repository.get_etf_tickers())
        etfs = []
        for listed_etf in listed_etfs:
            if listed_etf.ticker in etf_tickers_in_db:
                continue
            etfs.append(listed_etf)
        infos = await self.etf_repository.save_initial_etf_infos(etfs)

        tasks = [
            self.save_new_etf_price_histories(meta["ticker"], meta["id"])
            for meta in infos
        ]
        await asyncio.gather(*tasks)

    async def save_new_etf_price_histories(self, ticker: str, etf_id: int):
        async with self.semaphore:  # 동시성 제어
            price_histories = await self.pykrx_client.get_price_history(ticker)

            if not price_histories:
                print(f"price_histories not found {price_histories}")
                return

            # 데이터 가공: 각 row에 etf_id 주입
            for history in price_histories:
                history['etf_id'] = etf_id

            # Repository를 통한 Bulk Insert
            await self.etf_price_repository.save_bulk(price_histories)