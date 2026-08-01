from datetime import datetime
from typing import Literal

from pydantic import BaseModel, ConfigDict


class HealthResponse(BaseModel):
    model_config = ConfigDict(extra="forbid")

    status: Literal["UP"]


class SystemStatusResponse(BaseModel):
    model_config = ConfigDict(extra="forbid")

    service: str
    version: str
    environment: str
    provider: str
    status: Literal["UP"]
    timestamp: datetime
