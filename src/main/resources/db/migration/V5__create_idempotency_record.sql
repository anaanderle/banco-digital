CREATE TABLE idempotency_record (
    idempotency_key  VARCHAR(255) PRIMARY KEY,
    request_hash     VARCHAR(255) NOT NULL,
    response_status  INTEGER      NOT NULL,
    response_body    TEXT,
    data_criacao     TIMESTAMP WITH TIME ZONE NOT NULL
);