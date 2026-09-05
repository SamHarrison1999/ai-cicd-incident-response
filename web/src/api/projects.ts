import { requestJson } from "./httpClient";

export interface Project {
    id: string;
    organisationId: string;
    name: string;
    slug: string;
    description: string | null;
    status: string;
    createdAt: string;
    updatedAt: string;
    version: number;
}

export interface CreateProjectRequest {
    name: string;
    slug: string;
    description: string;
}

export function getProjects(
    accessToken: string,
    organisationId: string,
): Promise<Project[]> {
    return requestJson(
        "/api/v1/organisations/" + organisationId + "/projects",
        {
            headers: {
                Authorization: "Bearer " + accessToken,
            },
        },
    );
}

export function createProject(
    accessToken: string,
    organisationId: string,
    request: CreateProjectRequest,
): Promise<Project> {
    return requestJson(
        "/api/v1/organisations/" + organisationId + "/projects",
        {
            method: "POST",
            headers: {
                Authorization: "Bearer " + accessToken,
            },
            body: JSON.stringify(request),
        },
    );
}
