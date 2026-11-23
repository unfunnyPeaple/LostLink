import { CATEGORIES } from "../../data/listings";

export default function CategoryFilter({ value, onChange }) {
    return (
        <div className="chips" role="group" aria-label="Kategorie">
            {CATEGORIES.map((c) => (
                <button
                    key={c.id}
                    className={`chip ${value === c.id ? "is-active" : ""}`}
                    aria-pressed={value === c.id}
                    onClick={() => onChange(c.id)}
                >
                    {c.label}
                </button>
            ))}
        </div>
    );
}