export interface SystemStatus {
    service: string;
    version: string;
    status: "UP";
    timestamp: string;
}

const controlPlaneBaseUrl = "/control-plane";

export async function getControlPlaneStatus(
    signal?: AbortSignal,
): Promise<SystemStatus> {
    const response = await fetch(
        `${controlPlaneBaseUrl}/api/v1/system/status`,
        {
            headers: {
                Accept: "application/json",
            },
            signal,
        },
    );

    if (!response.ok) {
        throw new Error(
            `Control plane status request failed with HTTP ${String(response.status)}`,
        );
    }

    return (await response.json()) as SystemStatus;
}
