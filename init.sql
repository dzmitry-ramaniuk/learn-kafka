CREATE TABLE IF NOT EXISTS client
(
    id    INT PRIMARY KEY,
    email TEXT NOT NULL
);

CREATE TYPE transaction_type AS ENUM ('INCOME', 'OUTCOME');

CREATE TABLE IF NOT EXISTS transaction
(
    id               SERIAL PRIMARY KEY,
    bank             TEXT             NOT NULL,
    client_id        INT              NOT NULL REFERENCES client (id) ON DELETE CASCADE,
    transaction_type transaction_type NOT NULL,
    quantity         INT              NOT NULL,
    price            DOUBLE PRECISION NOT NULL,
    cost             DOUBLE PRECISION NOT NULL,
    created_at       TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP
);
