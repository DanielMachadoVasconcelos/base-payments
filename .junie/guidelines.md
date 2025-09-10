Project development guidelines (base-payments)

Scope
- Audience: experienced Java/Spring developers onboarding to this codebase.
- Goal: capture project-specific build, test, and development conventions that are easy to miss.

1) Build and configuration
- JDK/Toolchain
  - Requires Java 24 with preview features. The Gradle build configures the toolchain and enables preview for both compile and test tasks, so the wrapper is sufficient as long as a Java 24 JDK is available.
    - build.gradle → java.toolchain.languageVersion = 24; JavaCompile/test jvmArgs include --enable-preview.
  - Always use the Gradle wrapper from the repo.
    - ./gradlew clean build

- Spring Boot / Versions
  - Spring Boot 3.5.5; Spring Cloud 2025.0.0; Spring Modulith 1.4.0.

- Local runtime dependencies (compose)
  - compose.yaml defines the local infra stack:
    - Postgres (orders DB), Kafka (KRaft), AKHQ, WireMock (issuer-service stub), Zipkin, Prometheus, Tempo, Grafana, PgAdmin.
  - Default application profile (src/main/resources/application.properties) is active=local with JDBC URL pointing to localhost services that match compose.yaml ports.
  - Start the stack optionally for local runs:
    - docker compose up -d
  - Spring Boot Docker Compose integration is enabled (start_only). During tests, see “Tests” below for behavior.

- Observability
  - Actuator endpoints are fully exposed in local/tests; Prometheus/Zipkin/Tempo are pre-integrated via application.properties and compose.
  - Useful URLs after docker compose up:
    - Grafana: http://localhost:3000 (admin/admin), Prometheus: http://localhost:9090, Zipkin: http://localhost:9411, Tempo: http://localhost:3200/status, AKHQ: http://localhost:8080, PgAdmin: http://localhost:5050.

- Security
  - Stateless HTTP basic auth; all endpoints except "/" require authentication.
  - For local and integration-test profiles, in-memory users are available (SecurityConfiguration):
    - customer/password (ROLE_CUSTOMER)
    - merchant/password (ROLE_MERCHANT)
    - engineer/password (ROLE_ADMIN)

- Configuration properties of note
  - issuer.client.base-url → points to the issuer WireMock stub. Local runs use http://localhost:8081 (compose). Tests override to a dynamic port (see WireMock in tests section).
  - Jackson naming strategy is SNAKE_CASE globally.
  - Virtual threads enabled (spring.threads.virtual.enabled=true).

2) Tests
- Test harness and profiles
  - JUnit 5; tests run in parallel (JUnit parallel execution enabled in build.gradle). Keep tests isolated and thread-safe.
  - Active profile under tests defaults to integration-test (src/test/resources/application.properties sets spring.profiles.default=integration-test).
  - Hikari tuning for tests minimizes DB connections; if you need multiple concurrent connections, adjust spring.datasource.hikari.maximum-pool-size.

- Database and Docker Compose in tests
  - spring.docker.compose.skip.in-tests=false and lifecycle-management=start_only are enabled in test properties.
    - When running tests, Spring Boot can start services defined in compose.yaml if required. Postgres, Kafka, and WireMock containers match the default local ports used by application/test properties.
  - Flyway migrations run against the orders schema (see resources/db/migration and modulith event tables configuration).

- WireMock in tests (issuer-service)
  - Integration tests extend SpringBootIntegrationTest, which:
    - @EnableWireMock with name="issuer-service" bound to property issuer.client.base-url.
    - Injects a WireMockServer into tests via @InjectWireMock.
    - Provides issuerService() helper that returns IssuerServiceMockProvider for higher-level stubbing via TestMocks.
  - Example usage (see CancelOrderControllerTest):
    - TestMocks.setup(issuerService()).toAcceptTheAuthorizationWith(expectedCorrelationId);
    - Then issue HTTP calls with MockMvc; Correlation-ID header is validated/propagated.

- Security in tests
  - Use @WithMockUser to bypass basic auth if the endpoint just needs a role.
    - Example: @WithMockUser(username = "user", roles = "USER")
  - Alternatively, use Basic auth headers that match the in-memory users for local/integration-test profiles.

- Actuator in tests
  - management.endpoints.web.exposure.include=* and health details enabled in test properties; see ActuatorControllerTest for examples.

- How to run tests
  - All tests: ./gradlew clean test
  - Single test class:
    - ./gradlew test --tests 'com.ead.payments.orders.cancel.CancelOrderControllerTest'
  - Single test method (Gradle filter supports pattern):
    - ./gradlew test --tests 'com.ead.payments.orders.cancel.CancelOrderControllerTest.shouldAllowToCancelAnOrderByIdWhenTheOrderExists'
  - Generate coverage reports:
    - ./gradlew jacocoTestReport (reports under build/reports/jacoco/test/html)

