-- ============================================================
-- V3__seed_sample_data.sql (optional)
-- ============================================================
INSERT INTO public.tenants (id, name, created_at)
VALUES ('00000000-0000-0000-0000-000000000001', 'Default Tenant', NOW())
    ON CONFLICT (id) DO NOTHING;