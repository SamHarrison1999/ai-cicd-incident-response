from collections.abc import AsyncIterator
from contextlib import asynccontextmanager

import structlog
from fastapi import FastAPI

from incident_intelligence.api.recommendations import router as recommendations_router
from incident_intelligence.api.system import router as system_router
from incident_intelligence.core.config import Settings, get_settings
from incident_intelligence.core.logging import configure_logging
from incident_intelligence.middleware.correlation import CorrelationIdMiddleware
from incident_intelligence.providers.deterministic import DeterministicProvider


def create_app(settings: Settings | None = None) -> FastAPI:
    resolved_settings = settings or get_settings()
    configure_logging(resolved_settings.log_level)

    @asynccontextmanager
    async def lifespan(app: FastAPI) -> AsyncIterator[None]:
        logger = structlog.get_logger(__name__)
        logger.info(
            "service_started",
            service=resolved_settings.service_name,
            version=resolved_settings.service_version,
            environment=resolved_settings.environment,
            provider=resolved_settings.provider,
        )
        yield
        logger.info("service_stopped", service=resolved_settings.service_name)

    application = FastAPI(
        title="CI/CD Incident Intelligence API",
        description=(
            "Safe, evidence-grounded log analysis and recommendation service. "
            "Logs are treated as untrusted input."
        ),
        version=resolved_settings.service_version,
        lifespan=lifespan,
    )
    application.state.settings = resolved_settings
    application.state.provider = DeterministicProvider(
        provider_version=resolved_settings.deterministic_provider_version
    )
    application.add_middleware(CorrelationIdMiddleware)
    application.include_router(system_router)
    application.include_router(recommendations_router, prefix="/api/v1")

    return application


app = create_app()
