CREATE TABLE IF NOT EXISTS users (
    id            TEXT PRIMARY KEY,
    email         TEXT NOT NULL UNIQUE COLLATE NOCASE,
    password_hash TEXT NOT NULL,
    display_name  TEXT NOT NULL,
    user_role     TEXT NOT NULL DEFAULT 'USER' CHECK (user_role IN ('ADMIN', 'USER')),
    is_blocked    INTEGER NOT NULL DEFAULT 0 CHECK (is_blocked IN (0, 1)),
    created_at    TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS listings (
    id          TEXT PRIMARY KEY,
    type        TEXT NOT NULL CHECK (type IN ('LOST', 'FOUND')),
    status      TEXT NOT NULL CHECK (status IN ('ACTIVE', 'PICKUP', 'RETURNED')),
    category    TEXT NOT NULL CHECK (category IN (
        'ELECTRONICS', 'DOCUMENTS', 'KEYS', 'WALLETS',
        'BAGS', 'CLOTHES', 'ANIMALS', 'OTHER'
    )),
    title       TEXT NOT NULL,
    location    TEXT NOT NULL,
    date        TEXT NOT NULL,
    description TEXT NOT NULL,
    author_id   TEXT NOT NULL REFERENCES users(id),
    photo       TEXT
);