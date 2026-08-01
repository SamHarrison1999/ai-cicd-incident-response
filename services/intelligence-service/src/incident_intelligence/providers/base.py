from typing import Protocol

from incident_intelligence.models.recommendation import (
    RecommendationRequest,
    RecommendationResponse,
)


class RecommendationProvider(Protocol):
    async def generate(
        self,
        request: RecommendationRequest,
    ) -> RecommendationResponse: ...
