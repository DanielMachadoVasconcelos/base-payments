# Project Atlas

Use this file to orient quickly before making changes.

## Project Identity

`base-payments` is a payments/order case study that demonstrates modular Spring design, payment authorization, reliable events, local observability, and executable architecture rules.

The project is not only about delivering endpoints. It is also about documenting how a thoughtful Java/Spring codebase can be shaped so future developers and AI agents can reason about it safely.

## Source Of Truth

Trust these files first:

| Concern | Source |
| --- | --- |
| Java, Spring, dependencies | `build.gradle` |
| Local app configuration | `src/main/resources/application.yml` |
| Test profile configuration | `src/test/resources/application.properties` |
| Local dependencies | `compose.yaml` |
| CI dependencies | `docker-ci.yml` |
| CI workflow | `.github/workflows/workflow.yml` |
| Architecture policy | `src/test/java/com/ead/payments/architecture` |
| Integration test harness | `src/test/java/com/ead/payments/SpringBootIntegrationTest.java` |
| Feature/release history | `CHANGELOG.md` |

Current build facts:

- Java 25 toolchain.
- `--enable-preview` for Java compilation and tests.
- Spring Boot 4.0.6.
- Spring Modulith 2.0.6.
- Spring Cloud 2025.1.1.
- Flyway 12.6.2.

## Package Map

```text
src/main/java/com/ead/payments
  Application.java
  auditing
  confidentiality
  configurations
  errors
  inventory
  logging
  observability
  orders
    cancel
    events
    mapping
    place
      mapping
      request
      response
    response
    search
  products
  purchases
  security
  tracing
```

Business modules:

- `orders`: aggregate, order state, line items, repository, order events, and order use cases.
- `orders.place`: place-order HTTP flow, request versions, issuer authorization, command and response mapping.
- `orders.cancel`: cancel-order use case.
- `orders.search`: search-order use case.
- `orders.events`: read access over stored order events.
- `products`: product aggregate and create-product use case.
- `inventory`: listener reacting to order/product events.
- `purchases`: listener reacting to product events.

Cross-cutting modules:

- `logging`: correlation id, principal logging, request traffic, order id context, mocked-stub logging, OpenTelemetry appender configuration.
- `security`: HTTP Basic, role setup for local/test, method security.
- `observability`: HTTP observation filter configuration.
- `auditing`, `tracing`, `confidentiality`, `errors`, `configurations`: supporting infrastructure and edge behavior.

## Runtime Services

`compose.yaml` starts:

| Service | Local endpoint | Why it exists |
| --- | --- | --- |
| PostgreSQL | `localhost:5432` | Main database and Modulith event publication tables |
| pgAdmin | `http://localhost:5050` | Database inspection |
| Kafka | `localhost:9092` | Event transport |
| AKHQ | `http://localhost:8082` | Kafka UI |
| WireMock | `http://localhost:18081` | Mock issuer authorization service |
| Grafana LGTM | `http://localhost:3000` | Local metrics, traces, and logs |
| OTLP | `localhost:4317`, `localhost:4318` | Telemetry ingest |

CI uses `docker-ci.yml`, which only starts PostgreSQL and Kafka.

## Data And Event Artifacts

- Flyway migrations live under `src/main/resources/db/migration`.
- Modulith event publication is configured under `spring.modulith` in `application.yml`.
- Order events are externalized to `orders-events.v1.topic`.
- WireMock mappings for issuer authorization live under `wiremock/mappings`.
- Grafana dashboards live under `grafana/provisioning/dashboards`.
- Feature and release notes live in root `CHANGELOG.md`.

## Public HTTP Shape

Current controllers expose:

- `POST /orders` with `version: 1.0.0` for V1 orders.
- `POST /orders` without a version header for V2 line-item orders.
- `GET /orders/{order_id}` with `version: 1.0.0`.
- `POST /orders/{order_id}/cancel` with `version: 1.0.0`.
- `GET /orders/{order_id}/events` with `version: 1.0.0`.
- `GET /orders/{order_id}/events/{event_id}` with `version: 1.0.0`.
- `POST /products` with `version: 1.0.0`.

JSON is `snake_case`.

## Candid Current Gotchas

- Older docs used to mention Java 24 and Spring Boot 3.x. `build.gradle` says Java 25 and Spring Boot 4.0.6.
- The worktree may include a user package move from `com.ead.payments.json` to `com.ead.payments.configurations`. Do not revert it.
- `EventsController` has a hardcoded fallback response when stored events are missing. That is useful to know before treating event reads as fully production-shaped.
- Some generated/local directories may be present in the worktree. Ignore unrelated artifacts unless the task explicitly targets them.
