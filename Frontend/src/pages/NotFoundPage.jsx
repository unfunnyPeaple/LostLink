import { Link } from "react-router-dom";

export default function NotFoundPage() {
    return (
        <div className="container panel">
            <section>
                <h2>Nie znaleziono strony</h2>
                <p>Ten adres nie prowadzi do żadnej strony LostLink.</p>
                <Link className="btn btn--outline" to="/">Wróć na stronę główną</Link>
            </section>
        </div>
    );
}
