import { useQuery } from "@tanstack/react-query";

import { getControlPlaneStatus } from "../api/controlPlane";

export function useControlPlaneStatus() {
    return useQuery({
        queryKey: ["control-plane", "system-status"],
        queryFn: ({ signal }) => getControlPlaneStatus(signal),
    });
}
