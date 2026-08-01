import re
import time
import uuid

import structlog
from starlette.datastructures import Headers, MutableHeaders
from starlette.types import ASGIApp, Message, Receive, Scope, Send

CORRELATION_ID_HEADER = "X-Correlation-ID"
CORRELATION_ID_CONTEXT_KEY = "correlation_id"
_ALLOWED_CORRELATION_ID = re.compile(r"^[A-Za-z0-9._:-]{1,128}$")


class CorrelationIdMiddleware:
    def __init__(self, app: ASGIApp) -> None:
        self.app = app

    async def __call__(self, scope: Scope, receive: Receive, send: Send) -> None:
        if scope["type"] != "http":
            await self.app(scope, receive, send)
            return

        headers = Headers(scope=scope)
        supplied_value = headers.get(CORRELATION_ID_HEADER)
        correlation_id = resolve_correlation_id(supplied_value)
        started_at = time.perf_counter()
        status_code = 500

        structlog.contextvars.clear_contextvars()
        structlog.contextvars.bind_contextvars(**{CORRELATION_ID_CONTEXT_KEY: correlation_id})

        async def send_with_correlation_id(message: Message) -> None:
            nonlocal status_code
            if message["type"] == "http.response.start":
                status_code = message["status"]
                response_headers = MutableHeaders(scope=message)
                response_headers[CORRELATION_ID_HEADER] = correlation_id
            await send(message)

        try:
            await self.app(scope, receive, send_with_correlation_id)
        finally:
            duration_ms = round((time.perf_counter() - started_at) * 1000, 3)
            structlog.get_logger(__name__).info(
                "request_completed",
                method=scope.get("method"),
                path=scope.get("path"),
                status_code=status_code,
                duration_ms=duration_ms,
            )
            structlog.contextvars.clear_contextvars()


def resolve_correlation_id(supplied_value: str | None) -> str:
    if supplied_value is not None:
        candidate = supplied_value.strip()
        if _ALLOWED_CORRELATION_ID.fullmatch(candidate):
            return candidate

    return str(uuid.uuid4())
