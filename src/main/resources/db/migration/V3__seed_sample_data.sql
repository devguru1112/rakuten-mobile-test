-- ============================================================
-- V3__seed_sample_data.sql (optional)
-- ============================================================

INSERT INTO tenants (id, name)
VALUES ('11111111-1111-1111-1111-111111111111', 'Demo Tenant')
    ON CONFLICT DO NOTHING;

INSERT INTO surveys (id, tenant_id, title, status)
VALUES ('22222222-2222-2222-2222-222222222222',
        '11111111-1111-1111-1111-111111111111',
        'Employee Engagement Survey',
        'ACTIVE')
    ON CONFLICT DO NOTHING;

INSERT INTO questions (id, tenant_id, survey_id, type, text, required, position)
VALUES
    ('33333333-3333-3333-3333-333333333333',
     '11111111-1111-1111-1111-111111111111',
     '22222222-2222-2222-2222-222222222222',
     'SINGLE_CHOICE', 'How satisfied are you at work?', TRUE, 1),
    ('44444444-4444-4444-4444-444444444444',
     '11111111-1111-1111-1111-111111111111',
     '22222222-2222-2222-2222-222222222222',
     'TEXT', 'What would you like to improve?', FALSE, 2)
    ON CONFLICT DO NOTHING;

INSERT INTO options (id, tenant_id, question_id, text, position)
VALUES
    (uuid_generate_v4(), '11111111-1111-1111-1111-111111111111', '33333333-3333-3333-3333-333333333333', 'Very satisfied', 1),
    (uuid_generate_v4(), '11111111-1111-1111-1111-111111111111', '33333333-3333-3333-3333-333333333333', 'Somewhat satisfied', 2),
    (uuid_generate_v4(), '11111111-1111-1111-1111-111111111111', '33333333-3333-3333-3333-333333333333', 'Not satisfied', 3);