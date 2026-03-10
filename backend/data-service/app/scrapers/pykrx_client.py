import datetime
from pykrx import stock
from app.scrapers.pykrx_login import KrxSessionManager
from dataclasses import dataclass
import logging

class PykrxClient:
    ASSET_MANAGER = {"TIGER", "KODEX"}
    def __init__(self, krx_session_manager : KrxSessionManager):
        self.krx_session_manager = krx_session_manager

    def get_today_etf_list(self) -> list[EtfInfo]:
        if self.krx_session_manager.login():
            etf_tickers = stock.get_etf_ticker_list(datetime.date.today().strftime("%Y%m%d"))
            etfs = list()
            for etf_ticker in etf_tickers:
                etf_info = stock.get_etf_ticker_name(etf_ticker).split()

                # 회사 및 이름 분리
                etf_manager = etf_info[0]
                etf_name = " ".join(etf_info[1:])

                # TIGER, KODEX 조회
                if etf_manager not in self.ASSET_MANAGER:
                    continue

                etfs.append(EtfInfo(etf_ticker, etf_manager, etf_name))
            return etfs
        else:
            logging.error("로그인 실패")
            return []


@dataclass
class EtfInfo:
    ticker: str
    etf_manager: str
    etf_name: str