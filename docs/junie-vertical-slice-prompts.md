# Junie Prompt: Vertical Slice Architecture & Packaging for base-payments

Copy-paste this into Junie (or any LLM) before generating features for this repository.

## Purpose
Describe and enforce the vertical slice (package-by-feature) architecture used in this codebase, powered by Spring Modulith. Follow these rules to keep code decoupled, testable, and compliant with the ArchUnit checks already present.

## Core principles of a vertical slice
- Package by feature first, not by technical layer.
  - Base: `com.ead.payments.<feature>[.<usecase>]` (lowercase, no underscores).
  - Examples: `com.ead.payments.orders.place`, `com.ead.payments.orders.cancel`, `com.ead.payments.orders.search`, `com.ead.payments.products`.
- Each slice groups everything needed for that use case/feature: controller, request/response, service, command, gateway/client, exceptions localized to the use case, and tests.
- Domain model (aggregates, entities, events, repositories) lives at the feature root package, e.g., `com.ead.payments.orders` or `com.ead.payments.products`.
- Cross-feature interactions happen via domain events (Spring Modulith) or via a narrowly exposed application service interface—not by reaching into another feature’s repositories or aggregates.
- Keep slice internals package-private where feasible; expose only true contracts (controllers, DTOs, gateways/clients, properties).

## Stereotypes and naming (align with ArchUnit)
- Allowed suffixes: Application, Handler, Exception, Interceptor, Advice, Aggregate, Order, Product, Controller, Listener, Service, Repository, Configuration, Entity, Status, Event, Command, Mapper, Request, Response, Client, Gateway, Properties.
- Suffix ⇒ required annotation:
  - Controller → `@RestController` or `@Controller`
  - Service → `@Service`
  - Repository → `@Repository`
  - Configuration → `@Configuration`
  - Listener → `@Component`
  - Mapper → `@Mapper` (MapStruct; allowed to be absent if not used)
- English only, no underscores in type names. JSON naming is SNAKE_CASE globally.

## Vertical slice layout template
Target package for a new use case under an existing feature:

```
com.ead.payments.<feature>.<usecase>
  ├─ <Usecase>Controller            // Thin HTTP adapter
  ├─ <Usecase>Request, <Usecase>Response
  ├─ <Usecase>Command               // Input to application layer
  ├─ <Usecase>Service               // Orchestrates the use case (transactional)
  ├─ <External>Gateway              // Abstraction for remote systems (optional)
  ├─ <External>Client               // HTTP client (optional)
  ├─ <External>ClientProperties     // @ConfigurationProperties (optional)
  ├─ <Usecase>Advice/Exception      // Localized web errors (optional)
  └─ package-private helpers only   // keep internals hidden
```

Feature root package (aggregate-centric domain):

```
com.ead.payments.<feature>
  ├─ <Feature>Aggregate             // Domain behavior + invariants
  ├─ <Entity>(ies)                  // State carriers
  ├─ <Feature>Repository            // Persistence for aggregate root only
  ├─ <Feature>PlacedEvent / ...     // Domain events (immutable)
  ├─ <Feature>ControlAdvice         // Common controller advice for the feature (optional)
  └─ listeners/gateways shared by multiple use cases (if truly cross-usecase)
```

Cross-cutting (used by multiple slices):

```
com.ead.payments.logging / security / observability / auditing
```

## Allowed dependencies within a slice
- Controller → depends on: Request/Response, Service. It must not call Repository directly.
- Service → depends on: Command, Aggregate, Repository, Gateway/Client, domain Events. Defines the transactional boundary.
- Aggregate → depends on: Entities/Value Objects, publishes Events. No direct infrastructure logic.
- Repository → depends on: JPA/JDBC only; persists the aggregate root.
- Gateway/Client → encapsulate external calls; ensure correlation/tracing headers are propagated.

Prohibited:
- Calling another feature’s Repository/Aggregate directly.
- Sharing Entities between features.
- Leaking internal types outside the slice/feature.

## Cross-feature interaction (Spring Modulith)
- Prefer domain events (e.g., `OrderPlacedEvent`, `OrderCancelledEvent`) to integrate features asynchronously.
- Events are persisted and republished via Spring Modulith JDBC configuration (outbox-like tables already configured in this project).
- If synchronous interaction is unavoidable, expose a minimal application service interface from the providing feature and inject it—never a foreign Repository/Aggregate.

