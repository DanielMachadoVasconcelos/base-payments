# Code Style and Design Guidelines (base-payments)

Audience: Experienced Java/Spring developers onboarding to this codebase.
Goal: Capture project-specific coding and design preferences aligned with our architecture and tests.

This project targets Java 24 with preview features and Spring Boot 3.5.5. Build and runtime details live in README.md and the development guidelines document. This file focuses on coding style and design choices.

---

## Guiding Principles

- Prefer clarity and correctness over cleverness.
- Keep business logic simple and isolated; cross-cutting concerns live outside of business code.
- Embrace immutability by default; use Java records for simple carriers.
- Apply SOLID pragmatically; align with our packaging by feature (Spring Modulith) approach.
- Fail fast and return fast; avoid deep nesting.
- Leverage Java 24 language features (records, sealed types, pattern matching) where they add clarity.

---

## SOLID in this project

- Single Responsibility
  - Controllers: HTTP boundary only (validate, translate request/response, delegate).
  - Services: Orchestrate use cases and transactions; no transport or persistence mapping logic.
  - Aggregates: Encapsulate domain invariants and state transitions; publish domain events.
  - Repositories: Persist aggregate roots only.

- Open/Closed
  - Prefer composition over conditionals and switches. E.g., extend behavior via new event listeners or strategy components rather than branching in services.
  - Use sealed interfaces for closed polymorphic hierarchies when the set of variants is known.

- Liskov Substitution
  - Use interfaces for cross-module contracts where substitution is required; keep implementations package-private when not part of the module API.

- Interface Segregation
  - Keep interfaces small and focused. Expose only what consumers need; avoid “fat” service interfaces.

- Dependency Inversion
  - Depend on abstractions at module edges (e.g., IssuerGateway) and inject concrete adapters (IssuerClient). Domain/application code must not depend on HTTP clients or frameworks.

---

## Immutability, Records, and DTOs

- Prefer Java records for:
  - Request/Response DTOs, domain read models, events, and value objects.
  - Example: see orders/Order and products/Product records.
- Benefits: compact syntax, built-in equals/hashCode/toString, clear intent of immutability.
- When not to use records:
  - JPA entities requiring no-arg constructors and proxies (we keep entities out of this project; aggregates are state holders, and persistence is handled by repositories). If you must model a mutable persistence entity, use a standard class with explicit getters and constructor(s); avoid heavy Lombok magic.

---

## Lombok Usage

We use Lombok sparingly to reduce ceremony while keeping intent explicit.

- Allowed and preferred
  - @RequiredArgsConstructor or @AllArgsConstructor for constructors on services/controllers where dependency lists are small and stable.
  - Logging annotations (@Slf4j, @Log4j2) for cross-cutting components and controllers.
  - Lightweight DTO helpers (@With) when modeling command objects that are still classes.
- Discouraged
  - @Data on domain types; it implies setters, equals/hashCode, and toString all at once. Use records or explicit annotations (@Getter, @EqualsAndHashCode) as needed.
  - Hidden mutability. Favor final fields and constructor injection.
- Examples in repo
  - Aggregates use Lombok for boilerplate where they are classic classes (e.g., ProductAggregate, OrderAggregate).
  - Controllers/Services use @AllArgsConstructor/@RequiredArgsConstructor and logging annotations.

---

## Sealed Classes and Pattern Matching (Java 24)

Use sealed hierarchies when the set of variants is known and we want the compiler to enforce exhaustiveness.

- Example pattern
  ```java
  public sealed interface AuthorizationResult permits Approved, Declined, Unknown {}
  public record Approved(String authId) implements AuthorizationResult {}
  public record Declined(String reason) implements AuthorizationResult {}
  public record Unknown(String raw) implements AuthorizationResult {}

  // Handling
  String outcome = switch (result) {
      case Approved a -> "APPROVED:" + a.authId();
      case Declined d -> "DECLINED:" + d.reason();
      case Unknown u -> throw new UnknownAuthorizationStatus("Unknown: " + u.raw());
  };
  ```
- Prefer switch expressions with exhaustiveness over chains of if/else.

---

## Fail Fast, Return Fast

- Use guard clauses to validate inputs and preconditions at the top of methods.
- Avoid nested if/else; prefer early returns.
- Throw domain-specific exceptions close to the source of the problem.
- Example
  ```java
  void place(PlaceOrderCommand cmd) {
      if (cmd == null) throw new IllegalArgumentException("command is required");
      if (cmd.amount() <= 0) throw new IllegalArgumentException("amount must be positive");
      // proceed with simple, happy-path code
  }
  ```

