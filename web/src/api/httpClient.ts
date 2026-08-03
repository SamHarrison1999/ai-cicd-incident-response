export class ApiError extends Error {
    public readonly status: number;
    public readonly code: string;

    public constructor(status: number, code: string, message: string) {
        super(message);
        this.name = "ApiError";
        this.status = status;
        this.code = code;
    }
}

interface ErrorPayload {
    code?: string;
    message?: string;
}

async function readErrorPayload(response: Response): Promise<ErrorPayload> {
    try {
        return (await response.json()) as ErrorPayload;
    } catch {
        return {};
    }
}

export async function requestJson<T>(
    path: string,
    init: RequestInit = {},
): Promise<T> {
    const headers = new Headers(init.headers);
    headers.set("Accept", "application/json");
    headers.set("Content-Type", "application/json");

    const response = await fetch(`/control-plane${path}`, {
        ...init,
        credentials: "include",
        headers,
    });

    if (!response.ok) {
        const payload = await readErrorPayload(response);

        throw new ApiError(
            response.status,
            payload.code ?? "HTTP_ERROR",
            payload.message ??
                `Request failed with HTTP ${String(response.status)}`,
        );
    }

    if (response.status === 204) {
        return undefined as T;
    }

    return (await response.json()) as T;
}
