/* eslint-disable react-refresh/only-export-components */
import {
    createContext,
    type PropsWithChildren,
    useCallback,
    useContext,
    useEffect,
    useMemo,
    useState,
} from "react";

import {
    type AuthenticationResponse,
    type CurrentUser,
    loginUser,
    logoutUser,
    refreshSession,
    registerUser,
    type RegisterRequest,
} from "../api/authentication";

interface AuthState {
    accessToken: string | null;
    currentUser: CurrentUser | null;
    isInitialising: boolean;
}

interface AuthContextValue extends AuthState {
    login(email: string, password: string): Promise<void>;
    register(request: RegisterRequest): Promise<void>;
    logout(): Promise<void>;
}

export const AuthContext = createContext<AuthContextValue | undefined>(
    undefined,
);

export function AuthProvider({ children }: PropsWithChildren) {
    const [state, setState] = useState<AuthState>({
        accessToken: null,
        currentUser: null,
        isInitialising: true,
    });

    const applyAuthentication = useCallback(
        (authentication: AuthenticationResponse) => {
            setState({
                accessToken: authentication.accessToken,
                currentUser: authentication.user,
                isInitialising: false,
            });
        },
        [],
    );

    useEffect(() => {
        let active = true;

        refreshSession()
            .then((authentication) => {
                if (active) {
                    applyAuthentication(authentication);
                }
            })
            .catch(() => {
                if (active) {
                    setState({
                        accessToken: null,
                        currentUser: null,
                        isInitialising: false,
                    });
                }
            });

        return () => {
            active = false;
        };
    }, [applyAuthentication]);

    const login = useCallback(
        async (email: string, password: string) => {
            applyAuthentication(await loginUser({ email, password }));
        },
        [applyAuthentication],
    );

    const register = useCallback(async (request: RegisterRequest) => {
        await registerUser(request);
    }, []);

    const logout = useCallback(async () => {
        try {
            await logoutUser();
        } finally {
            setState({
                accessToken: null,
                currentUser: null,
                isInitialising: false,
            });
        }
    }, []);

    const value = useMemo<AuthContextValue>(
        () => ({
            ...state,
            login,
            register,
            logout,
        }),
        [login, logout, register, state],
    );

    return (
        <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
    );
}

export function useAuth(): AuthContextValue {
    const context = useContext(AuthContext);

    if (context === undefined) {
        throw new Error("useAuth must be used inside AuthProvider.");
    }

    return context;
}
