# Base Payments Service

Case-study service by Daniel Machado Vasconcelos.

[![Java CI with Gradle](https://github.com/DanielMachadoVasconcelos/base-payments/actions/workflows/workflow.yml/badge.svg)](https://github.com/DanielMachadoVasconcelos/base-payments/actions/workflows/workflow.yml)

## What This Project Is

`base-payments` is a Spring Boot payments and order-management case study. It is intentionally shaped as a modular, observable, event-driven service so developers and AI agents can study how payment authorization, order placement, product registration, inventory reactions, and audit-style event access fit together without turning the codebase into a layered package maze.

The project demonstrates:

- Spring Modulith modules organized by business capability and use case.
- Order and product aggregates with domain events.
- Reliable event publication through Spring Modulith and Kafka.
- PostgreSQL persistence with Flyway migrations.
- HTTP APIs secured with Spring Security.
- External issuer authorization behind a gateway/client boundary.
- Local observability with metrics, traces, logs, and Grafana dashboards.
- Architecture tests that make naming, packaging, annotations, and test style executable rules.

AI agents should start with [AGENTS.md](AGENTS.md) before changing anything.

## Tech Stack

- Java 25 with preview features enabled.
- Spring Boot 4.0.6.
- Spring Modulith 2.0.6.
- Gradle wrapper.
- PostgreSQL, Kafka, WireMock, Grafana LGTM, pgAdmin, AKHQ.
- Flyway, Spring Data JPA, Spring Security, Spring MVC, MapStruct, Lombok.
- JUnit 5, MockMvc, WireMock Spring Boot, ArchUnit, JaCoCo.

## Getting Started

### Prerequisites

- JDK 25.
- Docker with Docker Compose.
- A shell that can run the Gradle wrapper.

### Clone

```bash
git clone https://github.com/DanielMachadoVasconcelos/base-payments.git
cd base-payments
```

### Start Local Infrastructure

```bash
docker compose up -d
```

`compose.yaml` starts the local services used by the app:

| Service | URL or port | Purpose |
| --- | --- | --- |
| PostgreSQL | `localhost:5432` | Orders, products, Modulith event tables |
| pgAdmin | `http://localhost:5050` | Database UI |
| Kafka | `localhost:9092` | Event broker |
| AKHQ | `http://localhost:8082` | Kafka UI |
| WireMock issuer | `http://localhost:18081` | Mock payment issuer |
| Grafana LGTM | `http://localhost:3000` | Metrics, traces, logs |
| OTLP ingest | `localhost:4317`, `localhost:4318` | Telemetry ingest |

Local database credentials:

```text
database: orders
username: user
password: password
```

pgAdmin credentials:

```text
username: admin@admin.com
password: admin
```

### Run The Application

```bash
./gradlew bootRun
```

By default the service starts on `http://localhost:8080`.

Useful local URLs:

- Swagger UI: `http://localhost:8080/swagger-ui-html`
- Actuator health: `http://localhost:8080/actuator/health`
- Grafana: `http://localhost:3000`
- AKHQ: `http://localhost:8082`
- pgAdmin: `http://localhost:5050`

### Run Tests

The integration tests expect PostgreSQL and Kafka on localhost. For a local run, start `compose.yaml` first:

```bash
docker compose up -d
./gradlew test
```

CI starts a smaller infrastructure set from `docker-ci.yml`, then runs:

```bash
./gradlew test -Djava.compiler.args="--enable-preview" --parallel
```

The Gradle `test` task runs JUnit 5 tests, architecture tests, and then produces JaCoCo reports.

## API Surface

Security is enabled. In local and integration-test profiles, these users exist:

| Username | Password | Role |
| --- | --- | --- |
| `customer` | `password` | `CUSTOMER` |
| `merchant` | `password` | `MERCHANT` |
| `engineer` | `password` | `ADMIN` |
| `grafana` | `password` | `ADMIN` |

Current controller surface:

| Method | Path | Version behavior | Role intent |
| --- | --- | --- | --- |
| `POST` | `/orders` | Header `version: 1.0.0` creates a V1 order | `CUSTOMER` or `MERCHANT` |
| `POST` | `/orders` | No version header defaults to V2 with line items | `CUSTOMER` or `MERCHANT` |
| `GET` | `/orders/{order_id}` | Header `version: 1.0.0`; includes lifecycle `status` | `CUSTOMER` or `MERCHANT` |
| `POST` | `/orders/{order_id}/cancel` | Header `version: 1.0.0` | `CUSTOMER` or `MERCHANT` |
| `PUT` | `/orders/{order_id}/complete` | Header `version: 1.0.0`; idempotent for already completed orders | `CUSTOMER` or `MERCHANT` |
| `GET` | `/orders/{order_id}/events` | Header `version: 1.0.0` | `CUSTOMER` or `MERCHANT` |
| `GET` | `/orders/{order_id}/events/{event_id}` | Header `version: 1.0.0` | `CUSTOMER` or `MERCHANT` |
| `POST` | `/products` | Header `version: 1.0.0` | `ADMIN` |

Example V2 order request:

```bash
curl --request POST 'http://localhost:8080/orders' \
  --user customer:password \
  --header 'Content-Type: application/json' \
  --header 'X-Correlation-ID: demo-correlation-id' \
  --data '{
    "currency": "USD",
    "line_items": [
      {
        "name": "Wireless Bluetooth Headphones",
        "quantity": 2,
        "unit_price": 1000,
        "reference": "SKU-HEADPHONE-001"
      }
    ]
  }'
```

Example V1 order request:

```bash
curl --request POST 'http://localhost:8080/orders' \
  --user customer:password \
  --header 'Content-Type: application/json' \
  --header 'version: 1.0.0' \
  --header 'X-Correlation-ID: demo-correlation-id' \
  --data '{
    "currency": "USD",
    "amount": 3500
  }'
```

JSON uses `snake_case` globally.

## Architecture At A Glance

The code is organized around business modules and vertical slices:

```text
com.ead.payments
  orders
    place
    cancel
    complete
    search
    events
  products
  inventory
  purchases
  logging
  security
  observability
  auditing
  tracing
```

Core rules:

- Controllers translate HTTP to commands and responses.
- Services orchestrate use cases and define transactional flow.
- Aggregates enforce invariants and publish domain events.
- Repositories persist aggregate roots.
- External systems sit behind gateways and clients.
- Cross-module communication should prefer Spring Modulith events.
- Cross-cutting concerns belong in configuration, filters, interceptors, and advice.

The architecture tests in `src/test/java/com/ead/payments/architecture` are the executable source of truth for naming, annotations, package layering, immutable event style, and test method names.

## Observability

`compose.yaml` runs `grafana/otel-lgtm`, and `application.yml` exports local telemetry through OTLP:

- Metrics: `http://localhost:4318/v1/metrics`
- Traces: `http://localhost:4318/v1/traces`
- Logs: `http://localhost:4318/v1/logs`

Useful dashboards are provisioned under `grafana/provisioning/dashboards`.

Correlation and principal information are handled by interceptors in `src/main/java/com/ead/payments/logging`. Business services should stay focused on domain behavior, not request plumbing.

## Notes For Future Agents

This repository has a dedicated AI-agent guide:

- [AGENTS.md](AGENTS.md)
- [docs/agents/README.md](docs/agents/README.md)
- [docs/agents/MEMORIES.md](docs/agents/MEMORIES.md)
- [CHANGELOG.md](CHANGELOG.md)

When documentation disagrees with code, trust `build.gradle`, `compose.yaml`, `src/main/java`, `src/test/java`, and the architecture tests first. Then update the docs so the next agent has one fewer trap to step around.
