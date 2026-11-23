const DAY = 24 * 60 * 60 * 1000;

function startOfDay(date) {
    const copy = new Date(date);
    copy.setHours(0, 0, 0, 0);
    return copy;
}

export function formatListingDate(iso) {
    const date = new Date(iso);
    const days = Math.round((startOfDay(new Date()) - startOfDay(date)) / DAY);
    const time = date.toLocaleTimeString("pl-PL", { hour: "2-digit", minute: "2-digit" });

    if (days <= 0) return `Dziś, ${time}`;
    if (days === 1) return "Wczoraj";
    if (days < 7) return `${days} dni temu`;
    return date.toLocaleDateString("pl-PL");
}

export function initials(name) {
    return name
        .split(/\s+/)
        .map((part) => part[0])
        .join("")
        .slice(0, 2)
        .toUpperCase();
}