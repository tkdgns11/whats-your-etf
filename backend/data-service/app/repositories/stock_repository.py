from sqlalchemy.dialects.postgresql import insert
from sqlalchemy.ext.asyncio import AsyncSession
from app.models.stock import Stock

class StockRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def save_tickers(self, tickers: list[str]):
        if not tickers:
            return
        
        # Deduplicate the tickers list to prevent duplicates in the same values list
        unique_tickers = list(set(tickers))
        
        insert_stmt = insert(Stock).values([{"ticker": t} for t in unique_tickers])
        do_nothing_stmt = insert_stmt.on_conflict_do_nothing(index_elements=['ticker'])
        
        await self.db.execute(do_nothing_stmt)
        await self.db.commit()
