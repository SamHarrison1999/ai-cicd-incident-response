from datetime import UTC, datetime

from fastapi import APIRouter, Request

from incident_intelligence.core.config import Settings
from incident_intelligence.models.system import HealthResponse, SystemStatusResponse

router = APIRouter(tags=["system"])


@router.get("/health/live", response_model=HealthResponse)
async def liveness() -> HealthResponse:
    return HealthResponse(status="UP")


@router.get("/health/ready", response_model=HealthResponse)
async def readiness() -> HealthResponse:
    return HealthResponse(status="UP")


@router.get("/api/v1/system/status", response_model=SystemStatusResponse)
async def system_status(request: Request) -> SystemStatusResponse:
    settings: Settings = request.app.state.settings
    return SystemStatusResponse(
        service=settings.service_name,
        version=settings.service_version,
        environment=settings.environment,
        provider=settings.provider,
        status="UP",
        timestamp=datetime.now(UTC),
    )
