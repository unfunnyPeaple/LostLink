import { Link } from "react-router-dom";
import { LogoMark, PlusIcon } from "../components/icons";
import { initials } from "../utils/format";

export default function Header({ me, onAddListing }) {
    return (
        <header className="site-header">
            <div className="container site-header__inner">
                <Link className="logo" to="/">
                    <LogoMark />
                    <span>LostLink</span>
                </Link>

                <nav className="site-nav" aria-label="Główna nawigacja">
                    <Link to="/#ogloszenia">Ogłoszenia</Link>
                    <a href="#" className="is-disabled" title="Mapa będzie później" onClick={(e) => e.preventDefault()}>
                        Mapa
                    </a>
                </nav>

                <div className="site-header__actions">
                    {me ? (
                        <>
                            <Link className="btn btn--account" to="/account">
                                <span className="avatar avatar--sm">{initials(me.displayName)}</span>
                                <span className="btn--account__name">{me.displayName}</span>
                            </Link>
                            <button className="btn btn--primary" onClick={onAddListing}>
                                <PlusIcon size={16} />
                                Dodaj ogłoszenie
                            </button>
                        </>
                    ) : (
                        <Link className="btn btn--ghost" to="/login">
                            Zaloguj się
                        </Link>
                    )}
                </div>
            </div>
        </header>
    );
}