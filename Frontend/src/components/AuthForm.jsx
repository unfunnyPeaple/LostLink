import { useState } from "react";
import { api, setToken } from "../api";

export default function AuthForm({ onLoggedIn, onError }) {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [displayName, setDisplayName] = useState("");
    const [info, setInfo] = useState("");

    async function login(event) {
        event.preventDefault();
        try {
            const { token } = await api("POST", "/auth/login", { email, password });
            setToken(token);
            onError("");
            await onLoggedIn();
        } catch (error) {
            onError(error.message);
        }
    }

    async function register() {
        try {
            await api("POST", "/auth/register", { email, password, displayName });
            setInfo("Konto utworzone – możesz się zalogować.");
            onError("");
        } catch (error) {
            setInfo("");
            onError(error.message);
        }
    }

    return (
        <form onSubmit={login}>
            <h2>Logowanie / rejestracja</h2>.

            <input
                placeholder="Email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
            />
            <input
                placeholder="Hasło"
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
            />
            <input
                placeholder="Nazwa (tylko przy rejestracji)"
                value={displayName}
                onChange={(event) => setDisplayName(event.target.value)}
            />

            <div>
                <button type="submit">Zaloguj</button>
                <button type="button" onClick={register}>Zarejestruj</button>
            </div>
            {info && <p>{info}</p>}
        </form>
    );

}