- Adding a new integration test (recommended pattern)
  1) Create a test class extending SpringBootIntegrationTest so you get MockMvc, WireMock, and the proper test profile.
  2) If your test calls secured endpoints, annotate with @WithMockUser or set an Authorization header for one of the in-memory users.
  3) If you need issuer-service behavior, use TestMocks.setup(issuerService()) helpers to stub.
  4) Use MockMvc for controller-level HTTP interactions; set required headers (e.g., version, X-Correlation-ID) as production code expects them.
  5) Keep tests idempotent and independent; parallel execution is on by default.

- Demonstrated example (what we ran while drafting this)
  - A minimal sanity test to verify the pipeline:
    - File (temporary, not committed): src/test/java/com/ead/payments/sanity/DemoTest.java
      - @Test sanityCheck() { assertEquals(4, 2 + 2); }
    - Run: ./gradlew test --tests 'com.ead.payments.sanity.DemoTest'
    - Result: passed.
  - We removed this temporary file to keep the repository clean as per the task requirements.

3) Development conventions and tips
- Architectural/testing conventions
  - There are ArchUnit tests enforcing project conventions under src/test/java/com/ead/payments/architecture:
    - AnnotationConventionTest, ClassesNamesConventionTest, ImmutableEventsConventionTest, LayerConventionTest, TestMethodsConventionTest.
  - When adding modules, aggregates, events, or controllers, run these tests to catch violations early.

- Domain structure highlights
  - Orders and Products follow an Aggregate pattern with event publication (Spring Modulith JDBC events configured under the orders schema).
  - Controllers typically accept/return request/response DTOs using SNAKE_CASE JSON. Observe required headers such as version and optional correlation ID.

- Logging and tracing
  - Custom interceptors add correlation id and principal info; ensure to propagate X-Correlation-ID in client calls when relevant.
  - Micrometer metrics are enabled; Prometheus registry is on; tracing exports to Zipkin/Tempo when those stacks are running.

- Performance/parallelism
  - Tests run concurrently; avoid static shared mutable state. Database pool for tests is intentionally small; if you increase it, ensure Postgres (compose) max_connections is sufficient (compose sets it to 200).

- Common pitfalls
  - Using a JDK < 24 or running without preview features will fail the build.
  - Forgetting @WithMockUser (or proper basic auth) will yield 401/403 in MockMvc tests.
  - issuer.client.base-url must align with the WireMock instance. In tests, it’s wired via @EnableWireMock(ConfigureWireMock(... baseUrlProperties = 'issuer.client.base-url')).
  - Jackson is set to SNAKE_CASE; assert JSON keys accordingly in tests (see jsonPath expectations).

Appendix: quick commands
- Start local infra: docker compose up -d
- Stop local infra: docker compose down -v
- Run app locally (requires infra): ./gradlew bootRun
- Run all tests: ./gradlew clean test
- One test: ./gradlew test --tests 'com.ead.payments.orders.cancel.CancelOrderControllerTest'
- Coverage: ./gradlew jacocoTestReport


4) Testing style and naming conventions (opinionated)
- Method naming
  - Follow BDD-like pattern enforced by ArchUnit: should<Behavior>When<Scenario>
    - Example: shouldAllowToCancelAnOrderByIdWhenTheOrderExists
  - Always annotate with @DisplayName providing a readable sentence mirroring the method name.
- Class naming
  - Keep test classes suffixed with ControllerTest, ServiceTest, RepositoryTest, etc., matching the subject under test (also aligned with ClassesNamesConventionTest).
  - One class per HTTP controller grouping happy-path and edge cases. For complex aggregates, consider one test class per use case.
- Structure and style
  - Use Given/When/Then comments to partition the test body; prefer arranging all inputs first, act once, assert many.
  - Prefer MockMvc for controller tests; use jsonPath with explicit SNAKE_CASE keys (global Jackson setting).
  - For external calls, stub via WireMock using TestMocks.setup(issuerService()) helpers; propagate X-Correlation-ID and assert it where relevant.
  - Avoid time-based flakiness; don’t Thread.sleep. If waiting is required, use Awaitility (not currently added) or poll endpoints deterministically.
- Security in tests
  - Prefer @WithMockUser with the minimal role needed; otherwise send Basic auth headers for customer/merchant/engineer users in local/integration-test profiles.
- Parallelism and isolation
  - Tests run concurrently; avoid static state and use unique test data. DB pool size is 1 in tests—keep interactions short and sequential within each test.
- Assertions
  - Fail fast and be explicit. Use andExpect chains with descriptive jsonPath checks. For business rules, assert domain state via repositories when feasible.

5) DDD and architecture preferences (opinionated)
- Aggregates and boundaries
  - Model Orders and Products as aggregates. Invariants live inside Aggregate classes (OrderAggregate, ProductAggregate). Entities (Order, Product) are state carriers; domain behavior resides in aggregates.
  - Keep one repository per aggregate (OrderRepository, ProductRepository) that persists the aggregate root only.
- Application layer
  - Controllers are thin: validate/translate HTTP → Command/Query DTOs. Orchestrate via Services (e.g., PlaceOrderService, CancelOrderService). Services enforce transaction boundaries.
  - Use Commands for state changes (PlaceOrderCommand) and Request/Response DTOs for transport; never pass entities across layers.
