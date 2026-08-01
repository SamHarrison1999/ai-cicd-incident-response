from fastapi.testclient import TestClient


def test_liveness(client: TestClient) -> None:
    response = client.get("/health/live")

    assert response.status_code == 200
    assert response.json() == {"status": "UP"}


def test_readiness(client: TestClient) -> None:
    response = client.get("/health/ready")

    assert response.status_code == 200
    assert response.json() == {"status": "UP"}


def test_system_status(client: TestClient) -> None:
    response = client.get("/api/v1/system/status")

    assert response.status_code == 200
    body = response.json()
    assert body["service"] == "intelligence-service"
    assert body["version"] == "0.1.0"
    assert body["environment"] == "test"
    assert body["provider"] == "deterministic"
    assert body["status"] == "UP"
    assert "timestamp" in body
