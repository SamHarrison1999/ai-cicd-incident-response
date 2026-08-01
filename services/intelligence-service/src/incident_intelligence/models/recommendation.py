from enum import StrEnum
from uuid import UUID

from pydantic import BaseModel, ConfigDict, Field, model_validator


class ConfidenceLevel(StrEnum):
    LOW = "LOW"
    MEDIUM = "MEDIUM"
    HIGH = "HIGH"


class ReviewStatus(StrEnum):
    PENDING = "PENDING"
    ACCEPTED = "ACCEPTED"
    EDITED = "EDITED"
    REJECTED = "REJECTED"


class EvidenceItem(BaseModel):
    model_config = ConfigDict(extra="forbid", str_strip_whitespace=True)

    evidence_id: str = Field(min_length=1, max_length=200)
    source: str = Field(min_length=1, max_length=200)
    line_start: int = Field(ge=1)
    line_end: int = Field(ge=1)
    content: str = Field(min_length=1, max_length=4000)

    @model_validator(mode="after")
    def validate_line_range(self) -> EvidenceItem:
        if self.line_end < self.line_start:
            raise ValueError("line_end must be greater than or equal to line_start")
        return self


class RecommendationRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")

    incident_id: UUID
    project_id: UUID
    pipeline_run_id: UUID | None = None
    untrusted_log_evidence: list[EvidenceItem] = Field(min_length=1, max_length=100)


class EvidenceCitation(BaseModel):
    model_config = ConfigDict(extra="forbid")

    evidence_id: str
    line_start: int = Field(ge=1)
    line_end: int = Field(ge=1)
    rationale: str = Field(min_length=1, max_length=500)


class ProviderMetadata(BaseModel):
    model_config = ConfigDict(extra="forbid")

    provider: str
    model: str
    version: str
    prompt_version: str | None = None


class RecommendationResponse(BaseModel):
    model_config = ConfigDict(extra="forbid")

    incident_id: UUID
    summary: str = Field(min_length=1, max_length=2000)
    likely_cause: str | None = Field(default=None, max_length=2000)
    confidence: ConfidenceLevel
    supporting_evidence: list[EvidenceCitation]
    alternative_possible_causes: list[str]
    suggested_next_checks: list[str]
    abstained: bool
    abstention_reason: str | None = Field(default=None, max_length=1000)
    human_review_status: ReviewStatus = ReviewStatus.PENDING
    provider_metadata: ProviderMetadata

    @model_validator(mode="after")
    def validate_abstention_contract(self) -> RecommendationResponse:
        if self.abstained:
            if self.likely_cause is not None:
                raise ValueError("an abstained recommendation cannot declare a likely cause")
            if self.supporting_evidence:
                raise ValueError("an abstained recommendation cannot include supporting evidence")
            if not self.abstention_reason:
                raise ValueError("an abstained recommendation requires an abstention reason")
        elif not self.supporting_evidence:
            raise ValueError("a non-abstained recommendation requires supporting evidence")

        return self
