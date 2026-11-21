import { useEffect, useState } from "react";
import { api, getToken, setToken } from "./api";
import AuthForm from "./components/AuthForm";
import Profile from "./components/Profile";
import AdminPanel from "./components/AdminPanel";

export default function App() {
  const [me, setMe] = useState(null);
  const [error, setError] = useState("");

  async function loadMe() {
    if (!getToken()) return;
    try {
      setMe(await api("GET", "/users/me"));
    } catch (error) {
      setToken(null);
      setMe(null);
      setError(error.message);
    }
  }

  function logout() {
    setToken(null);
    setMe(null);
  }

  useEffect(() => {
    loadMe();
  }, []);

  return (
    <main>
      <h1>LostLink</h1>
      {error && <p className="error" onClick={() => setError("")}> {error} </p>}

      {!me && <AuthForm onLoggedIn={loadMe} onError={setError} />}
      {me && <Profile me={me} onLogout={logout} />}
      {me?.role === "ADMIN" && <AdminPanel me={me} onError={setError} />}
    </main>
  );
}