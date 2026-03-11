from pydantic_settings import BaseSettings, SettingsConfigDict
from functools import lru_cache


class Settings(BaseSettings):
    # Database
    db_host: str = ""
    db_port: str = ""
    db_name: str = ""
    db_user: str = ""
    db_password: str = ""
    
    # News Scraping
    news_scrape_interval_minutes: int = 10
    news_max_per_keyword: int = 5

    # Naver API (optional)
    naver_client_id: str = ""
    naver_client_secret: str = ""

    # SSAFY GMS API (Anthropic)
    gms_api_key: str = ""
    gms_base_url: str = "https://gms.ssafy.io/gmsapi/api.anthropic.com"
    gms_model: str = "claude-sonnet-4-20250514"  # 포트폴리오 등 복잡한 분석용
    gms_model_light: str = "claude-3-haiku-20240307"  # 뉴스 분석 등 간단한 작업용
    gms_max_tokens: int = 4096
    gms_temperature: float = 0.3

    # Anthropic API (직접 호출용)
    anthropic_api_key: str = ""

    # OpenAI API (직접 호출용)
    openai_api_key: str = ""

    krx_id: str = ""
    krx_pw: str = ""

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8"
    )

    @property
    def database_url(self) -> str:
        return (
            f"postgresql+psycopg://{self.db_user}:{self.db_password}"
            f"@{self.db_host}:{self.db_port}/{self.db_name}"
        )


@lru_cache()
def get_settings() -> Settings:
    return Settings()
