import { useEffect, useState } from "react";
import { api, getToken, setToken } from "./api";
import Header from "./components/layout/Header";
import Footer from "./components/layout/Footer";
import HomePage from "./pages/HomePage";
import AuthForm from "./components/AuthForm";
import Profile from "./components/Profile";
import AdminPanel from "./components/AdminPanel";

export default function App() {
  const [me, setMe] = useState(null);
  const [error, setError] = useState("");
  const [view, setView] = useState("home");

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

  async function handleLoggedIn() {
    await loadMe();
    setView("home");
  }

  function logout() {
    setToken(null);
    setMe(null);
    setView("home");
  }

  function navigate(next) {
    setError("");
    setView(next);
    window.scrollTo(0, 0);
  }

  useEffect(() => {
    loadMe();
  }, []);

  const currentView = view === "account" && !me ? "home" : view;

  return (
      <div className="app">
        <Header
            me={me}
            onNavigate={navigate}
            onAddListing={() => {
              /* TODO: formularz dodawania ogłoszenia */
            }}
        />

        <main className="app__main">
          {currentView === "home" && <HomePage />}

          {currentView !== "home" && (
              <div className="container panel">
                {error && <p className="error" onClick={() => setError("")}>{error}</p>}

                {currentView === "login" && <AuthForm onLoggedIn={handleLoggedIn} onError={setError} />}

                {currentView === "account" && (
                    <>
                      <Profile me={me} onLogout={logout} />
                      {me.role === "ADMIN" && <AdminPanel me={me} onError={setError} />}
                    </>
                )}
              </div>
          )}
        </main>

        <Footer />
      </div>
  );
}