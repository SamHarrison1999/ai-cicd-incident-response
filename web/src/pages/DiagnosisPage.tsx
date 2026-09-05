import { useQuery } from "@tanstack/react-query";
import { getDiagnosis } from "../api/diagnosis";
import { useAuth } from "../auth/useAuth";
import { useWorkspace } from "../workspace/useWorkspace";

function formatConfidence(value: number) {
    return String(Math.round(value * 100)) + "%";
}

export function DiagnosisPage() {
    const { accessToken } = useAuth();
    const { organisationId, projectId, setOrganisationId, setProjectId } =
        useWorkspace();
    const canQuery =
        organisationId.trim().length > 0 && projectId.trim().length > 0;
    const diagnosis = useQuery({
        queryKey: ["diagnosis", organisationId, projectId],
        queryFn: () =>
            getDiagnosis(accessToken ?? "", organisationId, projectId),
        enabled: canQuery && accessToken !== null,
    });

    return (
        <>
            <section className="page-heading">
                <p className="eyebrow">Decision support</p>
                <h2>Diagnosis</h2>
                <p>
                    Review bounded, deterministic hypotheses from sanitised
                    evidence. Results are not confirmed causes.
                </p>
            </section>
            <section
                className="settings-panel"
                aria-labelledby="diagnosis-scope-heading"
            >
                <h3 id="diagnosis-scope-heading">Workspace scope</h3>
                <div className="scope-form">
                    <label>
                        Organisation ID
                        <input
                            value={organisationId}
                            onChange={(event) => {
                                setOrganisationId(event.target.value);
                            }}
                        />
                    </label>
                    <label>
                        Project ID
                        <input
                            value={projectId}
                            onChange={(event) => {
                                setProjectId(event.target.value);
                            }}
                        />
                    </label>
                </div>
                {!canQuery ? (
                    <p className="field-hint">
                        Enter both IDs to load the tenant-scoped diagnosis.
                    </p>
                ) : null}
            </section>
            {diagnosis.isPending && canQuery ? (
                <p>Loading diagnosis...</p>
            ) : null}
            {diagnosis.isError ? (
                <div className="notice notice-error" role="alert">
                    Diagnosis could not be loaded.
                </div>
            ) : null}
            {diagnosis.data ? (
                <section
                    className="diagnosis-panel"
                    aria-labelledby="diagnosis-result-heading"
                >
                    <div className="section-heading">
                        <div>
                            <p className="eyebrow">Human review required</p>
                            <h3 id="diagnosis-result-heading">
                                {diagnosis.data.category}
                            </h3>
                        </div>
                        <span className="diagnosis-confidence">
                            {formatConfidence(diagnosis.data.confidence)}
                        </span>
                    </div>
                    <dl className="diagnosis-detail-list">
                        <div>
                            <dt>Rule version</dt>
                            <dd>{diagnosis.data.ruleVersion}</dd>
                        </div>
                        <div>
                            <dt>Supporting signals</dt>
                            <dd>{diagnosis.data.supportingSignalIds.length}</dd>
                        </div>
                        <div>
                            <dt>Abstention</dt>
                            <dd>{diagnosis.data.abstentionReason ?? "None"}</dd>
                        </div>
                    </dl>
                    {diagnosis.data.missingEvidence.length > 0 ? (
                        <p className="diagnosis-callout">
                            Missing evidence:{" "}
                            {diagnosis.data.missingEvidence.join(", ")}
                        </p>
                    ) : null}
                    {diagnosis.data.warnings.length > 0 ? (
                        <p className="diagnosis-callout">
                            Safety warnings:{" "}
                            {diagnosis.data.warnings.join(", ")}
                        </p>
                    ) : null}
                </section>
            ) : null}
        </>
    );
}
