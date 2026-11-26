const TOKEN_KEY = "token";

export function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token) {
    if (token) localStorage.setItem(TOKEN_KEY, token);
    else localStorage.removeItem(TOKEN_KEY);
}

export async function api(method, path, body) {
    const headers = {};
    const token = getToken();

    if (token) headers.Authorization = `Bearer ${token}`;

    if (body !== undefined) {
        headers["Content-Type"] = "application/json";
    }

    const response = await fetch(`/api${path}`, {
        method,
        headers,
        body: body !== undefined ? JSON.stringify(body) : undefined,
    });

    if (response.status === 204) {
        return null;
    }

    const data = await response.json().catch(() => null);
    if (!response.ok) {
        throw new Error(data?.error ?? `Błąd ${response.status}`);
    }
    return data;
}