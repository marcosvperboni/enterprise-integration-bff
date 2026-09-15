# Enterprise Integration BFF

A Backend for Frontend that fans out, in parallel, to three heterogeneous
downstream systems - a modern external REST API, a legacy system with quirky
field naming, and a clean internal microservice - and normalizes their
responses into a single, consistent product view.

This project demonstrates production-grade integration patterns: reactive
orchestration, per-downstream resilience (timeout, retry, circuit breaker,
fallback), data normalization from a legacy shape into a clean domain model,
correlation-id propagation, JWT-secured endpoints, and cache-aside response
caching - built with Domain-Driven Design layering and test-driven
development.

## What it demonstrates

- **BFF orchestration pattern**: a single client-facing endpoint aggregates
  data from three independent systems via parallel, non-blocking WebClient
  calls (`Mono.zip`).
- **Resilience**: each downstream client has its own timeout, retry with
  exponential backoff, and circuit breaker (Resilience4j). If pricing or
  inventory is down, the aggregate response still returns with the healthy
  data plus a `warnings` field - no hard failure from a partial outage.
- **Normalization**: the legacy inventory system returns abbreviated,
  uppercase, string-typed fields (`QTY_AVAIL`, `LAST_UPD_DT` in `yyyyMMdd`).
  The BFF's infrastructure layer translates that into a clean, strongly-typed
  domain model - this translation is unit tested in isolation.
- **Security**: reactive Spring Security resource server validating HS256
  JWTs issued by a demo login endpoint.
- **Observability**: every request gets an `X-Correlation-Id` (generated or
  propagated from the caller), forwarded to all three downstream calls and
  included in log output.
- **Caching**: aggregated product responses are cached in Redis with a short
  TTL (cache-aside), invalidated on writes.
- **DDD + TDD**: each module is layered into `domain` / `application` /
  `infrastructure` / `web`, and the normalization and resilience-fallback
  logic were written test-first.

## Architecture

```
                          ┌─────────────────────┐
   CLIENT  ── REST ──────▶│      bff-gateway      │
                          │  (Spring WebFlux BFF)  │
                          └───────────┬────────────┘
                                      │ fan-out in parallel (Mono.zip)
              ┌───────────────────────┼───────────────────────┐
              ▼                       ▼                       ▼
   ┌─────────────────────┐ ┌───────────────────────┐ ┌───────────────────────┐
   │ external-pricing-api │ │ legacy-inventory-svc  │ │ internal-catalog-api  │
   │  (modern REST/JSON)  │ │ (quirky legacy shape) │ │  (internal standard)  │
   └─────────────────────┘ └───────────┬───────────┘ └───────────────────────┘
                                        │
                             Normalizacao dos dados
                        (abbreviated fields, string-typed
                         numbers/dates -> clean domain model)
                                        │
                                        ▼
                          ┌─────────────────────┐
                          │  ProductAggregateDTO  │──▶ single response to CLIENT
                          │ (+ warnings on partial │
                          │      downstream failure)│
                          └─────────────────────┘

   Cross-cutting: JWT auth at the edge · X-Correlation-Id propagation
   · per-downstream circuit breaker/retry/timeout · Redis cache-aside
```

## Module layout

Multi-module Maven reactor, each module following DDD layering
(`domain` → `application` → `infrastructure` → `web`, no framework
dependencies in `domain`):

| Module | Role | Default port |
|---|---|---|
| `bff-gateway` | The BFF itself: orchestration, normalization, resilience, security, caching | 8080 |
| `external-pricing-api-mock` | Simulates a third-party pricing REST API | 8081 |
| `legacy-inventory-service-mock` | Simulates a legacy inventory system with quirky field shapes | 8082 |
| `internal-catalog-api-mock` | Simulates an internal product catalog microservice (source of truth for product identity) | 8083 |

All three downstream mocks are in-memory (no database) - the point of this
project is the BFF's orchestration, resilience and normalization layer, not
persistence.

## API

Base path: `/api/v1/products` (JWT-protected)

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/products` | List all products, each aggregated from all three downstreams |
| `GET` | `/api/v1/products/{id}` | Get one aggregated product (cached, cache-aside) |
| `POST` | `/api/v1/products` | Create a product (writes to the internal catalog, the source of truth) |
| `PUT` | `/api/v1/products/{id}` | Update a product, evicts the cache entry |
| `DELETE` | `/api/v1/products/{id}` | Delete a product, evicts the cache entry |

Auth: `POST /api/auth/token` with `{"username":"demo","password":"demo123"}`
returns a bearer token (demo-only, hardcoded credentials/issuer - a real
deployment would delegate to an identity provider).

## Tech stack

Java 25 · Spring Boot 4.0.8 · Spring WebFlux · Spring WebClient ·
Spring Security (reactive resource server, JWT) · Resilience4j
(circuit breaker, retry, time limiter) · Redis (reactive, cache-aside) ·
Maven multi-module reactor · WireMock (adapter integration tests) ·
Podman / podman-compose

## Running locally with Podman

```bash
mvn clean package -DskipTests
podman-compose up --build
```

This starts all four services plus Redis on:

- BFF gateway: `http://localhost:18124`
- External pricing API mock: `http://localhost:18121`
- Legacy inventory service mock: `http://localhost:18122`
- Internal catalog API mock: `http://localhost:18123`
- Redis: `localhost:16420`

Get a token and call the API:

```bash
TOKEN=$(curl -s -X POST http://localhost:18124/api/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo123"}' | jq -r .accessToken)

curl -H "Authorization: Bearer $TOKEN" -H "X-Correlation-Id: demo-1" \
  http://localhost:18124/api/v1/products/prod-2001
```

Tear down:

```bash
podman-compose down
```

## Testing

```bash
mvn clean verify
```

Runs, across the whole reactor: domain/application unit tests, `@WebFluxTest`
controller tests (happy path, validation errors, partial-downstream-failure
fallback) and WireMock-backed integration tests per WebClient adapter
(success, timeout, 5xx retry/circuit-breaker behavior).
