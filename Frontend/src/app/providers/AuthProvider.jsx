import { useCallback, useEffect, useMemo, useState } from "react";
import { api, getToken, setToken } from "../../lib/api";
import { AuthContext } from "./authContext";

export function AuthProvider({ children }) {
    const [me, setMe] = useState(null);
    const [loading, setLoading] = useState(() => Boolean(getToken()));
    const [error, setError] = useState("");

    const refreshUser = useCallback(async () => {
        if (!getToken()) {
            setMe(null);
            return null;
        }

        try {
            const user = await api("GET", "/users/me");
            setMe(user);
            setError("");
            return user;
        } catch (requestError) {
            setToken(null);
            setMe(null);
            setError(requestError.message);
            return null;
        }
    }, []);

    const login = useCallback(async (email, password) => {
        setLoading(true);
        setError("");

        try {
            const { token } = await api("POST", "/auth/login", { email, password });
            if (!token) {
                throw new Error("Serwer nie zwrócił tokenu logowania.");
            }

            setToken(token);
            const user = await api("GET", "/users/me");
            setMe(user);
            return user;
        } catch (requestError) {
            setToken(null);
            setMe(null);
            throw requestError;
        } finally {
            setLoading(false);
        }
    }, []);

    const logout = useCallback(() => {
        setToken(null);
        setMe(null);
        setError("");
    }, []);

    useEffect(() => {
        if (!getToken()) return;

        let active = true;
        Promise.resolve()
            .then(() => (active ? refreshUser() : null))
            .then(() => {
                if (active) setLoading(false);
            });

        return () => {
            active = false;
        };
    }, [refreshUser]);

    const value = useMemo(
        () => ({ me, loading, error, login, logout, refreshUser }),
        [me, loading, error, login, logout, refreshUser],
    );

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
