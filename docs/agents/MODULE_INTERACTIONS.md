# Module Interactions

This guide explains how the main modules collaborate at runtime.

## Order Placement Flow

```mermaid
sequenceDiagram
    participant Client
    participant Controller as PlaceOrderController
    participant Mapper as PlaceOrderCommandMapper
    participant Service as PlaceOrderService
    participant Issuer as IssuerService
    participant Gateway as IssuerGateway/IssuerClient
    participant Aggregate as OrderAggregate
    participant Repo as OrderRepository
    participant Modulith as Spring Modulith Events
    participant Inventory as InventoryListener
    participant Kafka

    Client->>Controller: POST /orders
    Controller->>Mapper: map request to command
    Mapper-->>Controller: PlaceOrderCommand
    Controller->>Service: handle(command)
    Service->>Issuer: authorize(command)
    Issuer->>Gateway: authorize(request)
    Gateway-->>Issuer: issuer response
    Issuer-->>Service: approved or rejected authorization
    Service->>Aggregate: new OrderAggregate(command)
    Aggregate->>Aggregate: validate invariants
    Aggregate->>Aggregate: register OrderPlacedEvent
    Service->>Repo: save(aggregate)
    Repo-->>Service: persisted aggregate
    Repo->>Modulith: publish registered events
    Modulith->>Inventory: on(OrderPlacedEvent)
    Modulith->>Kafka: externalize event
    Service-->>Controller: Order
    Controller-->>Client: response
```

Key decisions:

- Issuer authorization happens before order persistence.
- `OrderAggregate` owns order invariants and event registration.
- Spring Modulith handles event publication after persistence.
- Inventory reacts through events, not direct order repository access.

## Product Creation Flow

```mermaid
sequenceDiagram
    participant Client
    participant Controller as ProductController
    participant Service as ProductService
    participant Aggregate as ProductAggregate
    participant Repo as ProductRepository
    participant Modulith as Spring Modulith Events
    participant Inventory as InventoryListener
    participant Purchases as PurchaseListener

    Client->>Controller: POST /products
    Controller->>Service: handle(CreateProductCommand)
    Service->>Aggregate: new ProductAggregate(command)
    Aggregate->>Aggregate: validate product data
    Aggregate->>Aggregate: register ProductCreatedEvent
    Service->>Repo: save(aggregate)
    Repo->>Modulith: publish registered events
    Modulith->>Inventory: on(ProductCreatedEvent)
    Modulith->>Purchases: on(ProductCreatedEvent)
    Service-->>Controller: Product
    Controller-->>Client: CreateProductResponse
```

Key decisions:

- Product data validation belongs in `ProductAggregate`.
- Inventory and purchase reactions are event listeners.
- Product creation does not call inventory or purchase modules directly.

## Cancellation Flow

```text
CancelOrderController
  -> CancelOrderService
    -> OrderRepository.findById(orderId)
    -> OrderAggregate.cancel()
      -> register OrderCancelledEvent
    -> OrderRepository.save(aggregate)
  -> CancelOrderResponse
```

`OrderAggregate.cancel()` prevents cancellation of completed orders and emits `OrderCancelledEvent`.

## Event Read Flow

```text
EventsController
  -> OrderEventService
    -> OrderEventRepository
      -> event publication table
  -> OrderEventResponse
```

Candid note: `EventsController` currently returns hardcoded fallback event responses if the service returns no stored event. Treat this as a rough edge and avoid copying the pattern into new production-shaped endpoints.

## Module Contract Table

| Module | Owns | Consumes | Publishes or reacts |
| --- | --- | --- | --- |
| `orders` | Order aggregate, repository, line items, order events | Issuer authorization through `orders.place` | `OrderPlacedEvent`, `OrderCancelledEvent` |
| `orders.place` | Place-order use case, issuer gateway/client | `OrderRepository`, `IssuerGateway` | Order aggregate events through save |
| `orders.cancel` | Cancel-order use case | `OrderRepository` | `OrderCancelledEvent` through aggregate |
| `orders.search` | Order lookup | `OrderRepository` | None |
| `orders.events` | Event read API | Modulith/event publication persistence | None |
| `products` | Product aggregate and create-product use case | `ProductRepository` | `ProductCreatedEvent` |
| `inventory` | Inventory reaction behavior | `OrderPlacedEvent`, `ProductCreatedEvent` | Logs simulated inventory changes |
| `purchases` | Purchase reaction behavior | `ProductCreatedEvent` | Logs simulated purchase flow |
| `logging` | Correlation, principal, traffic context | HTTP request lifecycle | MDC/structured logs |
| `security` | HTTP Basic and role configuration | Local/test users | Method and endpoint authorization |

## Cross-Module Rule

Good:

```java
@Component
class InventoryListener {

    @ApplicationModuleListener
    void on(OrderPlacedEvent event) {
        // react to a stable event contract
    }
}
```

Bad:

```java
@Component
class InventoryListener {

    private final OrderRepository orderRepository;

    void reserve(UUID orderId) {
        var order = orderRepository.findById(orderId).orElseThrow();
        // reaches into another module's persistence model
    }
}
```

The current ArchUnit rules also check that listener components do not depend on repositories.

