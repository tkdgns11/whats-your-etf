from pydantic_settings import BaseSettings, SettingsConfigDict
from functools import lru_cache
from urllib.parse import quote_plus


class Settings(BaseSettings):
    # Database (환경변수 기반)
    db_host: str = "localhost"
    db_port: int = 5432
    db_name: str = "whatsyouretf"
    db_user: str = "wye"
    db_password: str = ""

    # DATABASE_URL 직접 지정 시 사용 (로컬 개발용)
    database_url: str | None = None

    @property
    def get_database_url(self) -> str:
        """비밀번호 특수문자 URL 인코딩 처리"""
        if self.database_url:
            return self.database_url
        encoded_password = quote_plus(self.db_password)
        return f"postgresql+psycopg://{self.db_user}:{encoded_password}@{self.db_host}:{self.db_port}/{self.db_name}"

    # News Scraping
    news_scrape_interval_minutes: int = 10
    news_max_per_keyword: int = 5

    # Naver API (optional)
    naver_client_id: str = ""
    naver_client_secret: str = ""

    # SSAFY GMS API (Anthropic)
    gms_api_key: str = ""
    gms_base_url: str = "https://gms.ssafy.io/gmsapi/api.anthropic.com"
    gms_model: str = "claude-sonnet-4-20250514"
    gms_model_light: str = "claude-3-haiku-20240307"
    gms_max_tokens: int = 4096
    gms_temperature: float = 0.3

    # Anthropic API
    anthropic_api_key: str = ""

    # OpenAI API
    openai_api_key: str = ""

    # KRX Login
    krx_id: str = ""
    krx_pw: str = ""

    # Data Portal API
    data_portal_company_service_key: str = ""

    # KIS API (한국투자증권)
    kis_app_key: str = ""
    kis_app_secret: str = ""

    # RabbitMQ
    rabbitmq_host: str = "localhost"
    rabbitmq_port: int = 5672
    rabbitmq_user: str = "guest"
    rabbitmq_password: str = "guest"

    # Redis
    redis_host: str = "localhost"
    redis_port: int = 6379
    redis_password: str = ""
    redis_db: int = 0

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8"
    )


@lru_cache()
def get_settings() -> Settings:
    return Settings()
