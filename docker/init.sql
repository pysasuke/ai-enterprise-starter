CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS rag_collection (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO rag_collection (id, name) VALUES ('default', 'Default')
ON CONFLICT (id) DO NOTHING;

-- Demo tables for Database Analyze Agent
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price NUMERIC(12, 2) NOT NULL
);

INSERT INTO orders (user_id, status, amount) VALUES
    (1001, 'PAID', 299.00),
    (1002, 'PENDING', 159.50),
    (1003, 'PAID', 88.00);

INSERT INTO order_items (order_id, product_id, quantity, price) VALUES
    (1, 501, 1, 299.00),
    (2, 502, 2, 79.75),
    (3, 503, 1, 88.00);

-- Intentionally no index on orders.user_id for demo analysis
