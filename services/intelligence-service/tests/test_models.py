from uuid import uuid4

import pytest
from pydantic import ValidationError

from incident_intelligence.models.recommendation import (
    ConfidenceLevel,
    ProviderMetadata,
    RecommendationResponse,
)


def test_non_abstained_recommendation_requires_evidence() -> None:
    with pytest.raises(ValidationError):
        RecommendationResponse(
            incident_id=uuid4(),
            summary="A cause was identified.",
            likely_cause="Database connection exhaustion",
            confidence=ConfidenceLevel.HIGH,
            supporting_evidence=[],
            alternative_possible_causes=[],
            suggested_next_checks=["Inspect connection-pool metrics."],
            abstained=False,
            abstention_reason=None,
            provider_metadata=ProviderMetadata(
                provider="deterministic",
                model="rule-engine",
                version="test",
            ),
        )


def test_abstained_recommendation_cannot_declare_likely_cause() -> None:
    with pytest.raises(ValidationError):
        RecommendationResponse(
            incident_id=uuid4(),
            summary="Insufficient evidence.",
            likely_cause="Invented cause",
            confidence=ConfidenceLevel.LOW,
            supporting_evidence=[],
            alternative_possible_causes=[],
            suggested_next_checks=[],
            abstained=True,
            abstention_reason="Insufficient evidence.",
            provider_metadata=ProviderMetadata(
                provider="deterministic",
                model="rule-engine",
                version="test",
            ),
        )
