from pydantic_settings import BaseSettings
from functools import lru_cache


class Settings(BaseSettings):
    # Database
    database_url: str = "postgresql+psycopg://wye:wye1234@localhost:5432/whatsyouretf"

    # News Scraping
    news_scrape_interval_minutes: int = 10
    news_max_per_keyword: int = 5

    # Naver API (optional)
    naver_client_id: str = ""
    naver_client_secret: str = ""

    # OpenAI API
    openai_api_key: str = ""
    openai_model: str = "gpt-4o-mini"  # gpt-4o, gpt-4o-mini, gpt-3.5-turbo
    openai_max_tokens: int = 1000
    openai_temperature: float = 0.3

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"


@lru_cache()
def get_settings() -> Settings:
    return Settings()
