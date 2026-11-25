import { useState } from "react";
import { Link } from "react-router-dom";
import { CATEGORIES, LISTING_TYPES } from "../constants";

function localDateParts(value) {
    const date = value ? new Date(value) : new Date();
    if (Number.isNaN(date.getTime())) return { date: "", time: "" };

    const pad = (part) => String(part).padStart(2, "0");
    return {
        date: `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`,
        time: `${pad(date.getHours())}:${pad(date.getMinutes())}`,
    };
}

function initialValues(listing) {
    const dateParts = localDateParts(listing?.date);
    return {
        type: listing?.type ?? "FOUND",
        category: listing?.category ?? "OTHER",
        title: listing?.title ?? "",
        location: listing?.location ?? "",
        date: dateParts.date,
        time: dateParts.time,
        description: listing?.description ?? "",
    };
}

export default function ListingForm({
    listing,
    isEditing = false,
    error,
    saving = false,
    onSubmit,
}) {
    const [values, setValues] = useState(() => initialValues(listing));
    const [formError, setFormError] = useState("");
    const originalDate = localDateParts(listing?.date);
    const categories = CATEGORIES.filter(({ id }) => id !== "ALL");
    const types = LISTING_TYPES.filter(({ id }) => id !== "ALL");

    function updateValue(field, value) {
        setValues((current) => ({ ...current, [field]: value }));
        setFormError("");
    }

    async function handleSubmit(event) {
        event.preventDefault();
        setFormError("");

        const selectedDate = new Date(`${values.date}T${values.time || "00:00"}:00`);
        if (Number.isNaN(selectedDate.getTime())) {
            setFormError("Wybierz prawidłową datę.");
            return;
        }

        const date = selectedDate.toISOString();
        const fields = {
            type: values.type,
            category: values.category,
            title: values.title.trim(),
            location: values.location.trim(),
            date,
            description: values.description.trim(),
        };

        if (!isEditing) {
            await onSubmit(fields);
            return;
        }

        const changes = { id: listing.id };
        for (const field of ["type", "category", "title", "location", "description"]) {
            if (fields[field] !== listing[field]) changes[field] = fields[field];
        }
        if (values.date !== originalDate.date || values.time !== originalDate.time) {
            changes.date = date;
        }

        if (Object.keys(changes).length === 1) {
            setFormError("Nie wprowadzono żadnych zmian.");
            return;
        }

        await onSubmit(changes);
    }

    return (
        <div className="container listing-form-page">
            <div className="listing-form-page__head">
                <h1 className="section-title">{isEditing ? "Edytuj ogłoszenie" : "Dodaj ogłoszenie"}</h1>
                <Link className="link-btn" to="/myListings">Moje ogłoszenia</Link>
            </div>

            {error && <p className="error listing-form__error" role="alert">{error}</p>}
            {formError && <p className="error listing-form__error" role="alert">{formError}</p>}

            <form className="listing-form" onSubmit={handleSubmit}>
                <section className="listing-form__section">
                    <h2><span>1.</span> Co się stało?</h2>
                    <div className="listing-form__type-options">
                        {types.map(({ id }) => (
                            <button
                                key={id}
                                type="button"
                                className={`listing-form__type ${values.type === id ? "is-selected" : ""}`}
                                aria-pressed={values.type === id}
                                onClick={() => updateValue("type", id)}
                            >
                                <strong>{id === "LOST" ? "Zgubiłem/am" : "Znalazłem/am"}</strong>
                                <span>{id === "LOST" ? "Szukam swojej rzeczy" : "Chcę oddać właścicielowi"}</span>
                            </button>
                        ))}
                    </div>
                </section>

                <section className="listing-form__section">
                    <h2><span>2.</span> Przedmiot</h2>
                    <fieldset className="listing-form__category-field">
                        <legend>Kategoria</legend>
                        <div className="listing-form__categories">
                            {categories.map(({ id, label }) => (
                                <button
                                    key={id}
                                    type="button"
                                    className={`chip ${values.category === id ? "is-active" : ""}`}
                                    aria-pressed={values.category === id}
                                    onClick={() => updateValue("category", id)}
                                >
                                    {label}
                                </button>
                            ))}
                        </div>
                    </fieldset>
                    <label className="listing-form__field">
                        Tytuł
                        <input
                            type="text"
                            value={values.title}
                            required
                            onChange={(event) => updateValue("title", event.target.value)}
                        />
                    </label>
                </section>

                <section className="listing-form__section">
                    <h2><span>3.</span> Gdzie i kiedy?</h2>
                    <label className="listing-form__field">
                        Miejsce
                        <input
                            type="text"
                            value={values.location}
                            placeholder="Podaj miejsce"
                            required
                            onChange={(event) => updateValue("location", event.target.value)}
                        />
                    </label>
                    <div className="listing-form__fields listing-form__fields--two-col">
                        <label className="listing-form__field">
                            Data
                            <input
                                type="date"
                                value={values.date}
                                required
                                onChange={(event) => updateValue("date", event.target.value)}
                            />
                        </label>
                        <label className="listing-form__field">
                            Godzina
                            <input
                                type="time"
                                value={values.time}
                                onChange={(event) => updateValue("time", event.target.value)}
                            />
                        </label>
                    </div>
                </section>

                <section className="listing-form__section">
                    <h2><span>4.</span> Opis i kontakt</h2>
                    <label className="listing-form__field">
                        Opis
                        <textarea
                            value={values.description}
                            placeholder="Opisz okoliczności i wygląd przedmiotu. Możesz dodać informacje kontaktowe."
                            required
                            onChange={(event) => updateValue("description", event.target.value)}
                        />
                    </label>
                </section>

                <div className="listing-form__actions">
                    <button className="btn btn--primary" type="submit" disabled={saving}>
                        {saving
                            ? "Zapisywanie..."
                            : isEditing
                                ? "Zapisz zmiany"
                                : "Opublikuj ogłoszenie"}
                    </button>
                </div>
            </form>
        </div>
    );
}
