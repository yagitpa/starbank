CREATE SCHEMA IF NOT EXISTS public;

CREATE TABLE IF NOT EXISTS public.users (
    id UUID PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS public.products (
    id UUID PRIMARY KEY,
    type VARCHAR(32) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.transactions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    product_id UUID NOT NULL,
    type VARCHAR(32) NOT NULL,
    amount BIGINT NOT NULL,
    CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) REFERENCES public.users(id),
    CONSTRAINT fk_transactions_product FOREIGN KEY (product_id) REFERENCES public.products(id)
);