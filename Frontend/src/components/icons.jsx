function Icon({ size = 18, children, ...props }) {
    return (
        <svg
            width={size}
            height={size}
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.8"
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
            {...props}
        >
            {children}
        </svg>
    );
}

export const SearchIcon = (p) => (
    <Icon {...p}>
        <circle cx="11" cy="11" r="7" />
        <path d="m20 20-3.5-3.5" />
    </Icon>
);

export const PinIcon = (p) => (
    <Icon {...p}>
        <path d="M12 21s-7-6.2-7-11.5a7 7 0 0 1 14 0C19 14.8 12 21 12 21Z" />
        <circle cx="12" cy="9.5" r="2.5" />
    </Icon>
);

export const CalendarIcon = (p) => (
    <Icon {...p}>
        <rect x="4" y="5" width="16" height="15" rx="2" />
        <path d="M4 10h16M9 3v4M15 3v4" />
    </Icon>
);

export const ArrowRightIcon = (p) => (
    <Icon {...p}>
        <path d="M5 12h14M13 6l6 6-6 6" />
    </Icon>
);

export const ChevronDownIcon = (p) => (
    <Icon {...p}>
        <path d="m6 9 6 6 6-6" />
    </Icon>
);

export const PlusIcon = (p) => (
    <Icon {...p}>
        <path d="M12 5v14M5 12h14" />
    </Icon>
);

/* --- Ikony kategorii --- */

const KeyIcon = (p) => (
    <Icon {...p}>
        <circle cx="8" cy="15" r="4" />
        <path d="m11 12 8-8M16 7l2 2M14 9l2 2" />
    </Icon>
);

const IdCardIcon = (p) => (
    <Icon {...p}>
        <rect x="3" y="5" width="18" height="14" rx="2" />
        <circle cx="9" cy="11" r="2" />
        <path d="M6 16c.6-1.4 1.7-2 3-2s2.4.6 3 2M15 10h3M15 13h3" />
    </Icon>
);

const HeadphonesIcon = (p) => (
    <Icon {...p}>
        <path d="M4 15v-3a8 8 0 0 1 16 0v3" />
        <rect x="3" y="14" width="5" height="6" rx="1.5" />
        <rect x="16" y="14" width="5" height="6" rx="1.5" />
    </Icon>
);

const WalletIcon = (p) => (
    <Icon {...p}>
        <path d="M4 7a2 2 0 0 1 2-2h11v4" />
        <rect x="4" y="7" width="16" height="12" rx="2" />
        <path d="M16 13h1.5" />
    </Icon>
);

const BagIcon = (p) => (
    <Icon {...p}>
        <path d="M9 6V5a3 3 0 0 1 6 0v1" />
        <rect x="5" y="6" width="14" height="15" rx="3" />
        <path d="M9 13h6" />
    </Icon>
);

const ShirtIcon = (p) => (
    <Icon {...p}>
        <path d="M8 4 4 6.5l1.5 4L8 9.5V20h8V9.5l2.5 1 1.5-4L16 4a4 4 0 0 1-8 0Z" />
    </Icon>
);

const PawIcon = (p) => (
    <Icon {...p}>
        <circle cx="6" cy="10" r="1.8" />
        <circle cx="9.5" cy="6" r="1.8" />
        <circle cx="14.5" cy="6" r="1.8" />
        <circle cx="18" cy="10" r="1.8" />
        <path d="M12 12c-2.5 0-5 3-5 5.2C7 19 8.3 20 10 20c.8 0 1.3-.4 2-.4s1.2.4 2 .4c1.7 0 3-1 3-2.8C17 15 14.5 12 12 12Z" />
    </Icon>
);

const BoxIcon = (p) => (
    <Icon {...p}>
        <path d="m4 7.5 8-4 8 4v9l-8 4-8-4v-9Z" />
        <path d="m4 7.5 8 4 8-4M12 11.5v9" />
    </Icon>
);

export const CATEGORY_ICONS = {
    KEYS: KeyIcon,
    DOCUMENTS: IdCardIcon,
    ELECTRONICS: HeadphonesIcon,
    WALLETS: WalletIcon,
    BAGS: BagIcon,
    CLOTHES: ShirtIcon,
    ANIMALS: PawIcon,
    OTHER: BoxIcon,
};

export function LogoMark({ size = 32 }) {
    return (
        <span className="logo-mark" style={{ width: size, height: size }}>
            <svg width={size * 0.6} height={size * 0.6} viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round"
                 aria-hidden="true">
                <path d="M9 15 6.5 17.5a3.5 3.5 0 0 1-5-5L4 10" transform="translate(2 0)" />
                <path d="m15 9 2.5-2.5a3.5 3.5 0 0 0-5-5L10 4" transform="translate(0 2)" />
                <path d="M11 11.5a2 2 0 1 1 2.5 1.9c-.5.2-.5.6-.5 1.1" />
                <path d="M13 17.5h.01" />
            </svg>
        </span>
    );
}