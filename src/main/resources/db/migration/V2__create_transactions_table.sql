CREATE TABLE transactions (
    transaction_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_transaction_id VARCHAR(255) NOT NULL UNIQUE,
    amount DECIMAL(19, 4) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL,
    country VARCHAR(2) NOT NULL,
    payment_method_id VARCHAR(255) NOT NULL,
    webhook_url VARCHAR(255) NOT NULL,
    redirect_url VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    expiration_time TIMESTAMP WITH TIME ZONE,
    status VARCHAR(50) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    customer_id BIGINT NOT NULL,
    CONSTRAINT fk_customer
        FOREIGN KEY(customer_id)
        REFERENCES customers(id)
);

CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_processed_at ON transactions(processed_at);
