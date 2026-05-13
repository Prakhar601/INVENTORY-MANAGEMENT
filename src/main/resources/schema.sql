-- Enable foreign keys and Write-Ahead Logging for concurrency/performance
PRAGMA foreign_keys = ON;
PRAGMA journal_mode = WAL;

-- 1. Authentication
CREATE TABLE IF NOT EXISTS users (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    sync_id       TEXT    NOT NULL UNIQUE DEFAULT (lower(hex(randomblob(16)))), -- SQLite UUID hack
    username      TEXT    NOT NULL UNIQUE,
    email         TEXT    NOT NULL UNIQUE,
    password_hash TEXT    NOT NULL,
    created_at    TEXT    DEFAULT (datetime('now')),
    updated_at    TEXT    DEFAULT (datetime('now')),
    is_deleted    INTEGER DEFAULT 0
);

-- 2. Settings (1:1 with User)
CREATE TABLE IF NOT EXISTS settings (
    user_id       INTEGER PRIMARY KEY,
    currency_code TEXT    DEFAULT 'USD',
    theme         TEXT    DEFAULT 'LIGHT' CHECK(theme IN ('LIGHT', 'DARK', 'SYSTEM')),
    language      TEXT    DEFAULT 'en',
    created_at    TEXT    DEFAULT (datetime('now')),
    updated_at    TEXT    DEFAULT (datetime('now')),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. Accounts
CREATE TABLE IF NOT EXISTS accounts (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    sync_id       TEXT    NOT NULL UNIQUE DEFAULT (lower(hex(randomblob(16)))),
    user_id       INTEGER NOT NULL,
    name          TEXT    NOT NULL,
    type          TEXT    NOT NULL CHECK(type IN ('SAVINGS','CHECKING','CREDIT','CASH')),
    balance       REAL    DEFAULT 0.0,
    created_at    TEXT    DEFAULT (datetime('now')),
    updated_at    TEXT    DEFAULT (datetime('now')),
    is_deleted    INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. Categories (Income & Expense)
CREATE TABLE IF NOT EXISTS categories (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    sync_id       TEXT    NOT NULL UNIQUE DEFAULT (lower(hex(randomblob(16)))),
    user_id       INTEGER NOT NULL,
    name          TEXT    NOT NULL,
    type          TEXT    NOT NULL CHECK(type IN ('INCOME','EXPENSE')),
    icon          TEXT,
    color_hex     TEXT    DEFAULT '#cccccc',
    created_at    TEXT    DEFAULT (datetime('now')),
    updated_at    TEXT    DEFAULT (datetime('now')),
    is_deleted    INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 5. Transactions
CREATE TABLE IF NOT EXISTS transactions (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    sync_id          TEXT    NOT NULL UNIQUE DEFAULT (lower(hex(randomblob(16)))),
    user_id          INTEGER NOT NULL, 
    account_id       INTEGER NOT NULL,
    category_id      INTEGER,
    amount           REAL    NOT NULL CHECK(amount > 0),
    type             TEXT    NOT NULL CHECK(type IN ('INCOME','EXPENSE','TRANSFER')),
    description      TEXT,
    transaction_date TEXT    NOT NULL,
    created_at       TEXT    DEFAULT (datetime('now')),
    updated_at       TEXT    DEFAULT (datetime('now')),
    is_deleted       INTEGER DEFAULT 0,
    FOREIGN KEY (user_id)     REFERENCES users(id)      ON DELETE CASCADE,
    FOREIGN KEY (account_id)  REFERENCES accounts(id)   ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- 6. Budgets (Monthly Tracking)
CREATE TABLE IF NOT EXISTS budgets (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    sync_id       TEXT    NOT NULL UNIQUE DEFAULT (lower(hex(randomblob(16)))),
    user_id       INTEGER NOT NULL,
    category_id   INTEGER NOT NULL,
    amount        REAL    NOT NULL CHECK(amount > 0),
    month         INTEGER NOT NULL CHECK(month BETWEEN 1 AND 12),
    year          INTEGER NOT NULL,
    created_at    TEXT    DEFAULT (datetime('now')),
    updated_at    TEXT    DEFAULT (datetime('now')),
    is_deleted    INTEGER DEFAULT 0,
    FOREIGN KEY (user_id)     REFERENCES users(id)      ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    UNIQUE(user_id, category_id, month, year)
);

-- Indexes for performance and sync
CREATE INDEX IF NOT EXISTS idx_transactions_user_date ON transactions(user_id, transaction_date);
CREATE INDEX IF NOT EXISTS idx_transactions_account ON transactions(account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_category ON transactions(category_id, transaction_date);
CREATE INDEX IF NOT EXISTS idx_transactions_updated ON transactions(updated_at);
CREATE INDEX IF NOT EXISTS idx_accounts_updated ON accounts(updated_at);
CREATE INDEX IF NOT EXISTS idx_categories_updated ON categories(updated_at);
CREATE INDEX IF NOT EXISTS idx_budgets_updated ON budgets(updated_at);
