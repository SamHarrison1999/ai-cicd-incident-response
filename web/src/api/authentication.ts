import { requestJson } from "./httpClient";

export interface CurrentUser {
    userId: string;
    email: string;
    displayName: string;
}

export interface AuthenticationResponse {
    accessToken: string;
    tokenType: "Bearer";
    expiresInSeconds: number;
    user: CurrentUser;
}

export interface RegisterRequest {
    email: string;
    displayName: string;
    password: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export async function registerUser(request: RegisterRequest): Promise<void> {
    await requestJson("/api/v1/auth/register", {
        method: "POST",
        body: JSON.stringify(request),
    });
}

export function loginUser(
    request: LoginRequest,
): Promise<AuthenticationResponse> {
    return requestJson("/api/v1/auth/login", {
        method: "POST",
        body: JSON.stringify(request),
    });
}

export function refreshSession(): Promise<AuthenticationResponse> {
    return requestJson("/api/v1/auth/refresh", {
        method: "POST",
    });
}

export async function logoutUser(): Promise<void> {
    await requestJson("/api/v1/auth/logout", {
        method: "POST",
    });
}
