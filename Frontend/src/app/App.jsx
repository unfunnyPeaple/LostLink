import { Navigate, Route, Routes } from "react-router-dom";
import { useAuth } from "./providers/useAuth";
import HomePage from "../pages/HomePage";
import AccountPage from "../pages/AccountPage";
import LoginPage from "../pages/LoginPage";
import NotFoundPage from "../pages/NotFoundPage";
import MainLayout from "../layouts/MainLayout";
import MyListingsPage from "../pages/MyListingsPage";
import AddListingPage from "../pages/AddListingPage";
import EditListingPage from "../pages/EditListingPage";

function ProtectedAccountRoute() {
    const { loading, me } = useAuth();

    if (loading) {
        return <div className="container panel" aria-live="polite">Ładowanie profilu...</div>;
    }

    return me ? <AccountPage /> : <Navigate to="/login" replace />;
}

function ProtectedMyListingsRoute() {
    const { loading, me } = useAuth();

    if (loading) {
        return <div className="container panel" aria-live="polite">Ładowanie profilu...</div>;
    }

    return me ? <MyListingsPage /> : <Navigate to="/login" replace />;
}

function ProtectedAddListingRoute() {
    const { loading, me } = useAuth();

    if (loading) {
        return <div className="container panel" aria-live="polite">Ładowanie profilu...</div>;
    }

    return me ? <AddListingPage /> : <Navigate to="/login" replace />;
}

function ProtectedEditListingRoute() {
    const { loading, me } = useAuth();

    if (loading) {
        return <div className="container panel" aria-live="polite">Ładowanie profilu...</div>;
    }

    return me ? <EditListingPage /> : <Navigate to="/login" replace />;
}

export default function App() {
    return (
        <Routes>
            <Route element={<MainLayout />}>
                <Route index element={<HomePage />} />
                <Route path="login" element={<LoginPage />} />
                <Route path="account" element={<ProtectedAccountRoute />} />
                <Route path="myListings" element={<ProtectedMyListingsRoute />} />
                <Route path="listings/add" element={<ProtectedAddListingRoute />} />
                <Route path="listings/:id/edit" element={<ProtectedEditListingRoute />} />
                <Route path="*" element={<NotFoundPage />} />
            </Route>
        </Routes>
    );
}
