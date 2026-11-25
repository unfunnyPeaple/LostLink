import { LISTING_TYPES } from "../constants";
import { SearchIcon } from "../../../components/icons";

export default function Hero({ type, onTypeChange, query, onQueryChange, onSearch }) {
    function submit(event) {
        event.preventDefault();
        onSearch();
    }

    return (
        <section className="hero">
            <div className="container">
                <p className="hero__eyebrow">Rzeczy zgubione i znalezione</p>
                <h1 className="hero__title">
                    Zgubione wraca
                    <br />
                    do właściciela.
                </h1>
                <p className="hero__lead">
                    Zgłoś zgubę albo znalezisko, sprawdź mapę i umów zwrot w prywatnej wiadomości.
                </p>

                <form className="search-bar" onSubmit={submit} role="search">
                    <div className="segmented" role="tablist" aria-label="Typ ogłoszenia">
                        {LISTING_TYPES.map((t) => (
                            <button
                                key={t.id}
                                type="button"
                                role="tab"
                                aria-selected={type === t.id}
                                className={type === t.id ? "is-active" : ""}
                                onClick={() => onTypeChange(t.id)}
                            >
                                {t.label}
                            </button>
                        ))}
                    </div>

                    <label className="search-bar__field">
                        <SearchIcon size={20} />
                        <input
                            type="search"
                            placeholder="Czego szukasz? np. klucze, portfel, legitymacja"
                            value={query}
                            onChange={(e) => onQueryChange(e.target.value)}
                            aria-label="Szukaj ogłoszeń"
                        />
                    </label>

                    <button type="submit" className="btn btn--dark search-bar__submit">Szukaj</button>
                </form>
            </div>
        </section>
    );
}