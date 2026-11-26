import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { CATEGORY_ICONS, ChevronDownIcon, TrashIcon } from "../components/icons";
import { LISTING_STATUS_OPTIONS, MY_LISTING_FILTERS } from "../features/listings/constants";
import { api } from "../lib/api";
import { formatListingDate } from "../utils/format";

function statusLabel(listing) {
    if (listing.status === "PICKUP") return "W trakcie odbioru";
    if (listing.status === "RETURNED") return "Zakończone";
    return listing.type === "LOST" ? "Zgubione" : "Znalezione";
}

function listingBadgeClass(listing) {
    if (listing.status === "PICKUP") return "pickup";
    if (listing.status === "RETURNED") return "returned";
    return listing.type === "LOST" ? "lost" : "found";
}

export default function MyListingsPage() {
    const [filter, setFilter] = useState("ALL");
    const [listings, setListings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [reloadKey, setReloadKey] = useState(0);
    const [busyIds, setBusyIds] = useState(() => new Set());

    useEffect(() => {
        let active = true;

        api("GET", "/listings/my")
            .then((response) => {
                if (!Array.isArray(response)) {
                    throw new Error("Serwer zwrócił nieprawidłową listę ogłoszeń.");
                }
                if (active) setListings(response);
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
    }, [reloadKey]);

    const visibleListings = useMemo(
        () => listings.filter((listing) => filter === "ALL" || listing.status === filter),
        [listings, filter],
    );

    function setBusy(id, busy) {
        setBusyIds((current) => {
            const next = new Set(current);
            if (busy) next.add(id);
            else next.delete(id);
            return next;
        });
    }

    async function updateStatus(listing, status) {
        setError("");
        setBusy(listing.id, true);
        try {
            const updated = await api("PUT", "/listings/update", { id: listing.id, status });
            setListings((current) =>
                current.map((item) =>
                    item.id === listing.id
                        ? { ...item, ...(updated && typeof updated === "object" ? updated : {}), status }
                        : item,
                ),
            );
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setBusy(listing.id, false);
        }
    }

    async function deleteListing(listing) {
        if (!window.confirm(`Czy na pewno chcesz usunąć ogłoszenie „${listing.title}”?`)) return;

        setError("");
        setBusy(listing.id, true);
        try {
            await api("DELETE", "/listings/delete", { id: listing.id });
            setListings((current) => current.filter((item) => item.id !== listing.id));
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setBusy(listing.id, false);
        }
    }

    return (
        <div className="container my-listings-page">
            <h1 className="section-title">Moje ogłoszenia</h1>

            <div className="my-listings__filters" role="group" aria-label="Filtruj ogłoszenia">
                {MY_LISTING_FILTERS.map(({ id, label }) => (
                    <button
                        key={id}
                        type="button"
                        className={`chip ${filter === id ? "is-active" : ""}`}
                        aria-pressed={filter === id}
                        onClick={() => setFilter(id)}
                    >
                        {label}
                    </button>
                ))}
            </div>

            {error && <p className="error my-listings__error" role="alert">{error}</p>}

            {loading ? (
                <div className="empty" aria-live="polite">
                    <p>Ładowanie ogłoszeń...</p>
                </div>
            ) : error && listings.length === 0 ? (
                <div className="empty">
                    <button
                        className="btn btn--outline"
                        type="button"
                        onClick={() => {
                            setLoading(true);
                            setError("");
                            setReloadKey((key) => key + 1);
                        }}
                    >
                        Spróbuj ponownie
                    </button>
                </div>
            ) : visibleListings.length > 0 ? (
                <div className="my-listings">
                    {visibleListings.map((listing) => {
                        const Icon = CATEGORY_ICONS[listing.category] ?? CATEGORY_ICONS.OTHER;
                        const busy = busyIds.has(listing.id);
                        const badgeClass = listingBadgeClass(listing);

                        return (
                            <article className="my-listing" key={listing.id}>
                                <span className={`my-listing__icon my-listing__icon--${badgeClass}`}>
                                    <Icon size={18} />
                                </span>
                                <div className="my-listing__info">
                                    <h2 className="my-listing__title">{listing.title}</h2>
                                    <p className="my-listing__meta">
                                        {listing.location}
                                        {listing.date ? `, dodano ${formatListingDate(listing.date)}` : ""}
                                    </p>
                                </div>
                                <span className={`my-listing__badge my-listing__badge--${badgeClass}`}>
                                    {statusLabel(listing)}
                                </span>

                                <label className="my-listing__status">
                                    <span className="sr-only">Zmień status: {listing.title}</span>
                                    <select
                                        value={listing.status}
                                        disabled={busy}
                                        onChange={(event) => updateStatus(listing, event.target.value)}
                                    >
                                        {LISTING_STATUS_OPTIONS.map(({ id, label }) => (
                                            <option key={id} value={id}>{label}</option>
                                        ))}
                                    </select>
                                    <ChevronDownIcon size={16} />
                                </label>

                                <Link
                                    className="my-listing__edit"
                                    to={`/listings/${listing.id}/edit`}
                                >
                                    Edytuj
                                </Link>
                                <button
                                    className="my-listing__delete"
                                    type="button"
                                    aria-label={`Usuń ogłoszenie: ${listing.title}`}
                                    disabled={busy}
                                    onClick={() => deleteListing(listing)}
                                >
                                    <TrashIcon />
                                </button>
                            </article>
                        );
                    })}
                </div>
            ) : (
                <div className="empty">
                    <p>
                        {listings.length === 0
                            ? "Nie masz jeszcze żadnych ogłoszeń."
                            : "Brak ogłoszeń w tej kategorii."}
                    </p>
                    {listings.length > 0 && (
                        <button className="btn btn--outline" type="button" onClick={() => setFilter("ALL")}>
                            Pokaż wszystkie
                        </button>
                    )}
                </div>
            )}
        </div>
    );
}
