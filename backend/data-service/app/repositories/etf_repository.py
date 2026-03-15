from sqlalchemy import select, insert
from sqlalchemy.ext.asyncio import AsyncSession

from app.models import ETF, ETFPrice
from app.scrapers.pykrx_client import EtfInfo


class EtfRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    # db에 적재 중인 etf 목록
    async def get_etf_tickers(self) -> list[str]:
        stmt = select(ETF.stock_code)
        result = await self.db.execute(stmt)
        return list(result.scalars().all())

    async def save_initial_etf_infos(self, etf_infos: list[EtfInfo]) -> list[dict]:
        if not etf_infos:
            return []

        rows = [
            {
                "stock_code" : etf.ticker,
                "name" : etf.etf_name,
                "asset_manager" : etf.etf_manager
            }
            for etf in etf_infos
        ]

        await self.db.execute(insert(ETF), rows)
        await self.db.flush()

        tickers = [etf_info.ticker for etf_info in etf_infos]

        result = await self.db.execute(
            select(ETF.id, ETF.stock_code).where(ETF.stock_code.in_(tickers))
        )
        await self.db.commit()

        return [{"id": row.id, "ticker":row.stock_code} for row in result.all()]


class EtfPriceRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def save_bulk(self, price_histories):
        if not price_histories:
            return

            # SQLAlchemy 2.0 Core 스타일의 Bulk Insert
        stmt = insert(ETFPrice)
        await self.db.execute(stmt, price_histories)
        await self.db.commit()
