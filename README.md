# Survey Management System (Spring Boot)

A backend-only, multi-tenant Survey SaaS built with **Spring Boot 3**, **Java 17**, and **PostgreSQL**.  
Tenancy is enforced with a **Hibernate Filter** and a `tenant_id` column on every domain table.  
Auth uses **JWT** with a `tenant` claim that must match the `X-Tenant-Id` header.

---

## Why these choices?

### Multi-tenancy strategy
- **Row-level isolation** (single schema, `tenant_id` per row) + **Hibernate Filter** enabled per request.
- Pros: simple local setup, one schema to migrate, low operational overhead.
- Risk: the app must *always* enable the Hibernate filter. We mitigate with tests and a single request filter that enables it every time.
- Future hardening: add Postgres **Row-Level Security (RLS)** and set a per-session `app.tenant_id`.

### Database
- **PostgreSQL**: JSONB for flexible `answers.value_json`, excellent indexing, easy local run via Docker.
- **Flyway** manages schema migrations.
- Each domain table contains `tenant_id`, `created_at`, and indexes to optimize tenant-specific queries.

### Security
- **JWT** (HMAC for demo; use RS256/JWKS in production).
- Requires both:
    - `Authorization: Bearer <jwt>` with claims `{ sub, tenant, roles[], iss }`
    - `X-Tenant-Id: <tenant-uuid>`
- The filter denies requests if `tenant` claim ≠ `X-Tenant-Id`, preventing cross-tenant token reuse.
- Stateless design: no sessions, no cookies — fully scalable behind load balancers.

---

## Architecture highlights

| Layer | Description |
|-------|--------------|
| **Controller** | Thin REST layer using DTOs and validation. |
| **Service** | Business logic, validation, async event publishing, idempotency control. |
| **Repository** | Spring Data JPA with tenant filter applied automatically. |
| **Security** | Stateless JWT validation, tenant enforcement per request. |
| **Event System** | Asynchronous notifications when a survey is published. |

---

## API highlights

- **Surveys:** create, list (paginated), get, publish, delete.
- **Questions:** bulk replace for a survey (simple & predictable order).
- **Responses:** submit (with validation & idempotency), list, **export CSV/JSON**, or stream large datasets.
- **Async notification:** fires `SurveyPublishedEvent` on publish, handled by an async listener.
- **Swagger UI:** interactive docs at `/swagger-ui/index.html` with pre-configured global headers (`Authorization`, `X-Tenant-Id`).

---

##  Advanced Features

### Idempotency
- Each submission to `/api/surveys/{id}/responses` can include an `Idempotency-Key` header.
- The server stores `(tenant_id, key, response_id)` to avoid duplicate submissions during retries.
- Re-sending the same key returns the same `responseId`.

### Async notifications
- When a survey is published, the app emits a `SurveyPublishedEvent`.
- A listener handles it asynchronously (after commit) and logs a notification like:
- The same pattern can easily integrate with Kafka, SQS, or email systems.

### 📤 Exports
- `/api/surveys/{id}/responses/export?format=csv|json` — synchronous export.
- `/api/surveys/{id}/responses/export/stream?format=csv` — streaming CSV export using `ResponseBodyEmitter` for large datasets.

### 📘 Swagger UI Documentation
- Auto-generated via `springdoc-openapi`.
- Global headers (`Authorization`, `X-Tenant-Id`) appear on every endpoint.
- Browse at: http://localhost:8080/swagger-ui/index.html


---

## 🧪 Testing strategy

| Type | File | Purpose |
|------|------|----------|
| **JPA** | `JpaTenantIsolationTest` | Ensures tenant filter prevents data leakage. |
| **WebMvc** | `SecurityWebMvcTest` | Verifies 403 when tenant mismatch, 200 when valid. |
| **WebMvc** | `IdempotencyWebMvcTest` | Confirms same `Idempotency-Key` yields same `responseId`. |
| **Integration** | `IntegrationFlowTest` | Runs full flow on real PostgreSQL (Testcontainers). |
| **Async** | `AsyncNotificationTest` | Confirms async event fired after survey publish. |

All tests are automated and run with:
```bash
mvn test
```
---

## Run locally
## Prerequisties
- JDK 17+
- Maven
```bash
./mvnw spring-boot:run
```
Then open:
```bash
http://localhost:8080/swagger-ui/index.html
```
---
