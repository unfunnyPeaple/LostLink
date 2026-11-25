import { useNavigate } from "react-router-dom";
import AuthForm from "../features/auth/components/AuthForm";
import { useAuth } from "../app/providers/useAuth";

export default function LoginPage() {
    const navigate = useNavigate();
    const { error } = useAuth();

    return (
        <div className="container panel">
            {error && <p className="error" role="alert">{error}</p>}
            <AuthForm onLoggedIn={() => navigate("/")} />
        </div>
    );
}
