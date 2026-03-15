from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from app.models.stock import Stock
from app.models.company import CompanyInfo

class StockRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_or_create_company(self, corp_name: str, market_category: str = None) -> CompanyInfo:
        result = await self.db.execute(select(CompanyInfo).where(CompanyInfo.company_name == corp_name))
        company = result.scalar_one_or_none()
        if not company:
            company = CompanyInfo(company_name=corp_name, market_type=market_category)
            self.db.add(company)
            await self.db.flush()
        return company

    async def update_company_info(self, company_id: int, info: dict):
        result = await self.db.execute(select(CompanyInfo).where(CompanyInfo.id == company_id))
        company = result.scalar_one_or_none()
        if company:
            if info.get("industry_name"):
                company.industry_name = info["industry_name"]
            if info.get("ceo_name"):
                company.ceo_name = info["ceo_name"]
            if info.get("homepage"):
                company.homepage = info["homepage"]
            if info.get("region"):
                company.region = info["region"]
            if info.get("description"):
                company.description = info["description"]
            await self.db.flush()

    async def get_or_create_stock(self, ticker: str, company_id: int, market_type: str = None) -> Stock:
        result = await self.db.execute(select(Stock).where(Stock.ticker == ticker))
        stock = result.scalar_one_or_none()
        if not stock:
            stock = Stock(ticker=ticker, company_id=company_id, market_type=market_type)
            self.db.add(stock)
            await self.db.flush()
        else:
            if not stock.company_id:
                stock.company_id = company_id
            if market_type and not stock.market_type:
                stock.market_type = market_type
            await self.db.flush()
        return stock