- Domain events
  - Publish domain events from aggregates (OrderPlacedEvent, OrderCancelledEvent). Use Spring Modulith JDBC events for reliable publication; schema is orders, table events/snapshots per config.
  - Consumers/listeners (InventoryListener, PurchaseListener) react to events and integrate with other modules via Kafka through Modulith.
- External integrations
  - Hide external systems behind Gateways/Clients (IssuerGateway/IssuerClient). Use properties for base URLs (issuer.client.base-url) and ensure X-Correlation-ID propagation.
  - For tests, stub with WireMock via SpringBootIntegrationTest; don’t hit real services.
- Naming and packages
  - Suffixes follow ClassesNamesConventionTest: Controller, Service, Repository, Aggregate, Event, Command, Request, Response, Gateway, Client, Properties, etc. Names are English only; no underscores.
- Transactions and consistency
  - Use application service methods as transactional boundaries; aggregates enforce invariants before emitting events. Persist state then publish events through Modulith to achieve eventual consistency across modules.
- Validation and mapping
  - Validate inputs at the edge (controller) using javax.validation annotations on requests; map to commands. Keep aggregates free from transport concerns.
- Observability
  - CorrelationId interceptors add context; propagate tracing headers on outgoing calls. Emit metrics via Micrometer; keep domain operations observable for troubleshooting.


6) Packaging by feature and decoupling (Spring Modulith focus)
- Package by feature (module-first)
  - Top-level feature packages under com.ead.payments.<feature> (e.g., orders, products, inventory, purchases, observability/security as cross-cutting).
  - Within each feature:
    - API/Edge: <feature>.<usecase> for commands/queries and controllers (e.g., orders.place, orders.cancel, orders.search).
    - Application: <feature>.<usecase> Service classes orchestrating use cases and transactions.
    - Domain: <feature> Aggregate, Events, Value Objects (e.g., OrderAggregate, OrderPlacedEvent).
    - Persistence: <feature> Repository for the aggregate root only (e.g., OrderRepository, ProductRepository).
  - Co-locate tests with their feature; prefer integration tests per controller/use case.

- Decoupling rules between features
  - Do not call another feature’s Repository or Aggregate directly. Interactions happen via:
    - Application-level services explicitly exposed by the other feature, or
    - Domain events published by the source feature and handled by listeners in the target feature (preferred for async flows).
  - Avoid leaking internals: classes not intended for other features should be package-private. Only expose DTOs, Commands, Responses, Gateways, and Events that are true contracts.
  - Shared utils: minimize and keep under com.ead.payments.logging/… only for truly cross-cutting concerns (logging, security, interceptors). Avoid a generic “common” dumping ground.
  - Keep controller-to-service mapping and validation in the calling feature; never pass entities across features.

- How Spring Modulith supports and evidences decoupling
  - Events as the default integration mechanism:
    - This project uses spring-modulith-starter-core and spring-modulith-starter-jpa with JDBC-backed event publication.
    - Properties configured under spring.modulith.events.jdbc.* persist events in the orders schema for reliable, outbox-like delivery and republishing on restart.
  - Module boundaries by package:
    - Each top-level package under com.ead.payments is a Modulith module. Use @NamedInterface when you need to explicitly mark the public API of a module; keep implementation types package-private.
    - Prefer publishing domain events (e.g., OrderPlacedEvent, ProductCreatedEvent) over direct synchronous calls across modules.
  - Testing boundaries:
    - Keep Modulith’s test starter for module slice tests if needed (we already include spring-modulith-starter-test). You can verify that only allowed interfaces are visible by writing Modulith/ArchUnit rules.
  - Observability of module interactions:
    - Emitted events are traceable via Actuator and exported to Prometheus/Zipkin/Tempo; this gives runtime evidence of decoupled interactions.

- Practical do/don’t
  - Do: Place new use cases under their feature, e.g., orders.refund. Create RefundOrderCommand, RefundOrderService, RefundOrderController, and domain events like OrderRefundedEvent.
  - Do: Keep OrderAggregate as the only write entry point for Orders; repositories persist aggregates only.
  - Do: If Products must react to Orders, create a listener in products that consumes OrderPlacedEvent via Modulith instead of querying Orders directly.
  - Don’t: Inject OrderRepository into products.* or vice-versa.
  - Don’t: Share JPA entities across features. Map to request/response DTOs or events.
  - Don’t: Create a large common module for business logic—favor small, explicit module APIs and events.

- Naming/structure checklist when adding a feature
  - Package: com.ead.payments.<feature>[.<usecase>]
  - Contracts: Request/Response/Command/Event names reflect the use case (English, no underscores; see ClassesNamesConventionTest).
  - Visibility: default/package-private for internal types; only controllers, public DTOs, and gateways are public.
  - Integration: prefer Modulith events; if synchronous is unavoidable, expose a small application service interface in the providing feature and depend on it via Spring bean, not on persistence.
