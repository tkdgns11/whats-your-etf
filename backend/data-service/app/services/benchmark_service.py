import logging
import asyncio
from datetime import date, timedelta
from sqlalchemy.ext.asyncio import AsyncSession
from app.repositories.benchmark_repository import BenchmarkRepository
from app.scrapers.dependencies import get_pykrx_client

logger = logging.getLogger(__name__)

class BenchmarkService:
    def __init__(self, db: AsyncSession):
        self.benchmark_repo = BenchmarkRepository(db)
        self.pykrx_client = get_pykrx_client()

    async def sync_kospi_index(self):
        """KOSPI 벤치마크 지수 동기화"""
        ticker = "1001"  # KOSPI Index Ticker in pykrx
        market_type = "KOSPI"
        from datetime import datetime
        now = datetime.now()
        target_end_date = now.date() if now.hour >= 16 else now.date() - timedelta(days=1)
        
        latest_date = await self.benchmark_repo.get_latest_price_date(market_type)
        if latest_date is None:
            start_date = "20230302"
        else:
            if latest_date >= target_end_date:
                logger.info(f"{market_type} 지수는 이미 최신 상태입니다. (최신 날짜: {latest_date})")
                return
            next_date = latest_date + timedelta(days=1)
            if next_date > target_end_date:
                logger.info(f"{market_type} 지수는 이미 최신 상태입니다.")
                return
            start_date = next_date.strftime("%Y%m%d")
            
        logger.info(f"{market_type} 지수 종가 스크래핑 시작 (start_date={start_date})")
        
        price_histories = await self.pykrx_client.get_index_price_history(
            ticker, 
            start_date=start_date,
            end_date=target_end_date.strftime("%Y%m%d")
        )

        if not price_histories:
            logger.info(f"{market_type} 새로운 가격 데이터가 없습니다.")
            return

        for history in price_histories:
            history['market_type'] = market_type
            history['created_at'] = now
            history['updated_at'] = now

        logging.info(f"[{market_type}] DB에 가격 이력 데이터 bulk insert 진행 중... ({len(price_histories)}건)")
        await self.benchmark_repo.save_bulk(price_histories)
        logging.info(f"[{market_type}] DB 업데이트 완료.")
