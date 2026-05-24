# AGENTS.md

This is the mandatory first read for AI agents working on `base-payments`.

The project is a case study, not just a service. Treat every change as a chance to preserve architectural intent, make reasoning easier for the next agent, and keep the codebase teachable.

## Read This First

1. Read this file.
2. Read [docs/agents/README.md](docs/agents/README.md).
3. For implementation work, read the relevant guide before editing:
   - [Project Atlas](docs/agents/PROJECT_ATLAS.md)
   - [Architecture And Design](docs/agents/ARCHITECTURE_AND_DESIGN.md)
   - [Module Interactions](docs/agents/MODULE_INTERACTIONS.md)
   - [Testing Playbook](docs/agents/TESTING_PLAYBOOK.md)
   - [Skills And Rules](docs/agents/SKILLS_AND_RULES.md)
   - [Memories](docs/agents/MEMORIES.md)

## Current Source Of Truth

Use the repository, not memory, as the authority:

- Runtime and dependency versions: `build.gradle`
- Feature and release history: `CHANGELOG.md`
- Local infrastructure: `compose.yaml`
- CI infrastructure: `docker-ci.yml` and `.github/workflows/workflow.yml`
- Runtime config: `src/main/resources/application.yml`
- Test config: `src/test/resources/application.properties`
- Architecture rules: `src/test/java/com/ead/payments/architecture`
- Main code: `src/main/java/com/ead/payments`
- Integration test base: `src/test/java/com/ead/payments/SpringBootIntegrationTest.java`

As of this guide, the project uses Java 25, Spring Boot 4.0.6, Spring Modulith 2.0.6, PostgreSQL, Kafka, Flyway, MockMvc, WireMock, ArchUnit, and JaCoCo.

## Non-Negotiable Agent Rules

- Do not revert user changes. The worktree may be dirty.
- Keep documentation and code honest. If docs disagree with code, update docs or call out the mismatch.
- Preserve vertical-slice packaging. Do not create technical dumping-ground packages such as `service`, `controller`, `repository`, or `utils`.
- Treat ArchUnit tests as executable architecture policy.
- Prefer domain events for cross-module collaboration.
- Keep controllers thin, services orchestration-focused, aggregates responsible for invariants, and cross-cutting concerns outside business logic.
- Use `snake_case` JSON in examples and tests.
- Use test method names matching `should...When...` and add `@DisplayName`.
- For documentation-only changes, do not run formatters or code generators.

## Dirty-Tree Caution

There are existing user changes around moving Jackson configuration from:

```text
src/main/java/com/ead/payments/json/JacksonConfiguration.java
src/test/java/com/ead/payments/json/JacksonConfigurationTest.java
```

to:

```text
src/main/java/com/ead/payments/configurations/JacksonConfiguration.java
src/test/java/com/ead/payments/configurations/JacksonConfigurationTest.java
```

Do not undo that move unless the user explicitly asks. Work with it.

The worktree may also contain generated or local-only directories such as `.gradle-user-home/`, `BOOT-INF/`, `META-INF/`, `.DS_Store`, and `terraform/`. Ignore unrelated files unless the task explicitly involves them.

## Architecture Snapshot

`base-payments` is organized by business capability:

```text
com.ead.payments.orders
  place
  cancel
  complete
  search
  events
com.ead.payments.products
com.ead.payments.inventory
com.ead.payments.purchases
com.ead.payments.logging
com.ead.payments.security
com.ead.payments.observability
```

Core flow:

1. `PlaceOrderController` receives HTTP.
2. Request mappers create `PlaceOrderCommand`.
3. `PlaceOrderService` asks `IssuerService` to authorize.
4. `IssuerService` calls the issuer through `IssuerGateway` and `IssuerClient`.
5. `OrderAggregate` validates state and registers `OrderPlacedEvent`.
6. `OrderRepository` persists the aggregate.
7. Spring Modulith publishes events internally and externally.
8. `InventoryListener` reacts to order/product events; `PurchaseListener` reacts to product events.

Completion rule: `PUT /orders/{order_id}/complete` transitions `PLACED` orders to `COMPLETED`, returns the same completed order idempotently for already completed orders, and rejects cancelled orders.

## Testing Map

Run local infrastructure first when tests need PostgreSQL and Kafka:

```bash
docker compose up -d
./gradlew test
```

CI uses:

```bash
docker compose -f docker-ci.yml up -d
./gradlew test -Djava.compiler.args="--enable-preview" --parallel
```

Testing rules:

- Controller/integration tests extend `SpringBootIntegrationTest`.
- Use MockMvc for HTTP boundaries.
- Use `TestMocks.setup(issuerService())` for issuer WireMock behavior.
- Use `@WithMockUser` for secured controller tests.
- Assert JSON with `snake_case` field names.
- Architecture tests live in `src/test/java/com/ead/payments/architecture` and should guide new code.

## When Adding A Feature

Use a vertical slice under the owning business package:

```text
com.ead.payments.<feature>.<usecase>
  <Usecase>Controller
  <Usecase>Request
  <Usecase>Response
  <Usecase>Command
  <Usecase>Service
  <External>Gateway
  <External>Client
  <Usecase>Advice
```

Do not inject another feature's repository or aggregate. Use events first. If synchronous collaboration is unavoidable, expose the narrowest possible application contract.

## Candid Gotchas

- Older docs may mention Java 24 or Spring Boot 3.x. Trust `build.gradle`: Java 25 and Spring Boot 4.0.6.
- `EventsController` currently has hardcoded fallback event responses when no event is found. Treat that as a known rough edge.
- The README previously had stale ports and endpoint shapes. The current docs are intended to fix that.
- Some comments in source files are explanatory for the case-study style. Keep comments useful, not noisy.

## Agent Memory

Update [docs/agents/MEMORIES.md](docs/agents/MEMORIES.md) when you learn durable user preferences, project goals, caveats, or handoff notes that will help the next AI agent work more safely.

## Feature Log

Update [CHANGELOG.md](CHANGELOG.md) for feature work that will ship to `master`. Use `build.gradle` as the application-version source.
