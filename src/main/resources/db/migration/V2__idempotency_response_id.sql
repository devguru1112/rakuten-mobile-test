-- Ensure schema-qualified name to avoid search_path surprises
CREATE TABLE IF NOT EXISTS public.idempotency_keys (
                                                       tenant_id        uuid   NOT NULL,
                                                       key              text   NOT NULL,
                                                       response_id      uuid,
                                                       created_at_epoch bigint NOT NULL DEFAULT 0
);

-- Ensure primary key exists (Postgres doesn't support IF NOT EXISTS here)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM   pg_constraint
    WHERE  conname = 'idempotency_keys_pkey'
    AND    conrelid = 'public.idempotency_keys'::regclass
  ) THEN
ALTER TABLE public.idempotency_keys
    ADD CONSTRAINT idempotency_keys_pkey PRIMARY KEY (tenant_id, key);
END IF;
END$$;

-- Make sure the new columns exist even if table pre-existed without them
ALTER TABLE public.idempotency_keys
    ADD COLUMN IF NOT EXISTS response_id uuid;

ALTER TABLE public.idempotency_keys
    ADD COLUMN IF NOT EXISTS created_at_epoch bigint NOT NULL DEFAULT 0;
