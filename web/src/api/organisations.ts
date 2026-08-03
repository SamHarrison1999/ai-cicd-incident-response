import { requestJson } from "./httpClient";

export interface Organisation {
    id: string;
    name: string;
    slug: string;
    createdAt: string;
    updatedAt: string;
    version: number;
}

export interface CreateOrganisationRequest {
    name: string;
    slug: string;
}

export function getOrganisations(accessToken: string): Promise<Organisation[]> {
    return requestJson("/api/v1/organisations", {
        headers: {
            Authorization: `Bearer ${accessToken}`,
        },
    });
}

export function createOrganisation(
    accessToken: string,
    request: CreateOrganisationRequest,
): Promise<Organisation> {
    return requestJson("/api/v1/organisations", {
        method: "POST",
        headers: {
            Authorization: `Bearer ${accessToken}`,
        },
        body: JSON.stringify(request),
    });
}
