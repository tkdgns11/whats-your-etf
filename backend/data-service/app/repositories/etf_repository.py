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

        rows = []
        for etf in etf_infos:
            is_lev = "레버리지" in etf.etf_name
            is_inv = "인버스" in etf.etf_name
            
            rows.append({
                "stock_code" : etf.ticker,
                "name" : etf.etf_name,
                "asset_manager" : etf.etf_manager,
                "is_active": False,
                "is_leveraged": is_lev,
                "is_inverse": is_inv,
                "is_derivatives": is_lev or is_inv,
                "is_krx_only": None
            })

        await self.db.execute(insert(ETF), rows)
        await self.db.flush()

        tickers = [etf_info.ticker for etf_info in etf_infos]

        result = await self.db.execute(
            select(ETF.id, ETF.stock_code).where(ETF.stock_code.in_(tickers))
        )
        await self.db.commit()

        return [{"id": row.id, "ticker":row.stock_code} for row in result.all()]

    async def update_krx_status(self, etf_id: int, is_krx_only: bool):
        from sqlalchemy import update
        stmt = update(ETF).where(ETF.id == etf_id).values(is_krx_only=is_krx_only, is_active=is_krx_only)
        await self.db.execute(stmt)
        await self.db.commit()

    async def get_unchecked_etfs(self) -> list[dict]:
        stmt = select(ETF.id, ETF.stock_code, ETF.name).where(ETF.is_krx_only.is_(None))
        result = await self.db.execute(stmt)
        return [{"id": row.id, "ticker": row.stock_code, "name": row.name} for row in result.all()]

    async def get_all_etfs(self) -> list[dict]:
        stmt = select(ETF.id, ETF.stock_code, ETF.name)
        result = await self.db.execute(stmt)
        return [{"id": row.id, "ticker": row.stock_code, "name": row.name} for row in result.all()]

    async def get_active_etfs(self) -> list[dict]:
        stmt = select(ETF.id, ETF.stock_code).where(ETF.is_active == True)
        result = await self.db.execute(stmt)
        return [{"id": row.id, "ticker": row.stock_code} for row in result.all()]



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

    async def get_latest_price_date(self, etf_id: int):
        from sqlalchemy import func
        stmt = select(func.max(ETFPrice.trade_date)).where(ETFPrice.etf_id == etf_id)
        result = await self.db.execute(stmt)
        return result.scalar()
