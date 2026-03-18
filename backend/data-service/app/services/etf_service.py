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
        logger.info(f"{len(infos)}개의 신규 ETF가 기본 정보 수집 완료되었습니다.")

    async def update_etfs_active_status(self):
        unchecked_etfs = await self.etf_repository.get_unchecked_etfs()
        if not unchecked_etfs:
            logger.info("상태 검사가 필요한 신규 ETF가 없습니다.")
            return

        for etf in unchecked_etfs:
            ticker = etf["ticker"]
            etf_id = etf["id"]
            
            async with _krx_api_semaphore:
                await asyncio.sleep(0.2)
                pdf_tickers = await self.pykrx_client.get_etf_pdf_tickers(ticker)
                
                if not pdf_tickers:
                    logger.warning(f"[{ticker}] PDF 구성종목을 조회하지 못했습니다.")
                    continue
                
                has_foreign_stock = False
                for pdf_ticker in pdf_tickers:
                    if not (pdf_ticker.isdigit() and len(pdf_ticker) == 6):
                        has_foreign_stock = True
                        break
                
                is_krx_only = not has_foreign_stock
                logger.info(f"[{ticker}] 국내 전용 여부 판별 완료 (is_krx_only={is_krx_only})")
                await self.etf_repository.update_krx_status(etf_id, is_krx_only)

    async def sync_etf_prices(self):
        from datetime import date, datetime, timedelta
        active_etfs = await self.etf_repository.get_active_etfs()
        if not active_etfs:
            logger.info("가격 이력을 수집할 활성 ETF가 없습니다.")
            return

        now = datetime.now()
        # 오후 4시(16:00) 이전이면 어제 데이터를 최신 기준으로 설정
        target_end_date = now.date() if now.hour >= 16 else now.date() - timedelta(days=1)
        
        for etf in active_etfs:
            ticker = etf["ticker"]
            etf_id = etf["id"]
            
            latest_date = await self.etf_price_repository.get_latest_price_date(etf_id)
            if latest_date is None:
                start_date = "20230302"
            else:
                if latest_date >= target_end_date:
                    logger.info(f"[{ticker}] 이미 최신 날짜({latest_date})의 가격 이력이 존재합니다.")
                    continue
                next_date = latest_date + timedelta(days=1)
                if next_date > target_end_date:
                    continue  # 이미 최신
                start_date = next_date.strftime("%Y%m%d")
                
            async with _krx_api_semaphore:
                await asyncio.sleep(0.2)
                price_histories = await self.pykrx_client.get_price_history(
                    ticker, 
                    start_date=start_date,
                    end_date=target_end_date.strftime("%Y%m%d")
                )

                if not price_histories:
                    continue

                for history in price_histories:
                    history['etf_id'] = etf_id
                    history['created_at'] = now

                logging.info(f"[{ticker}] DB 가격 이력 {len(price_histories)}건 적재 완료.")
                await self.etf_price_repository.save_bulk(price_histories)

    async def update_empty_company_infos(self):
        tickers = await self.stock_repository.get_stocks_with_empty_company_info()
        if not tickers:
            logger.info("회사 정보 동기화가 필요한 국내 주식이 없습니다.")
            return
            
        logger.info(f"회사 정보가 누락된 주식 {len(tickers)}건 수집 시작...")
        await self.process_domestic_stocks(tickers)

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