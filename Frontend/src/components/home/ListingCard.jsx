import { CATEGORIES } from "../../data/listings";
import { formatListingDate, initials } from "../../utils/format";
import { ArrowRightIcon, CalendarIcon, CATEGORY_ICONS, PinIcon } from "../icons";

const CATEGORY_LABELS = Object.fromEntries(CATEGORIES.map((c) => [c.id, c.label]));

function badgeFor(listing) {
    if (listing.status === "RETURNED") return { label: "Zwrócone", mod: "returned" };
    if (listing.status === "PICKUP") return { label: "W trakcie odbioru", mod: "pickup" };
    return listing.type === "LOST"
        ? { label: "Zgubione", mod: "lost" }
        : { label: "Znalezione", mod: "found" };
}

export default function ListingCard({ listing }) {
    const badge = badgeFor(listing);
    const CategoryIcon = CATEGORY_ICONS[listing.category] ?? CATEGORY_ICONS.OTHER;

    return (
        <article className={`card ${listing.status === "RETURNED" ? "card--muted" : ""}`}>
            {listing.photo ? (
                <div className="card__photo">
                    <img src={listing.photo} alt="" loading="lazy" />
                    <span className={`badge badge--${badge.mod} card__photo-badge`}>{badge.label}</span>
                </div>
            ) : (
                <div className="card__top">
                    <span className={`card__icon card__icon--${listing.type.toLowerCase()}`}>
                        <CategoryIcon size={18} />
                    </span>
                    <span className={`badge badge--${badge.mod}`}>{badge.label}</span>
                </div>
            )}

            <div className="card__body">
                <p className="card__category">{CATEGORY_LABELS[listing.category]}</p>
                <h3 className="card__title">{listing.title}</h3>

                <ul className="card__meta">
                    <li>
                        <PinIcon size={16} />
                        {listing.location}
                    </li>
                    <li>
                        <CalendarIcon size={16} />
                        <time dateTime={listing.date}>{formatListingDate(listing.date)}</time>
                    </li>
                </ul>

                <p className="card__desc">{listing.description}</p>
            </div>

            <footer className="card__footer">
                <span className="avatar">{initials(listing.author)}</span>
                <span className="card__author">{listing.author}</span>
                <button className="icon-btn" aria-label={`Otwórz ogłoszenie: ${listing.title}`}>
                    <ArrowRightIcon size={14} />
                </button>
            </footer>
        </article>
    );
}