---

## Separation of Concerns (Non-Functional vs Business)

- Do not mix logging, authentication, tracing, or metrics with domain logic.
- Use Spring MVC interceptors and configuration for cross-cutting needs.
  - See logging/CorrelationIdLoggingInterceptor and logging/PrincipalLoggingInterceptor.
  - SecurityConfiguration configures HTTP Basic and in-memory users for local/tests; controllers and services remain unaware of auth specifics.
  - ObservabilityConfiguration wires Micrometer/Actuator; business code emits minimal, purpose-driven metrics (e.g., issuer.authorize) and remains framework-agnostic.

---

## Controllers and Services

- Controllers
  - Validate and translate HTTP ↔ DTO/Command; set required headers (version, X-Correlation-ID).
  - Use records for requests/responses (e.g., PlaceOrderRequest, PlaceOrderResponse can be records or simple DTOs).
  - No business rules in controllers.
- Services
  - Transactional boundary; orchestrate aggregates and gateways.
  - Depend on gateways/clients via interfaces; do not embed HTTP logic.

---

## Domain and Events

- Aggregates encapsulate state and invariants; publish domain events (OrderPlacedEvent, OrderCancelledEvent).
- Keep events immutable and serializable; prefer records or @Value classes.
- Use Spring Modulith JDBC-backed publication for reliability; do not couple features via repositories.

---

## Error Handling

- Map domain exceptions to HTTP responses via @ControllerAdvice (e.g., GeneralExceptionHandler, PlaceOrderAdvice).
- Keep exception types narrow and meaningful (e.g., UnknownAuthorizationStatus, IssuerDeclinedException, OrderNotFoundException).
- Never leak stack traces or internal details in API responses; log details at appropriate levels in advice/interceptors.

---

## Logging and Tracing

- Logging
  - Use structured, contextual logs. Interceptors add correlation id and principal data.
  - Avoid logging sensitive data; use MaskAuthorizationDeniedHandler as a reference for secure handling.
- Tracing/Metrics
  - Use Micrometer for metrics; keep metric names simple and domain-oriented (e.g., issuer.authorize).
  - Actuator endpoints are enabled in local/test profiles. Do not add actuator concerns inside services.

---

## Testing Style and Conventions

- Follow BDD-like naming: should<Behavior>When<Scenario> with @DisplayName mirroring the method name.
- Use MockMvc for controller tests; JSON SNAKE_CASE is global; assert with jsonPath accordingly.
- Use SpringBootIntegrationTest base class for WireMock and profile wiring; stub external integrations via TestMocks.
- Keep tests isolated and parallel-safe; avoid static mutable state.
- ArchUnit tests enforce naming, annotations, immutability for events, layering, and test method patterns.

---

## Practical Do/Don’t

- Do
  - Prefer records for DTOs/events; explicit classes for aggregates and persistence.
  - Keep non-functional requirements in interceptors/configuration; keep business code clean.
  - Use sealed types and switch expressions for closed polymorphism.
  - Apply early returns and guard clauses.
- Don’t
  - Mix logging/auth/observability into services or aggregates.
  - Add broad @Data to domain types; avoid setters.
  - Create deep nested conditionals; prefer decomposition or polymorphism.

---

## Small Examples from this repo

- Records: orders/Order, products/Product.
- Cross-cutting separated: logging/* interceptors; security/SecurityConfiguration; observability/ObservabilityConfiguration.
- Domain events: orders/OrderPlacedEvent, orders/OrderCancelledEvent and products/ProductCreatedEvent.
- Advice-based error mapping: errors/GeneralExceptionHandler, orders/place/PlaceOrderAdvice.

---

## Adoption Checklist for New Code

- [ ] Is the class/function doing one thing? If not, split.
- [ ] Can this be a record? If yes, prefer a record.
- [ ] If not a record, are fields final and constructors explicit? Avoid @Data.
- [ ] Are guard clauses present to fail fast?
- [ ] Are non-functional concerns handled outside (interceptors/config)?
- [ ] Are module boundaries and DTOs/events the only exposed contracts?
- [ ] Are tests named and structured per conventions, with JSON SNAKE_CASE assertions?

If in doubt, mirror the patterns used by Orders and Products features and run the ArchUnit tests to validate design choices.