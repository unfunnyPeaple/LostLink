import { Link } from "react-router-dom";
import { LogoMark, PlusIcon } from "../components/icons";
import { initials } from "../utils/format";

export default function Header({ me }) {
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
                    {me && <Link to="/myListings">Moje ogłoszenia</Link>}
                </nav>

                <div className="site-header__actions">
                    {me ? (
                        <>
                            <Link className="btn btn--account" to="/account">
                                <span className="avatar avatar--sm">{initials(me.displayName)}</span>
                                <span className="btn--account__name">{me.displayName}</span>
                            </Link>
                            <Link className="btn btn--primary" to="/listings/add">
                                <PlusIcon size={16} />
                                Dodaj ogłoszenie
                            </Link>
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