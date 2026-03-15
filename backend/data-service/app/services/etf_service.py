import asyncio
import logging

from sqlalchemy.ext.asyncio import AsyncSession

from app.config import get_settings
from app.repositories.etf_repository import EtfRepository, EtfPriceRepository
from app.scrapers.dependencies import get_pykrx_client

from app.repositories.stock_repository import StockRepository

logger = logging.getLogger(__name__)
settings = get_settings()

from fastapi import BackgroundTasks
from app.database import AsyncSessionLocal
import traceback

_krx_api_semaphore = asyncio.Semaphore(5)

async def process_all_etf_events_background(infos: list[dict]):
    from app.services.etf_service import EtfService
    
    async def _process_one(ticker: str, etf_id: int):
        async with AsyncSessionLocal() as db:
            try:
                service = EtfService(db)
                # 1. 가격 이력 저장 (테스트를 위해 잠시 주석 처리)
                # await service.save_new_etf_price_histories(ticker, etf_id)
                # 2. PDF 분석하여 해외 주식 등 포함시 비활성화 & 국내 주식 DB에 저장
                await service.check_and_update_etf_active_status(ticker, etf_id)
            except Exception as e:
                logger.error(f"Error processing ETF events for {ticker}: {e}")
                logger.error(traceback.format_exc())

    tasks = [_process_one(meta["ticker"], meta["id"]) for meta in infos]
    await asyncio.gather(*tasks)

class EtfService:
    def __init__(self, db: AsyncSession):
        self.etf_repository = EtfRepository(db)
        self.etf_price_repository = EtfPriceRepository(db)
        self.stock_repository = StockRepository(db)
        self.pykrx_client = get_pykrx_client()

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
            background_tasks.add_task(process_all_etf_events_background, infos)
        else:
            await process_all_etf_events_background(infos)

    async def save_new_etf_price_histories(self, ticker: str, etf_id: int):
        async with _krx_api_semaphore:  # 동시성 제어
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
        async with _krx_api_semaphore:
            await asyncio.sleep(0.2) # pykrx API 서버 호출 제한 방지
            pdf_tickers = await self.pykrx_client.get_etf_pdf_tickers(ticker)
            if not pdf_tickers:
                return
            
            # 주식의 ticker는 주로 6자리 숫자입니다 (예: 005930)
            # 6자리 숫자가 아닌 경우 해외 주식이나 기타 상품으로 간주하여 비활성화
            # User requirement: "pdf 안에 살펴 보면서 해외 주식이 포함된 etf는 isactive =false가 되도록 해주시면됩니다"
            has_foreign_stock = False
            domestic_stocks = []
            for pdf_ticker in pdf_tickers:
                if not (pdf_ticker.isdigit() and len(pdf_ticker) == 6):
                    has_foreign_stock = True
                else:
                    domestic_stocks.append(pdf_ticker)
            
            if domestic_stocks:
                logging.info(f"[{ticker}] 국내 상장 주식 {len(domestic_stocks)}건 DB insert 및 회사정보 수집 중...")
                await self.process_domestic_stocks(domestic_stocks)
                logging.info(f"[{ticker}] 국내 상장 주식 {len(domestic_stocks)}건 저장 완료.")

            if has_foreign_stock:
                logger.info(f"ETF {ticker} contains foreign or non-standard stock. Deactivating.")
                await self.etf_repository.update_etf_active_status(etf_id, False)

    async def process_domestic_stocks(self, tickers: list[str]):
        from app.scrapers.data_portal_client import DataPortalClient
        client = DataPortalClient()
        for idx, ticker in enumerate(tickers, start=1):
            try:
                # 1. ticker로 사업자등록번호(crno) 및 기본 회사명 조회
                item_info = await client.get_stock_item_info(ticker)
                if not item_info:
                    # API로 못찾는 경우라도 주식 테이블엔 저장되도록
                    await self.stock_repository.get_or_create_stock(ticker, None, None)
                else:
                    corp_name = item_info.get("corpNm")
                    market_type = item_info.get("mrktCtg")
                    corp_number = item_info.get("crno")

                    if not corp_name:
                        await self.stock_repository.get_or_create_stock(ticker, None, market_type)
                    else:
                        # 2. Company 저장 (외래키 대상)
                        company = await self.stock_repository.get_or_create_company(corp_name)

                        # 3. Stock 저장 (Company 외래키 연결)
                        await self.stock_repository.get_or_create_stock(ticker, company.id, market_type)

                        # 4. 사업자등록번호(crno)로 회사 세부정보 조회 및 채우기
                        if corp_number:
                            corp_outline = await client.get_corp_outline(corp_number)
                            if corp_outline:
                                info = {
                                    "industry_name": corp_outline.get("sicNm"),
                                    "ceo_name": corp_outline.get("enpRprFnm"),
                                    "homepage": corp_outline.get("enpHmpgUrl"),
                                    "region": corp_outline.get("enpBsadr"),
                                    "description": corp_outline.get("enpMainBizNm"),
                                    "corporation_number": corp_number
                                }
                                await self.stock_repository.update_company_info(company.id, info)
            except Exception as e:
                logging.error(f"[{ticker}] 처리 중 오류 발생: {e}")
                await self.stock_repository.db.rollback()
                continue

            # 10개마다 중간 commit하여 DB에 즉시 반영
            if idx % 10 == 0:
                await self.stock_repository.db.commit()
                logging.info(f"[진행상황] {idx}/{len(tickers)} 처리 완료, DB commit 완료")

        # 나머지 flush된 데이터 최종 commit
        await self.stock_repository.db.commit()
        logging.info(f"[완료] 전체 {len(tickers)}개 주식 정보 저장 완료")