# Architecture And Design

This project uses a Spring Modulith-friendly vertical-slice architecture. The most important idea is simple: organize by business capability and use case, not by technical layer.

## Design Principles

- Package by feature first.
- Keep use cases cohesive and visible.
- Keep cross-module coupling low.
- Put invariants in aggregates.
- Put orchestration in services.
- Put HTTP translation in controllers and mappers.
- Put external calls behind gateways and clients.
- Put logging, tracing, security, and configuration outside business logic.
- Let ArchUnit tests turn architecture decisions into executable checks.

## Vertical Slice Shape

Use this shape for a new use case:

```text
com.ead.payments.<feature>.<usecase>
  <Usecase>Controller
  <Usecase>Request
  <Usecase>Response
  <Usecase>Command
  <Usecase>Service
  <Usecase>Advice
  <External>Gateway
  <External>Client
  <External>ClientProperties
  mapping
```

Feature roots own the domain:

```text
com.ead.payments.<feature>
  <Feature>Aggregate
  <Feature>Repository
  <Feature>Event
  <Feature>Entity
  <Feature>ControlAdvice
```

Do not create `service`, `controller`, `repository`, or `utils` packages. The architecture tests explicitly discourage layer-named packages.

## Responsibility Boundaries

| Component | Responsibility | Must not do |
| --- | --- | --- |
| Controller | HTTP boundary, validation, request/response translation | Business rules, repository access |
| Service | Use-case orchestration, transaction flow, gateway coordination | HTTP concerns, unrelated module internals |
| Aggregate | Invariants, state transitions, domain events | Remote calls, request parsing |
| Repository | Persist aggregate root | Call services/controllers |
| Gateway | Boundary abstraction for external systems | Leak transport details into domain |
| Client | Concrete HTTP integration | Become business-rule owner |
| Listener | React to domain events | Reach directly into foreign repositories |
| Advice | Map exceptions to HTTP | Hide domain errors in generic responses |

## Example: Good Thin Controller

```java
@RestController
@RequestMapping("/orders")
class CancelOrderController {

    private final CancelOrderService service;

    @PostMapping(path = "/{order_id}/cancel", headers = "version=1.0.0")
    CancelOrderResponse cancelOrder(@PathVariable("order_id") UUID orderId) {
        var order = service.handle(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        return new CancelOrderResponse(
                order.id(),
                order.version(),
                order.status(),
                order.currency(),
                order.amount()
        );
    }
}
```

Why this is good:

- The controller translates HTTP input.
- The service owns the use case.
- The repository is not exposed at the HTTP edge.
- The response is explicit.

## Anti-Example: Fat Controller

```java
@RestController
class CancelOrderController {

    private final OrderRepository repository;

    @PostMapping("/orders/{id}/cancel")
    OrderAggregate cancel(@PathVariable UUID id) {
        var order = repository.findById(id).orElseThrow();
        order.setStatus(OrderStatus.CANCELLED);
        return repository.save(order);
    }
}
```

Why this is bad:

- The controller owns domain behavior.
- The aggregate state is mutated from the edge.
- Response shape leaks persistence shape.
- It bypasses service orchestration and event intent.

## Events And Modulith

Domain events are the preferred cross-module collaboration mechanism.

Current examples:

- `OrderPlacedEvent`
- `OrderCancelledEvent`
- `ProductCreatedEvent`

Events are registered by aggregates and handled by Spring Modulith. Externalization to Kafka is configured through `@Externalized`, with order events using `orders-events.v1.topic`.

Good event design:

```java
@Value
@Externalized("orders-events.v1.topic::#{getId().toString()}")
public class OrderPlacedEvent {
    UUID id;
    Long version;
    OrderStatus status;
}
```

Bad event design:

```java
public class OrderPlacedEvent {
    public UUID id;

    public void setId(UUID id) {
        this.id = id;
    }
}
```

Events should be public top-level value types with private final fields and no setters. The ArchUnit event tests enforce this.

## External Issuer Boundary

The issuer authorization flow uses a clean edge:

```text
PlaceOrderService -> IssuerService -> IssuerGateway -> IssuerClient -> WireMock/local issuer
```

`IssuerService.Authorization` is a sealed result hierarchy. This is the right place to parse issuer-specific statuses into meaningful application outcomes.

Keep external response parsing out of `PlaceOrderController` and out of `OrderAggregate`.

## Cross-Cutting Boundaries

Use infrastructure packages for non-functional behavior:

- Correlation and principal logging: `logging`.
- Authorization and users: `security`.
- HTTP observations and telemetry: `observability`, `tracing`, `logging`.
- Error mapping: `errors` and feature-specific `Advice` classes.
- JSON configuration: `configurations`.

Business code may add purposeful domain metrics, but it should not carry request plumbing or security mechanics.

## Versioned API Design

The place-order endpoint supports:

- V1: `POST /orders` with `version: 1.0.0` and amount-based request/response.
- V2: `POST /orders` with no version header and line-item request/response.

When adding or changing versions:

- Keep request and response DTOs explicit.
- Prefer names like `PlaceOrderRequestV2` and `PlaceOrderResponseV2`.
- Keep JSON examples in `snake_case`.
- Add tests for routing behavior and response shape.
