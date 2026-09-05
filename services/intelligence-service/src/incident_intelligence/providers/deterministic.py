from incident_intelligence.models.recommendation import (
    ConfidenceLevel,
    ProviderMetadata,
    RecommendationRequest,
    RecommendationResponse,
)


class DeterministicProvider:
    def __init__(self, provider_version: str) -> None:
        self._provider_version = provider_version

    async def generate(
        self,
        request: RecommendationRequest,
    ) -> RecommendationResponse:
        return RecommendationResponse(
            incident_id=request.incident_id,
            summary=(
                "Evidence was received, but the standalone deterministic provider "
                "did not establish a supported cause."
            ),
            likely_cause=None,
            confidence=ConfidenceLevel.LOW,
            supporting_evidence=[],
            alternative_possible_causes=[],
            suggested_next_checks=[
                "Review the supplied evidence in chronological order.",
                "Confirm the failing pipeline stage and deployment environment.",
                "Add or enable a reviewed deterministic diagnostic rule before assigning cause.",
            ],
            abstained=True,
            abstention_reason=(
                "The deterministic provider abstained because the supplied evidence "
                "did not establish a supported, evidence-grounded recommendation."
            ),
            provider_metadata=ProviderMetadata(
                provider="deterministic",
                model="rule-engine",
                version=self._provider_version,
                prompt_version=None,
            ),
        )
