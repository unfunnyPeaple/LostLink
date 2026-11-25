import { useState } from "react";
import { useNavigate } from "react-router-dom";
import ListingForm from "../features/listings/components/ListingForm";
import { api } from "../lib/api";

export default function AddListingPage() {
    const navigate = useNavigate();
    const [error, setError] = useState("");
    const [saving, setSaving] = useState(false);

    async function addListing(fields) {
        setError("");
        setSaving(true);
        try {
            await api("POST", "/listings/add", fields);
            navigate("/myListings");
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setSaving(false);
        }
    }

    return <ListingForm error={error} saving={saving} onSubmit={addListing} />;
}
