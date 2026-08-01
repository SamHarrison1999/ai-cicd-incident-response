from uuid import UUID

from fastapi.testclient import TestClient

from incident_intelligence.middleware.correlation import CORRELATION_ID_HEADER


def test_preserves_valid_correlation_id(client: TestClient) -> None:
    expected = "pipeline-run:phase-1-batch-3"

    response = client.get(
        "/health/live",
        headers={CORRELATION_ID_HEADER: expected},
    )

    assert response.headers[CORRELATION_ID_HEADER] == expected


def test_generates_id_when_header_is_missing(client: TestClient) -> None:
    response = client.get("/health/live")

    assert UUID(response.headers[CORRELATION_ID_HEADER])


def test_replaces_unsafe_correlation_id(client: TestClient) -> None:
    response = client.get(
        "/health/live",
        headers={CORRELATION_ID_HEADER: "unsafe value"},
    )

    assert response.headers[CORRELATION_ID_HEADER] != "unsafe value"
    assert UUID(response.headers[CORRELATION_ID_HEADER])
