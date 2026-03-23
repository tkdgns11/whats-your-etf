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
            etf_name = etf.get("name", "")
            
            foreign_keywords = [
                '미국', '중국', '일본', '유로', '유럽', '인도', '베트남', '글로벌', 
                '차이나', '항셍', '러셀', '나스닥', 'S&P', '달러', '선진', '신흥국', 
                '대만', '프랑스', '독일', '영국', 'MSCI', '브라질', '멕시코', '라틴', 
                '아시아', '한중', '필라델피아', 'STOXX', 'CSI', '니케이', 'TOPIX', 
                'STAR50', '엔화', '위안화', '월드', '테슬라', '엔비디아', '애플', '이머징', '(H)'
            ]
            if any(k in etf_name.upper() for k in foreign_keywords):
                logger.info(f"[{ticker}] 이름({etf_name}) 기반 해외 자산 판별 (is_krx_only=False)")
                await self.etf_repository.update_krx_status(etf_id, False)
                continue
            
            async with _krx_api_semaphore:
                await asyncio.sleep(0.2)
                pdf_infos = await self.pykrx_client.get_etf_pdf_info(ticker)
                
                if not pdf_infos:
                    logger.warning(f"[{ticker}] PDF 구성종목을 조회하지 못했습니다.")
                    continue
                
                has_foreign_stock = False
                for pdf in pdf_infos:
                    pdf_ticker = pdf["ticker"].strip()
                    pdf_name = pdf["name"].strip()
                    
                    # 1. 6자리 순수 숫자 (표준 국내 주식/ETF)
                    is_standard_stock = pdf_ticker.isdigit() and len(pdf_ticker) == 6
                    import re
                    # 2. 이름에 한글이 포함된 경우 (국내 파생/채권/스왑/현금/우선주 등 대부분의 국내 상장 자산)
                    has_korean_name = bool(re.search(r'[가-힣]', pdf_name))
                    # 3. 원화표시
                    is_krw = pdf_ticker.upper() == 'KRW'
                    
                    if not (is_standard_stock or has_korean_name or is_krw):
                        has_foreign_stock = True
                        break
                
                is_krx_only = not has_foreign_stock
                logger.info(f"[{ticker}] 국내 전용 여부 판별 완료 (is_krx_only={is_krx_only})")
                await self.etf_repository.update_krx_status(etf_id, is_krx_only)

    async def force_update_all_etfs_active_status(self):
        """기존 DB의 모든 ETF를 대상으로 PDF를 재검사하여 is_krx_only 상태를 강제 업데이트합니다."""
        all_etfs = await self.etf_repository.get_all_etfs()
        if not all_etfs:
            logger.info("상태 검사가 필요한 ETF가 없습니다.")
            return

        for etf in all_etfs:
            ticker = etf["ticker"]
            etf_id = etf["id"]
            etf_name = etf.get("name", "")
            
            # 1차 검증: ETF 이름 자체에 해외 냄새(미국, 중국 등)가 나면 즉시 False 처리 (선물/합성/테마 등 커버)
            foreign_keywords = [
                '미국', '중국', '일본', '유로', '유럽', '인도', '베트남', '글로벌', 
                '차이나', '항셍', '러셀', '나스닥', 'S&P', '달러', '선진', '신흥국', 
                '대만', '프랑스', '독일', '영국', 'MSCI', '브라질', '멕시코', '라틴', 
                '아시아', '한중', '필라델피아', 'STOXX', 'CSI', '니케이', 'TOPIX', 
                'STAR50', '엔화', '위안화', '월드', '테슬라', '엔비디아', '애플', '이머징', '(H)'
            ]
            if any(k in etf_name.upper() for k in foreign_keywords):
                logger.info(f"[{ticker}] 이름({etf_name}) 기반 해외 자산 판별 강제 업데이트 (is_krx_only=False)")
                await self.etf_repository.update_krx_status(etf_id, False)
                continue
            
            async with _krx_api_semaphore:
                await asyncio.sleep(0.2)
                pdf_infos = await self.pykrx_client.get_etf_pdf_info(ticker)
                
                if not pdf_infos:
                    logger.warning(f"[{ticker}] PDF 구성종목을 조회하지 못했습니다.")
                    continue
                
                has_foreign_stock = False
                for pdf in pdf_infos:
                    pdf_ticker = pdf["ticker"].strip()
                    pdf_name = pdf["name"].strip()
                    
                    is_standard_stock = pdf_ticker.isdigit() and len(pdf_ticker) == 6
                    import re
                    has_korean_name = bool(re.search(r'[가-힣]', pdf_name))
                    is_krw = pdf_ticker.upper() == 'KRW'
                    
                    if not (is_standard_stock or has_korean_name or is_krw):
                        has_foreign_stock = True
                        break
                
                is_krx_only = not has_foreign_stock
                logger.info(f"[{ticker}] 강제 국내 전용 여부 판별 업데이트 완료 (is_krx_only={is_krx_only})")
                await self.etf_repository.update_krx_status(etf_id, is_krx_only)

    async def sync_etf_prices(self):
        from datetime import date, datetime, timedelta
        # 활성화된(국내 전용) ETF들만 가격 이력을 수집합니다
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
                    logger.debug(f"[{ticker}] 이미 최신 날짜({latest_date})의 가격 이력이 존재합니다.")
                    continue
                next_date = latest_date + timedelta(days=1)
                if next_date > target_end_date:
                    continue  # 이미 최신
                
                import pandas as pd
                # 영업일(월~금)이 하루도 포함되어 있지 않으면 불필요한 API 호출 생략 (주말 건너뛰기)
                if len(pd.bdate_range(start=next_date, end=target_end_date)) == 0:
                    continue

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

                logging.debug(f"[{ticker}] DB 가격 이력 {len(price_histories)}건 적재 완료.")
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
                        company = await self.stock_repository.get_or_create_company(corp_name, corp_number)

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
                                    "corporation_number": corp_number
                                }
                                await self.stock_repository.update_company_info(company.id, info)
                                
                                biz_description = corp_outline.get("enpMainBizNm")
                                if biz_description:
                                    await self.stock_repository.update_stock_description(ticker, biz_description)
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

    async def sync_etf_metadata(self):
        """
        KIS API (FHPST02400000)를 통해 활성 ETF의 상세 메타데이터를 동기화합니다.
        - AUM(순자산총액), NAV, 운용사, 상장일, 배당주기, 대표섹터 등을 채웁니다.
        """
        from app.services.kis_client import KISClient
        from datetime import datetime

        active_etfs = await self.etf_repository.get_active_etfs()
        if not active_etfs:
            logger.info("메타데이터를 동기화할 활성 ETF가 없습니다.")
            return

        kis_client = KISClient()
        logger.info(f"총 {len(active_etfs)}개 ETF 상세 메타데이터 수집 시작...")

        # KIS etf_dvdn_cycl 코드 → dividend_freq 문자열 매핑
        # (069500 기준: etf_dvdn_cycl=3 → QUARTERLY 확인됨)
        dividend_freq_map = {
            "1": "MONTHLY",
            "3": "QUARTERLY",
            "6": "SEMI_AN",
            "12": "ANNUAL",
            "0": "NONE"
        }

        # 병렬 처리 (semaphore는 KISClient 내부에서 관리)
        import asyncio

        async def fetch_and_update(etf: dict):
            ticker = etf["ticker"]
            etf_id = etf["id"]
            try:
                res = await kis_client.get_etf_basic_info(ticker)
                if not res:
                    return

                info = {}

                # NAV
                if res.get("nav"):
                    try:
                        info["nav"] = float(res["nav"])
                    except ValueError:
                        pass

                # AUM: etf_ntas_ttam 단위 = 억원 → 원으로 변환 (×1억)
                if res.get("etf_ntas_ttam"):
                    try:
                        info["aum"] = int(res["etf_ntas_ttam"]) * 100_000_000
                    except ValueError:
                        pass

                # 운용사 (삼성자산운용(ETF) → 삼성자산운용)
                if res.get("mbcr_name"):
                    info["asset_manager"] = res["mbcr_name"].replace("(ETF)", "").strip()

                # 상장일
                if res.get("stck_lstn_date") and res["stck_lstn_date"] != "0":
                    try:
                        info["listing_date"] = datetime.strptime(res["stck_lstn_date"], "%Y%m%d").date()
                    except ValueError:
                        pass

                # 배당주기
                cycle = str(res.get("etf_dvdn_cycl", "")).strip()
                if cycle:
                    info["dividend_freq"] = dividend_freq_map.get(cycle, "NONE")

                # 대표 섹터 (KOSPI200, KOSDAQ150 등)
                if res.get("etf_rprs_bstp_kor_isnm"):
                    info["sector"] = res["etf_rprs_bstp_kor_isnm"]

                # 카테고리 (ETF(실물복제/수익증권) 등)
                if res.get("bstp_kor_isnm"):
                    info["category"] = res["bstp_kor_isnm"]

                if info:
                    await self.etf_repository.update_etf_advanced_info(etf_id, info)
                    logger.info(f"[{ticker}] 메타데이터 업데이트 완료: {list(info.keys())}")

            except Exception as e:
                logger.error(f"[{ticker}] 메타데이터 동기화 에러: {e}")

        tasks = [fetch_and_update(etf) for etf in active_etfs]
        await asyncio.gather(*tasks, return_exceptions=True)

        await self.etf_repository.db.commit()
        logger.info(f"=== ETF 메타데이터 동기화 완료: {len(active_etfs)}개 처리 ===")