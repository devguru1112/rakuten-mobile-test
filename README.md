# Survey Management System (Spring Boot)

A backend-only, multi-tenant Survey SaaS built with **Spring Boot 3**, **Java 17**, and **PostgreSQL**.  
Tenancy is enforced with a **Hibernate Filter** and a `tenant_id` column on every domain table.  
Auth uses **JWT** with a `tenant` claim that must match the `X-Tenant-Id` header.

## Why these choices?

### Multi-tenancy strategy
- **Row-level isolation** (single schema, `tenant_id` per row) + **Hibernate Filter** enabled per request.
- Pros: simple local setup, one schema to migrate, low operational overhead.
- Risk: the app must *always* enable the Hibernate filter. We mitigate with tests and a single request filter that enables it every time.
- Future hardening: add Postgres **Row-Level Security (RLS)** and set a per-session `app.tenant_id`.

### Database
- **PostgreSQL**: JSONB for flexible `answers.value_json`, excellent indexing, easy local run via Docker.
- **Flyway** manages schema migrations.

### Security
- **JWT** (HMAC for demo; use RS256/JWKS in prod).
- Require both:
    - `Authorization: Bearer <jwt>` with claims `{ sub, tenant, roles[], iss }`
    - `X-Tenant-Id: <tenant-uuid>`
- The filter denies requests if `tenant` claim ≠ `X-Tenant-Id`, preventing cross-tenant token reuse.

### API highlights
- Surveys: create, list (paginated), get, publish, delete.
- Questions: bulk replace for a survey (simple & predictable).
- Responses: submit, list, **export CSV/JSON** (added).
- Clean DTOs and global exception handling.

### Testing strategy
- **JPA** test proving tenant isolation.
- **WebMvc** test covering forbidden when tenant mismatch + a happy path.
- **Integration** test using **Testcontainers** (PostgreSQL) to exercise the flow end-to-end.

## Run locally

```bash
./mvnw spring-boot:run
