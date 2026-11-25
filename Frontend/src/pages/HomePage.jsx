import { useEffect, useMemo, useState } from "react";
import { api } from "../lib/api";
import Hero from "../features/listings/components/Hero";
import CategoryFilter from "../features/listings/components/CategoryFilter";
import ListingsSection from "../features/listings/components/ListingsSection";

const PREVIEW_COUNT = 6;

export default function HomePage() {
    const [listings, setListings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [reloadKey, setReloadKey] = useState(0);
    const [type, setType] = useState("ALL");
    const [category, setCategory] = useState("ALL");
    const [query, setQuery] = useState("");
    const [sort, setSort] = useState("newest");
    const [showAll, setShowAll] = useState(false);

    useEffect(() => {
        let active = true;

        api("GET", "/listings/")
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

    const filtered = useMemo(() => {
        const q = query.trim().toLowerCase();

        return listings
            .filter((l) => type === "ALL" || l.type === type)
            .filter((l) => category === "ALL" || l.category === category)
            .filter((l) => !q || `${l.title} ${l.description} ${l.location}`.toLowerCase().includes(q))
            .sort((a, b) => (sort === "newest" ? b.date.localeCompare(a.date) : a.date.localeCompare(b.date)));
    }, [listings, type, category, query, sort]);

    const visible = showAll ? filtered : filtered.slice(0, PREVIEW_COUNT);

    function scrollToListings() {
        document.getElementById("ogloszenia")?.scrollIntoView({ behavior: "smooth" });
    }

    function resetFilters() {
        setType("ALL");
        setCategory("ALL");
        setQuery("");
    }

    function retryLoading() {
        setError("");
        setLoading(true);
        setReloadKey((key) => key + 1);
    }

    return (
        <>
            <Hero
                type={type}
                onTypeChange={setType}
                query={query}
                onQueryChange={setQuery}
                onSearch={scrollToListings}
            />

            <div className="container home-content">
                <CategoryFilter value={category} onChange={setCategory} />

                <ListingsSection
                    listings={visible}
                    total={filtered.length}
                    allTotal={listings.length}
                    loading={loading}
                    error={error}
                    onRetry={retryLoading}
                    sort={sort}
                    onSortChange={setSort}
                    showAll={showAll}
                    onToggleShowAll={() => setShowAll((v) => !v)}
                    onReset={resetFilters}
                />
            </div>
        </>
    );
}