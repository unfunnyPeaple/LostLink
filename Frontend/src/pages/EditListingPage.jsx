import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import ListingForm from "../features/listings/components/ListingForm";
import { api } from "../lib/api";

export default function EditListingPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [listing, setListing] = useState(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        let active = true;

        api("GET", "/listings/my")
            .then((response) => {
                if (!Array.isArray(response)) {
                    throw new Error("Serwer zwrócił nieprawidłową listę ogłoszeń.");
                }
                const currentListing = response.find((item) => item.id === id);
                if (!currentListing) {
                    throw new Error("Nie znaleziono ogłoszenia do edycji.");
                }
                if (active) setListing(currentListing);
            })
            .catch((requestError) => {
                if (active) setError(requestError.message);
            })
            .finally(() => {
                if (active) setLoading(false);
            });

        return () => {
            active = false;
        };
    }, [id]);

    async function updateListing(changes) {
        setError("");
        setSaving(true);
        try {
            await api("PUT", "/listings/update", changes);
            navigate("/myListings");
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setSaving(false);
        }
    }

    if (loading) {
        return (
            <div className="container listing-form-page" aria-live="polite">
                Ładowanie ogłoszenia...
            </div>
        );
    }

    if (!listing) {
        return (
            <div className="container listing-form-page">
                <p className="error" role="alert">{error}</p>
                <Link className="btn btn--outline" to="/myListings">Wróć do moich ogłoszeń</Link>
            </div>
        );
    }

    return (
        <ListingForm
            listing={listing}
            isEditing
            error={error}
            saving={saving}
            onSubmit={updateListing}
        />
    );
}
