import httpx
import logging
import asyncio
import os
import time
from typing import Optional, Dict, Any

from app.config import get_settings

logger = logging.getLogger(__name__)
settings = get_settings()

class KISClient:
    """한국투자증권 Open API 연동 클라이언트"""
    
    BASE_URL = "https://openapi.koreainvestment.com:9443"
    TOKEN_FILE = "/tmp/kis_token.txt"
    
    def __init__(self):
        self.app_key = settings.kis_app_key
        self.app_secret = settings.kis_app_secret
        self._access_token: Optional[str] = None
        
        # 초당 최대 18건 병렬 호출 제한을 위한 세마포어
        self.semaphore = asyncio.Semaphore(18)

    async def _get_access_token(self) -> str:
        """액세스 토큰 발급 및 로컬 파일 캐싱 (24시간 유효)"""
        # 메모리 캐시 확인
        if self._access_token:
            return self._access_token
            
        # 파일 캐시 확인 (1일 = 86400초, 안전하게 86000초 기준)
        if os.path.exists(self.TOKEN_FILE):
            if time.time() - os.path.getmtime(self.TOKEN_FILE) < 86000:
                with open(self.TOKEN_FILE, "r") as f:
                    cached_token = f.read().strip()
                if cached_token:
                    self._access_token = cached_token
                    return self._access_token
                    
        # 신규 발급 (1분당 1회 Limit 주의)
        logger.info("KIS API 토큰 신규 발급 요청 중...")
        url = f"{self.BASE_URL}/oauth2/tokenP"
        payload = {
            "grant_type": "client_credentials",
            "appkey": self.app_key,
            "appsecret": self.app_secret
        }
        
        async with httpx.AsyncClient(timeout=10.0) as client:
            try:
                res = await client.post(url, json=payload)
                res.raise_for_status()
                data = res.json()
                
                token = data.get("access_token")
                if not token:
                    raise ValueError("응답에 access_token이 없습니다.")
                    
                self._access_token = token
                # 파일에 저장
                with open(self.TOKEN_FILE, "w") as f:
                    f.write(token)
                return token
            except Exception as e:
                logger.error(f"KIS 토큰 발급 실패: {e}")
                raise e

    async def get_etf_constituents(self, ticker: str) -> Optional[Dict[str, Any]]:
        """
        FHKST121600C0: ETF 구성종목시세 API 호출
        반환값: { "output1": ETF본체정보(dict), "output2": 구성종목리스트(list) }
        """
        if not self.app_key or not self.app_secret:
            logger.warning("KIS API 키가 설정되지 않아 호출을 스킵합니다.")
            return None
            
        token = await self._get_access_token()
        url = f"{self.BASE_URL}/uapi/etfetn/v1/quotations/inquire-component-stock-price"
        headers = {
            "content-type": "application/json; charset=utf-8",
            "authorization": f"Bearer {token}",
            "appkey": self.app_key,
            "appsecret": self.app_secret,
            "tr_id": "FHKST121600C0",
            "custtype": "P"
        }
        params = {
            "fid_cond_mrkt_div_code": "J",
            "fid_input_iscd": ticker,
            "fid_cond_scr_div_code": 11216
        }
        
        async with self.semaphore:
            # Rate limit 완화를 위한 미세한 대기
            await asyncio.sleep(1.0 / 18.0) 
            
            async with httpx.AsyncClient(timeout=10.0) as client:
                try:
                    res = await client.get(url, headers=headers, params=params)
                    res.raise_for_status()
                    data = res.json()
                    
                    if data.get("rt_cd") != "0":
                        logger.error(f"[{ticker}] KIS API 에러 응답: {data.get('msg1')}")
                        return None
                        
                    return {
                        "output1": data.get("output1", {}),
                        "output2": data.get("output2", [])
                    }
                except Exception as e:
                    logger.error(f"[{ticker}] KIS API 호출 실패: {e}")
                    return None

    async def get_etf_basic_info(self, ticker: str) -> Optional[Dict[str, Any]]:
        """
        FHPST02400000: ETF(ETN) 현재가/기본정보 조회 API 호출
        - nav, etf_ntas_ttam(AUM), mbcr_name(운용사), stck_lstn_date(상장일),
          etf_dvdn_cycl(배당주기), etf_rprs_bstp_kor_isnm(대표섹터) 등 포함
        """
        if not self.app_key or not self.app_secret:
            return None

        token = await self._get_access_token()
        url = f"{self.BASE_URL}/uapi/domestic-stock/v1/quotations/inquire-price"
        headers = {
            "content-type": "application/json; charset=utf-8",
            "authorization": f"Bearer {token}",
            "appkey": self.app_key,
            "appsecret": self.app_secret,
            "tr_id": "FHPST02400000",
            "custtype": "P"
        }
        params = {
            "FID_COND_MRKT_DIV_CODE": "J",
            "FID_INPUT_ISCD": ticker
        }

        async with self.semaphore:
            await asyncio.sleep(1.0 / 18.0)
            async with httpx.AsyncClient(timeout=10.0) as client:
                try:
                    res = await client.get(url, headers=headers, params=params)
                    res.raise_for_status()
                    data = res.json()

                    if data.get("rt_cd") != "0":
                        logger.error(f"[{ticker}] FHPST02400000 에러: {data.get('msg1')}")
                        return None

                    return data.get("output", {})
                except Exception as e:
                    logger.error(f"[{ticker}] FHPST02400000 호출 실패: {e}")
                    return None