## HTTP edge conventions
- Controllers are thin translators: HTTP → Command; Command → Service; Service returns model mapped to Response.
- Validate at the edge via javax.validation annotations on Request DTOs.
- Global JSON naming is SNAKE_CASE; assert keys accordingly in tests.
- Security: HTTP Basic; most endpoints require auth. Use in-memory users for local/tests.
- Observability: correlation id and principal interceptors are applied; propagate `X-Correlation-ID` on downstream calls.

## Transactions and consistency
- Service methods are the transactional boundaries; aggregates enforce invariants, then emit events.
- Persist state first, then publish events via Modulith for reliable delivery and eventual consistency.

## Example: orders.place vertical slice
Packages and classes already present illustrate the pattern:
- `com.ead.payments.orders.place.PlaceOrderController` → maps HTTP and validates input.
- `com.ead.payments.orders.place.PlaceOrderService` → orchestrates authorization with issuer, persists Order via `OrderRepository`, emits `OrderPlacedEvent`.
- `com.ead.payments.orders.place.PlaceOrderCommand/Request/Response` → transport and application input.
- `com.ead.payments.orders.place.IssuerGateway/IssuerClient/IssuerClientProperties` → external integration behind a gateway.
- Domain lives at `com.ead.payments.orders`: `OrderAggregate`, `Order`, `OrderRepository`, `OrderPlacedEvent`, `OrderCancelledEvent`.

## Testing vertical slices
- Extend `SpringBootIntegrationTest` for controller-level integration tests using MockMvc and WireMock.
- Active profile for tests: `integration-test`; Docker Compose integration may start Postgres/Kafka/WireMock as needed.
- Use `@WithMockUser` or proper Basic auth headers to hit secured endpoints.
- JSON assertions must use SNAKE_CASE keys.
- Test naming is strict (ArchUnit): methods must match `should…When…` and include `@DisplayName`.
- Keep tests independent; JUnit parallel execution is enabled.

## Do / Don’t
Do:
- Create use cases under their feature: e.g., `orders.refund` with `RefundOrderCommand`, `RefundOrderService`, `RefundOrderController`, and `OrderRefundedEvent`.
- Keep aggregates as the only write entry point for the feature; repositories persist only the aggregate root.
- Publish events for cross-feature communication; prefer listeners over direct calls.
- Mark non-contract types as package-private to avoid accidental coupling.

Don’t:
- Inject or call another feature’s Repository/Aggregate.
- Share JPA entities or database tables across features.
- Create a “common” dumping-ground module for business logic.

## Quick checklist before committing
1) Package path follows `com.ead.payments.<feature>[.<usecase>]`.
2) Controller/Service/Repository annotations match suffix stereotypes.
3) Controller is thin; Service is transactional; Repository persists aggregate root only.
4) No cross-feature repository/aggregate calls; use events or exposed application service interfaces.
5) DTOs use SNAKE_CASE JSON; validation at the edge.
6) Test methods follow `should…When…` with `@DisplayName`.

## Copy-ready template
Paste, replace placeholders, and implement:

```
package com.ead.payments.<feature>.<usecase>;

import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

@RestController
@RequestMapping("/api/<feature>/<usecase>")
@Validated
class <Usecase>Controller {

    private final <Usecase>Service service;

    <Usecase>Controller(<Usecase>Service service) { this.service = service; }

    @PostMapping
    <Usecase>Response handle(@Valid @RequestBody <Usecase>Request request) {
        var command = new <Usecase>Command(/* map from request */);
        var result = service.handle(command);
        return new <Usecase>Response(/* map from result */);
    }
}

@Service
class <Usecase>Service {
    private final com.ead.payments.<feature>.<Feature>Repository repository;
    // private final <External>Gateway gateway; // if needed

    <Usecase>ResponseModel handle(<Usecase>Command command) {
        // 1) load/manipulate aggregate
        // 2) enforce invariants and emit events
        // 3) persist via repository
        // 4) return response model
        return new <Usecase>ResponseModel();
    }
}

record <Usecase>Request(/* fields */) {}
record <Usecase>Response(/* fields */) {}
record <Usecase>Command(/* fields */) {}
```

---
By following this prompt, generated code will align with the project’s vertical slice architecture and pass the existing architecture tests.