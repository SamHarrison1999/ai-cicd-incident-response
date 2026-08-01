# Intelligence Service

The intelligence service is the Python and FastAPI component responsible for safe log analysis.

## Technology baseline

- Python 3.14.
- FastAPI.
- Pydantic 2.
- pydantic-settings.
- structlog.
- uv for dependency and lockfile management.
- Pytest and coverage.py.
- Ruff for linting and formatting.
- MyPy strict mode.

The exact resolved dependency versions are recorded in `uv.lock`.

## Responsibilities

- Log sanitisation.
- Secret redaction.
- Prompt-injection detection.
- Deterministic failure classification.
- Historical incident retrieval.
- Evidence-grounded recommendation generation.
- Confidence scoring and abstention.
- Evaluation execution.
- AI-provider abstraction.

## Foundation behaviour

Batch 3 establishes the API contract and provider abstraction. The deterministic provider deliberately abstains because diagnostic rules are introduced in Phase 7.

The service never treats log text as instructions and does not log request bodies.

## Install

```powershell
python -m pip install --user uv==0.11.29
python -m uv sync
```

## Quality checks

```powershell
python -m uv run ruff format --check .
python -m uv run ruff check .
python -m uv run mypy
python -m uv run pytest
python -m uv lock --check
```

## Run locally

```powershell
python -m uv run uvicorn incident_intelligence.main:app --reload --port 8000
```

Endpoints:

- `GET http://localhost:8000/health/live`
- `GET http://localhost:8000/health/ready`
- `GET http://localhost:8000/api/v1/system/status`
- `POST http://localhost:8000/api/v1/recommendations/generate`
- `GET http://localhost:8000/docs`
- `GET http://localhost:8000/openapi.json`

## Ownership boundary

The service receives bounded analysis requests from the Java control plane and returns structured recommendations. It does not mutate authoritative incident workflow state or perform remediation.
