import { ChevronDownIcon } from "../icons";
import ListingCard from "./ListingCard";

const SORT_OPTIONS = [
    { id: "newest", label: "Najnowsze" },
    { id: "oldest", label: "Najstarsze" },
];

export default function ListingsSection({ listings, total, sort, onSortChange, showAll, onToggleShowAll, onReset }) {
    return (
        <section className="listings" id="ogloszenia">
            <div className="listings__head">
                <h2 className="section-title">Najnowsze ogłoszenia</h2>

                <div className="listings__tools">
                    <label className="select">
                        <span className="select__label">Sortuj</span>
                        <span className="select__box">
                            <select value={sort} onChange={(e) => onSortChange(e.target.value)}>
                                {SORT_OPTIONS.map((o) => (
                                    <option key={o.id} value={o.id}>{o.label}</option>
                                ))}
                            </select>
                            <ChevronDownIcon size={16} />
                        </span>
                    </label>

                    {total > listings.length || showAll ? (
                        <button className="link-btn" onClick={onToggleShowAll}>
                            {showAll ? "Pokaż mniej" : "Zobacz wszystkie →"}
                        </button>
                    ) : null}
                </div>
            </div>

            {listings.length > 0 ? (
                <div className="card-grid">
                    {listings.map((listing) => (
                        <ListingCard key={listing.id} listing={listing} />
                    ))}
                </div>
            ) : (
                <div className="empty">
                    <p>Nic nie pasuje do wybranych filtrów.</p>
                    <button className="btn btn--outline" onClick={onReset}>Wyczyść filtry</button>
                </div>
            )}
        </section>
    );
}