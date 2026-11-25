import { useEffect } from "react";
import { Outlet, useLocation } from "react-router-dom";
import Footer from "./Footer";
import Header from "./Header";
import { useAuth } from "../app/providers/useAuth";

export default function MainLayout() {
    const { me } = useAuth();
    const location = useLocation();

    useEffect(() => {
        if (location.hash) {
            requestAnimationFrame(() => {
                document.getElementById(location.hash.slice(1))?.scrollIntoView();
            });
        } else {
            window.scrollTo(0, 0);
        }
    }, [location.pathname, location.hash]);

    return (
        <div className="app">
            <Header me={me} onAddListing={() => {}} />
            <main className="app__main">
                <Outlet />
            </main>
            <Footer />
        </div>
    );
}
