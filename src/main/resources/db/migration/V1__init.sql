-- ============================================================
-- V1__init.sql
-- Initial schema for Survey Management System
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ------------------------
-- Tenants
-- ------------------------
CREATE TABLE tenants (
                         id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         name TEXT NOT NULL,
                         status TEXT NOT NULL DEFAULT 'ACTIVE',
                         created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ------------------------
-- Surveys
-- ------------------------
CREATE TABLE surveys (
                         id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                         title TEXT NOT NULL,
                         description TEXT,
                         status TEXT NOT NULL DEFAULT 'DRAFT', -- DRAFT | ACTIVE
                         created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                         updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                         startsAt TIMESTAMPTZ,
                         endsAt TIMESTAMPTZ,
);
CREATE INDEX idx_surveys_tenant ON surveys(tenant_id);
CREATE INDEX idx_surveys_status ON surveys(status);

-- ------------------------
-- Questions
-- ------------------------
CREATE TABLE questions (
                           id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                           tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                           survey_id UUID NOT NULL REFERENCES surveys(id) ON DELETE CASCADE,
                           type TEXT NOT NULL,              -- TEXT, SINGLE_CHOICE, MULTI_CHOICE, NUMBER
                           text TEXT NOT NULL,
                           required BOOLEAN NOT NULL DEFAULT TRUE,
                           position INT NOT NULL,
                           created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_questions_tenant_survey ON questions(tenant_id, survey_id);

-- ------------------------
-- Options (for choice-type questions)
-- ------------------------
CREATE TABLE options (
                         id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                         question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
                         text TEXT NOT NULL,
                         position INT NOT NULL,
                         created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_options_tenant_question ON options(tenant_id, question_id);

-- -------------------------------
-- Option Choice
-- -------------------------------
CREATE TABLE option_choices (
                                id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                                question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
                                label TEXT NOT NULL,
                                value TEXT,
                                position INT NOT NULL
);
-- ------------------------
-- Responses (one per respondent per survey)
-- ------------------------
CREATE TABLE responses (
                           id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                           tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                           survey_id UUID NOT NULL REFERENCES surveys(id) ON DELETE CASCADE,
                           respondent_id UUID,
                           answers_json JSONB,
                           submitted_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_responses_tenant_survey ON responses(tenant_id, survey_id);

CREATE INDEX idx_answers_tenant_response ON answers(tenant_id, response_id);
CREATE INDEX idx_answers_question ON answers(question_id);