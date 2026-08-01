from fastapi import APIRouter, Request, status

from incident_intelligence.models.recommendation import (
    RecommendationRequest,
    RecommendationResponse,
)
from incident_intelligence.providers.base import RecommendationProvider

router = APIRouter(prefix="/recommendations", tags=["recommendations"])


@router.post(
    "/generate",
    response_model=RecommendationResponse,
    status_code=status.HTTP_200_OK,
)
async def generate_recommendation(
    payload: RecommendationRequest,
    request: Request,
) -> RecommendationResponse:
    provider: RecommendationProvider = request.app.state.provider
    return await provider.generate(payload)
