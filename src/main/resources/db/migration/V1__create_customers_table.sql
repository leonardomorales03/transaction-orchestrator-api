CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    document_type VARCHAR(255) NOT NULL,
    document_number VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    middle_name VARCHAR(255),
    second_last_name VARCHAR(255),
    country_calling_code VARCHAR(255),
    phone_number VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);
