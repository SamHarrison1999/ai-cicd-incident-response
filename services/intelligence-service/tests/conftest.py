from collections.abc import Iterator

import pytest
from fastapi.testclient import TestClient

from incident_intelligence.core.config import Settings
from incident_intelligence.main import create_app


@pytest.fixture
def client() -> Iterator[TestClient]:
    settings = Settings(
        environment="test",
        log_level="WARNING",
        deterministic_provider_version="test-foundation-1",
    )
    with TestClient(create_app(settings)) as test_client:
        yield test_client
