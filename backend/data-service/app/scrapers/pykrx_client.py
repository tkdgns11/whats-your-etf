import datetime
from typing import Any, List, Dict

from anyio import to_thread
from pykrx import stock
from app.scrapers.pykrx_login import KrxSessionManager
from dataclasses import dataclass
import logging

class PykrxClient:
    ASSET_MANAGER = {"TIGER", "KODEX"}
    def __init__(self, krx_session_manager : KrxSessionManager):
        self.krx_session_manager = krx_session_manager

    async def get_today_etf_list(self) -> list[EtfInfo]:
        if not self.krx_session_manager.login():
            logging.error("로그인 실패")
            return []

        # 1. 데이터 취득 (현재 날짜 기준 티커 리스트)
        today_str = datetime.date.today().strftime("%Y%m%d")
        etf_tickers = stock.get_etf_ticker_list(today_str)

        etfs = []
        for etf_ticker in etf_tickers:
            etf_full_name = stock.get_etf_ticker_name(etf_ticker)
            name_parts = etf_full_name.split()

            if not name_parts:
                continue

            # A. 운용사 필터링 (기존 로직)
            etf_manager = name_parts[0]
            if etf_manager not in self.ASSET_MANAGER:
                continue

            etfs.append(EtfInfo(etf_ticker, etf_manager, etf_full_name))

        return etfs

    async def get_price_history(self, ticker: str, start_date: str = "20230302") -> List[Dict[str, Any]]:
        if self.krx_session_manager.login():
            """
            pykrx로부터 데이터를 가져와 스키마 구조에 맞는 List[Dict]로 변환합니다.
            """
            end_date = datetime.date.today().strftime("%Y%m%d")

            # pykrx의 ETF OHLCV API는 NAV와 등락률을 포함합니다.
            df = await to_thread.run_sync(
                stock.get_etf_ohlcv_by_date, start_date, end_date, ticker
            )

            if df is None or df.empty:
                return []

            df = df.reset_index()
            # 컬럼 매핑: [날짜, 시가, 고가, 저가, 종가, 거래량, 거래대금, 등락률, NAV]
            # 우리 스키마에 필요한 것: trade_date, close, nav, volume, change_rate

            history_data = []
            for _, row in df.iterrows():
                history_data.append({
                    "trade_date": row['날짜'].date(),
                    "close": float(row['종가']),
                    "nav": float(row['NAV']),
                    "volume": int(row['거래량']),
                    "change_rate": float(row['등락률'])
                })

            return history_data
        else:
            logging.error("로그인 실패")
            return []


@dataclass
class EtfInfo:
    ticker: str
    etf_manager: str
    etf_name: str