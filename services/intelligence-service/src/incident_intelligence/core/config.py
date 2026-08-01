from functools import lru_cache
from typing import Literal

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_prefix="INTELLIGENCE_",
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )

    service_name: str = "intelligence-service"
    service_version: str = "0.1.0"
    environment: Literal["local", "test", "production"] = "local"
    log_level: str = "INFO"
    provider: Literal["deterministic"] = "deterministic"
    deterministic_provider_version: str = "foundation-1"
    max_evidence_items: int = Field(default=100, ge=1, le=500)


@lru_cache
def get_settings() -> Settings:
    return Settings()
