import { useState } from "react";
import { api } from "../../../lib/api";
import { useAuth } from "../../../app/providers/useAuth";

export default function AuthForm({ onLoggedIn }) {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [displayName, setDisplayName] = useState("");
    const [info, setInfo] = useState("");
    const [error, setError] = useState("");
    const { login: authenticate } = useAuth();

    async function handleLogin(event) {
        event.preventDefault();
        try {
            await authenticate(email, password);
            setError("");
            await onLoggedIn();
        } catch (requestError) {
            setError(requestError.message);
        }
    }

    async function register() {
        try {
            await api("POST", "/auth/register", { email, password, displayName });
            setInfo("Konto utworzone – możesz się zalogować.");
            setError("");
        } catch (requestError) {
            setInfo("");
            setError(requestError.message);
        }
    }

    return (
        <form onSubmit={handleLogin}>
            <h2>Logowanie / rejestracja</h2>

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
            {error && <p className="error" role="alert">{error}</p>}
        </form>
    );

}