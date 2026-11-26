import { useState } from "react";
import AdminPanel from "../features/admin/components/AdminPanel";
import Profile from "../features/account/components/Profile";
import { useAuth } from "../app/providers/useAuth";

export default function AccountPage() {
    const { me, logout, error } = useAuth();
    const [adminError, setAdminError] = useState("");

    return (
        <div className="container panel">
            {error && <p className="error" role="alert">{error}</p>}
            {adminError && <p className="error" role="alert">{adminError}</p>}
            <Profile me={me} onLogout={logout} />
            {me.role === "ADMIN" && <AdminPanel me={me} onError={setAdminError} />}
        </div>
    );
}
