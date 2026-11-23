import { LogoMark, PlusIcon } from "../icons";
import { initials } from "../../utils/format";

export default function Header({ me, onNavigate, onAddListing }) {
    return (
        <header className="site-header">
            <div className="container site-header__inner">
                <button className="logo" onClick={() => onNavigate("home")}>
                    <LogoMark />
                    <span>LostLink</span>
                </button>

                <nav className="site-nav" aria-label="Główna nawigacja">
                    <a href="#ogloszenia" onClick={() => onNavigate("home")}>Ogłoszenia</a>
                    <a href="#" className="is-disabled" title="Mapa będzie później" onClick={(e) => e.preventDefault()}>
                        Mapa
                    </a>
                </nav>

                <div className="site-header__actions">
                    {me ? (
                        <>
                            <button className="btn btn--account" onClick={() => onNavigate("account")}>
                                <span className="avatar avatar--sm">{initials(me.displayName)}</span>
                                <span className="btn--account__name">{me.displayName}</span>
                            </button>
                            <button className="btn btn--primary" onClick={onAddListing}>
                                <PlusIcon size={16} />
                                Dodaj ogłoszenie
                            </button>
                        </>
                    ) : (
                        <button className="btn btn--ghost" onClick={() => onNavigate("login")}>
                            Zaloguj się
                        </button>
                    )}
                </div>
            </div>
        </header>
    );
}