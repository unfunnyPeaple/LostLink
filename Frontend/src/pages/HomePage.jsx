import { useMemo, useState } from "react";
import { LISTINGS } from "../data/listings";
import Hero from "../components/home/Hero";
import CategoryFilter from "../components/home/CategoryFilter";
import ListingsSection from "../components/home/ListingsSection";

const PREVIEW_COUNT = 6;

export default function HomePage() {
    const [type, setType] = useState("ALL");
    const [category, setCategory] = useState("ALL");
    const [query, setQuery] = useState("");
    const [sort, setSort] = useState("newest");
    const [showAll, setShowAll] = useState(false);

    const filtered = useMemo(() => {
        const q = query.trim().toLowerCase();

        return LISTINGS
            .filter((l) => type === "ALL" || l.type === type)
            .filter((l) => category === "ALL" || l.category === category)
            .filter((l) => !q || `${l.title} ${l.description} ${l.location}`.toLowerCase().includes(q))
            .sort((a, b) => (sort === "newest" ? b.date.localeCompare(a.date) : a.date.localeCompare(b.date)));
    }, [type, category, query, sort]);

    const visible = showAll ? filtered : filtered.slice(0, PREVIEW_COUNT);

    function scrollToListings() {
        document.getElementById("ogloszenia")?.scrollIntoView({ behavior: "smooth" });
    }

    function resetFilters() {
        setType("ALL");
        setCategory("ALL");
        setQuery("");
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