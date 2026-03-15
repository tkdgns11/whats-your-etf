import asyncio
import logging

from sqlalchemy.ext.asyncio import AsyncSession

from app.config import get_settings
from app.repositories.etf_repository import EtfRepository, EtfPriceRepository
from app.scrapers.dependencies import get_pykrx_client

logger = logging.getLogger(__name__)
settings = get_settings()

from fastapi import BackgroundTasks
from app.database import AsyncSessionLocal
import traceback

async def process_etf_events_background(ticker: str, etf_id: int):
    from app.services.etf_service import EtfService
    async with AsyncSessionLocal() as db:
        try:
            service = EtfService(db)
            # 1. 가격 이력 저장
            await service.save_new_etf_price_histories(ticker, etf_id)
            # 2. PDF 분석하여 해외 주식 포함시 비활성화 처리
            await service.check_and_update_etf_active_status(ticker, etf_id)
        except Exception as e:
            logger.error(f"Error processing ETF events for {ticker}: {e}")
            logger.error(traceback.format_exc())

class EtfService:
    def __init__(self, db: AsyncSession):
        self.etf_repository = EtfRepository(db)
        self.etf_price_repository = EtfPriceRepository(db)
        self.pykrx_client = get_pykrx_client()
        self.semaphore = asyncio.Semaphore(5)

    async def sync_etf_tickers(self, background_tasks: BackgroundTasks = None):
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

        if background_tasks:
            for meta in infos:
                background_tasks.add_task(process_etf_events_background, meta["ticker"], meta["id"])
        else:
            # 백그라운드 태스크가 없으면 동기(gather)로 처리
            tasks = [
                process_etf_events_background(meta["ticker"], meta["id"])
                for meta in infos
            ]
            await asyncio.gather(*tasks)

    async def save_new_etf_price_histories(self, ticker: str, etf_id: int):
        async with self.semaphore:  # 동시성 제어
            await asyncio.sleep(0.2) # pykrx API 서버 호출 제한(CD006/CD003 등) 방지
            price_histories = await self.pykrx_client.get_price_history(ticker)

            if not price_histories:
                print(f"price_histories not found {ticker}")
                return

            # 데이터 가공: 각 row에 etf_id 주입
            for history in price_histories:
                history['etf_id'] = etf_id

            # Repository를 통한 Bulk Insert
            logging.info(f"[{ticker}] DB에 가격 이력 데이터 bulk insert 진행 중... ({len(price_histories)}건)")
            await self.etf_price_repository.save_bulk(price_histories)
            logging.info(f"[{ticker}] DB에 가격 이력 데이터 {len(price_histories)}건 적재 완료.")

    async def check_and_update_etf_active_status(self, ticker: str, etf_id: int):
        async with self.semaphore:
            await asyncio.sleep(0.2) # pykrx API 서버 호출 제한 방지
            pdf_tickers = await self.pykrx_client.get_etf_pdf_tickers(ticker)
            if not pdf_tickers:
                return
            
            # 주식의 ticker는 주로 6자리 숫자입니다 (예: 005930)
            # 6자리 숫자가 아닌 경우 해외 주식이나 기타 상품으로 간주하여 비활성화
            # User requirement: "pdf 안에 살펴 보면서 해외 주식이 포함된 etf는 isactive =false가 되도록 해주시면됩니다"
            has_foreign_stock = False
            for pdf_ticker in pdf_tickers:
                if not (pdf_ticker.isdigit() and len(pdf_ticker) == 6):
                    has_foreign_stock = True
                    break
            
            if has_foreign_stock:
                logger.info(f"ETF {ticker} contains foreign or non-standard stock. Deactivating.")
                await self.etf_repository.update_etf_active_status(etf_id, False)