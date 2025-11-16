# Plan To Introduce Line Items Into Place Order Flow

## Executive Summary

This document outlines the approach to add line items (name, quantity, unitPrice, reference) to the Place Order API while maintaining backward compatibility and a single, unversioned domain model. The solution uses versioned request DTOs at the API boundary, with an assembler layer translating between API versions and the unified domain model.

---

## 1. Current State Analysis

### 1.1 Current API Structure
- **Controller**: `PlaceOrderController` uses `@PostMapping(headers = "version=1.0.0")`
- **Request DTO**: `PlaceOrderRequest` (record) with:
  - `Currency currency`
  - `Long amount` (in minor units, e.g., cents)
- **Command**: `PlaceOrderCommand` mirrors the request structure
- **Domain**: `Order` (record) and `OrderAggregate` (JPA entity) both store:
  - `UUID id`
  - `long version` (JPA optimistic locking, not API versioning)
  - `OrderStatus status`
  - `Currency currency`
  - `Long amount`

### 1.2 Current Versioning Mechanism
- Spring's `headers = "version=1.0.0"` annotation-based routing
- **Problem**: If no version header is sent, the endpoint doesn't match (404)
- **Requirement**: Default to latest version when header is absent

### 1.3 Key Constraints
- ✅ Single `Order` domain object (no versioning)
- ✅ Single `OrderAggregate` (no versioning - the `version` field is for JPA optimistic locking)
- ✅ Multiple versions of `PlaceOrderRequest` DTOs allowed
- ✅ Default to latest version when no header is sent

---

## 2. Design Philosophy

### 2.1 Core Principles

1. **Domain Stability**: The domain model (`Order`, `OrderAggregate`) remains version-agnostic. All versioning happens at the API boundary (controllers and DTOs).

2. **Backward Compatibility**: Existing clients using `version=1.0.0` continue to work without changes.

3. **Version Isolation**: Each API version has its own DTO, but they all map to the same domain model through an assembler layer.

4. **Default to Latest**: When no version header is provided, the system defaults to the latest version (V2).

5. **Additive Changes**: New versions add fields; never remove fields in-place to maintain compatibility.

### 2.2 Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                    HTTP Request Layer                        │
│  (with or without version header)                            │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              Controller Layer (Versioned)                    │
│  PlaceOrderController: method overloading with @PostMapping │
│  - @PostMapping(headers = "version=1.0.0") → V1 handler     │
│  - @PostMapping → V2 handler (default, no header required)  │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              DTO Layer (Versioned)                           │
│  PlaceOrderRequestV1 | PlaceOrderRequestV2                  │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              Mapper Layer (Version Agnostic)                  │
│  PlaceOrderCommandMapper: V1/V2 DTO → PlaceOrderCommand      │
│  (Creates synthetic line item for V1)                        │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              Service Layer (Version Agnostic)                │
│  PlaceOrderService: Command → OrderAggregate                 │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              Domain Layer (Single Version)                   │
│  OrderAggregate (with List<LineItem>)                       │
│  Order (domain record)                                      │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Detailed Design

### 3.1 Domain Model Changes

#### 3.1.0 Update OrderPlacedEvent

```java
// Update OrderPlacedEvent.java to include line items
@Value
@ToString
@Externalized("orders-events.v1.topic::#{getId().toString()}")
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public class OrderPlacedEvent {

    UUID id;
    Long version;
    Order.OrderStatus status;
    Currency currency;
    Long amount;
    List<LineItem> lineItems;  // NEW

}
```

**Rationale**:
- Include line items in the event for complete order information
- Allows event consumers to access line item details
- Maintains backward compatibility (existing consumers can ignore new field)

#### 3.1.1 Introduce LineItem Value Object

```java
// com.ead.payments.orders.LineItem.java
public record LineItem(
    String name,
    int quantity,
    Long unitPrice,  // in minor units (e.g., cents)
    String reference  // nullable
) {
    public LineItem {
        Preconditions.checkArgument(name != null && !name.isBlank(), 
            "Line item name is required");
        Preconditions.checkArgument(quantity > 0, 
            "Line item quantity must be greater than 0");
        Preconditions.checkArgument(unitPrice != null && unitPrice >= 0, 
            "Line item unit price must be non-negative");
    }
    
    public Long lineTotal() {
        return unitPrice * quantity;
    }
}
```

**Rationale**: 
- Using `Long` for prices (minor units) to match existing `amount` field pattern
- `reference` is nullable to support optional use cases
- No validation here - validation happens in `OrderAggregate` constructor

#### 3.1.2 Update OrderAggregate

```java
// Add to OrderAggregate.java
@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
private List<OrderLineItemEntity> lineItems = new ArrayList<>();

// Update constructor to accept List<LineItem>
public OrderAggregate(PlaceOrderCommand command) {
    // Existing validations
    Preconditions.checkNotNull(command.currency(), "The currency is required");
    Preconditions.checkNotNull(command.lineItems(), "Line items are required");
    // Note: Line items can be empty for V1 orders
    
    // Validate each line item
    for (LineItem item : command.lineItems()) {
        Preconditions.checkArgument(item.quantity() > 0, 
            "Line item quantity must be greater than 0");
        Preconditions.checkArgument(item.unitPrice() != null && item.unitPrice() >= 0, 
            "Line item unit price must be non-negative");
    }
    
    // Validate all line items have same currency (if currency per line item is added later)
    // For now, currency is at command level
    
    // Convert LineItems to entities
    this.lineItems = command.lineItems().stream()
        .map(item -> new OrderLineItemEntity(this, item))
        .toList();
    
    // Compute total from line items (if any)
    Long computedTotal = lineItems.stream()
        .mapToLong(OrderLineItemEntity::getLineTotal)
        .sum();
    
    // Validate that computed total matches the provided amount (if provided)
    if (command.amount() != null) {
        Preconditions.checkArgument(
            computedTotal.equals(command.amount()),
            "Line items total (%s) does not match provided amount (%s)",
            computedTotal,
            command.amount()
        );
    }
    
    // For V1 orders (empty line items), use the provided amount
    // For V2 orders, use computed total from line items
    this.amount = command.amount() != null ? command.amount() : computedTotal;
    Preconditions.checkArgument(this.amount > 0, "The amount must be greater than 0");
    
    this.id = command.id();
    this.status = OrderStatus.PLACED;
    this.currency = command.currency();
    
        registerEvent(new OrderPlacedEvent(
        command.id(),
        version,
        status,
        command.currency(),
        this.amount,
        command.lineItems()  // Include line items in event
    ));
}
```

**Rationale**:
- Keep `amount` field for query efficiency (denormalized)
- Compute `amount` from line items to ensure consistency
- Use JPA entity for line items to handle persistence

#### 3.1.3 Update Order Domain Record

```java
// Update Order.java
public record Order(
    UUID id,
    long version,
    OrderStatus status,
    Currency currency,
    Long amount,
    List<LineItem> lineItems  // NEW
) {
    // ... existing code ...
}
```

**Rationale**: 
- Add `lineItems` to domain record for complete domain representation
- Keep backward compatibility by making it a new field

#### 3.1.4 Update SearchOrderResponse

```java
// Update SearchOrderResponse.java to include line items
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchOrderResponse {

    private @NotNull UUID id;
    private @NotNull @Min(0L) Long version;
    private @NotNull Currency currency;
    private @NotNull Long amount;
    private @NotNull List<LineItemResponse> lineItems;  // NEW
}

// New DTO for line items in response
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineItemResponse {
    private @NotBlank String name;
    private @Min(1) int quantity;
    private @NotNull @Min(0L) Long unitPrice;
    private String reference;  // nullable
}
```

**Rationale**:
- Include line items in search response for complete order information
- Separate response DTO (`LineItemResponse`) for API contract clarity
- Allows clients to see line item details when retrieving orders

### 3.2 Request DTO Versioning

#### 3.2.1 PlaceOrderRequestV1 (Rename Current)

```java
// com.ead.payments.orders.place.dto.PlaceOrderRequestV1.java
public record PlaceOrderRequestV1(
    @NotNull Currency currency,
    @NotNull @Min(0L) Long amount
) {
    // This is the current PlaceOrderRequest renamed
}
```

**Rationale**: 
- Rename existing `PlaceOrderRequest` to `PlaceOrderRequestV1` for clarity
- Maintain exact same structure for backward compatibility

#### 3.2.2 PlaceOrderRequestV2 (New)

```java
// com.ead.payments.orders.place.dto.PlaceOrderRequestV2.java
public record PlaceOrderRequestV2(
    @NotNull Currency currency,
    @NotNull @Size(min = 1, max = 100) @Valid List<LineItemRequest> lineItems,
    @Min(0L) Long amount  // Optional: for validation if provided
) {
}

// com.ead.payments.orders.place.dto.LineItemRequest.java
public record LineItemRequest(
    @NotBlank String name,
    @Min(1) int quantity,
    @NotNull @Min(0L) Long unitPrice,
    String reference  // nullable, no validation needed
) {
}
```

**Rationale**:
- `amount` is optional in V2 (can be computed from line items)
- If `amount` is provided, it must match sum of line items (validation in service layer)
- Max 100 line items to prevent abuse
- `reference` is nullable to match domain model
- Bean validation annotations for DTO-level validation (Spring handles this)

### 3.3 Command Layer Update

```java
// Update PlaceOrderCommand.java
public record PlaceOrderCommand(
    UUID id,
    Currency currency,
    Long amount,  // Keep for backward compatibility during transition
    List<LineItem> lineItems  // NEW
) {
}
```

**Rationale**:
- Add `lineItems` to command
- Keep `amount` during transition (can be removed later if not needed)
- Command remains version-agnostic

### 3.4 Mapper Layer (Using MapStruct)

#### 3.4.1 Dedicated LineItemMapper

```java
// com.ead.payments.orders.place.mapping.LineItemMapper.java
@Mapper(componentModel = "spring")
public interface LineItemMapper {
    
    /**
     * Maps LineItemRequest DTO to domain LineItem.
     * MapStruct automatically generates implementation for this mapping.
     */
    @Mapping(target = "name", source = "name")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "unitPrice", source = "unitPrice")
    @Mapping(target = "reference", source = "reference")
    LineItem toLineItem(LineItemRequest dto);
    
    /**
     * Maps list of LineItemRequest DTOs to list of domain LineItems.
     * MapStruct automatically handles list mapping by calling toLineItem for each element.
     */
    List<LineItem> toLineItems(List<LineItemRequest> dtos);
}
```

**Rationale**:
- **Dedicated mapper**: Separates line item mapping concerns into its own mapper
- **Reusable**: Can be injected into other mappers that need line item conversion
- **MapStruct generated**: Automatic, type-safe, compile-time code generation

#### 3.4.2 PlaceOrderCommandMapper

```java
// com.ead.payments.orders.place.mapping.PlaceOrderCommandMapper.java
@Mapper(componentModel = "spring", uses = {LineItemMapper.class})
public interface PlaceOrderCommandMapper {
    
    /**
     * Maps V1 request to command. Uses empty list for line items.
     * No validation - validation happens in OrderAggregate constructor.
     */
    default PlaceOrderCommand toCommand(PlaceOrderRequestV1 request, UUID orderId) {
        return new PlaceOrderCommand(
            orderId,
            request.currency(),
            request.amount(),
            List.of()  // Empty list for V1
        );
    }
    
    /**
     * Maps V2 request to command. Uses LineItemMapper (injected via uses) for line item conversion.
     * No validation - validation happens in service layer and OrderAggregate constructor.
     */
    default PlaceOrderCommand toCommand(PlaceOrderRequestV2 request, UUID orderId, LineItemMapper lineItemMapper) {
        // Use injected LineItemMapper to convert DTO line items to domain LineItems
        List<LineItem> lineItems = lineItemMapper.toLineItems(request.lineItems());
        
        // Compute total from line items
        Long computedTotal = lineItems.stream()
            .mapToLong(LineItem::lineTotal)
            .sum();
        
        return new PlaceOrderCommand(
            orderId,
            request.currency(),
            computedTotal,
            lineItems
        );
    }
}
```

**Alternative: Using MapStruct's automatic injection (recommended)**

```java
// com.ead.payments.orders.place.mapping.PlaceOrderCommandMapper.java
@Mapper(componentModel = "spring", uses = {LineItemMapper.class})
public interface PlaceOrderCommandMapper {
    
    /**
     * Maps V1 request to command. Uses empty list for line items.
     * No validation - validation happens in OrderAggregate constructor.
     */
    default PlaceOrderCommand toCommand(PlaceOrderRequestV1 request, UUID orderId) {
        return new PlaceOrderCommand(
            orderId,
            request.currency(),
            request.amount(),
            List.of()  // Empty list for V1
        );
    }
    
    /**
     * Maps V2 request to command.
     * MapStruct automatically injects LineItemMapper (via uses annotation) into generated implementation.
     * The generated code will call lineItemMapper.toLineItems() for the lineItems field.
     */
    @Mapping(target = "id", ignore = true)  // id is provided separately
    @Mapping(target = "amount", expression = "java(computeTotalFromLineItems(request.lineItems()))")
    @Mapping(target = "lineItems", source = "lineItems", qualifiedByName = "toLineItems")
    PlaceOrderCommand toCommand(PlaceOrderRequestV2 request, UUID orderId);
    
    /**
     * Helper method to compute total from line items.
     * Used in @Mapping expression.
     */
    default Long computeTotalFromLineItems(List<LineItemRequest> lineItems) {
        return lineItems.stream()
            .mapToLong(item -> item.unitPrice() * item.quantity())
            .sum();
    }
}
```

**Note**: Since MapStruct's `uses` annotation injects the mapper at the implementation level, and we're using default methods for V1, we have two options:
1. Use default methods for both V1 and V2, manually injecting LineItemMapper as a parameter (shown in first example)
2. Use MapStruct's automatic mapping for V2 with `@Mapping` annotations, which will automatically use the injected LineItemMapper (shown in second example)

**Recommended approach**: Use default methods for both versions and manually inject LineItemMapper:

```java
// com.ead.payments.orders.place.mapping.PlaceOrderCommandMapper.java
@Mapper(componentModel = "spring", uses = {LineItemMapper.class})
public interface PlaceOrderCommandMapper {
    
    /**
     * Maps V1 request to command. Uses empty list for line items.
     */
    default PlaceOrderCommand toCommand(PlaceOrderRequestV1 request, UUID orderId) {
        return new PlaceOrderCommand(
            orderId,
            request.currency(),
            request.amount(),
            List.of()  // Empty list for V1
        );
    }
    
    /**
     * Maps V2 request to command. Uses injected LineItemMapper for line item conversion.
     * MapStruct will inject LineItemMapper into the generated implementation,
     * and we can access it via a default method that receives it as parameter,
     * or we can use @AfterMapping to access it.
     * 
     * However, since we're using default methods, the cleanest approach is to
     * manually inject LineItemMapper in the controller and pass it here.
     * 
     * Alternatively, we can use MapStruct's @Context annotation pattern.
     */
    default PlaceOrderCommand toCommand(PlaceOrderRequestV2 request, UUID orderId, LineItemMapper lineItemMapper) {
        // Use injected LineItemMapper to convert DTO line items to domain LineItems
        List<LineItem> lineItems = lineItemMapper.toLineItems(request.lineItems());
        
        // Compute total from line items
        Long computedTotal = lineItems.stream()
            .mapToLong(LineItem::lineTotal)
            .sum();
        
        return new PlaceOrderCommand(
            orderId,
            request.currency(),
            computedTotal,
            lineItems
        );
    }
}
```

**Rationale**:
- **Uses MapStruct**: Leverages MapStruct's annotation processing for type-safe, compile-time mapping
- **Dedicated LineItemMapper**: Separates concerns, reusable, injectable via `uses` annotation
- **Empty list for V1**: No synthetic line item - V1 orders have empty line items list
- **Automatic injection**: MapStruct's `uses` annotation automatically injects LineItemMapper into generated implementation
- **No validation in mapper** - pure transformation only
- Method overloading (same name, different parameters) eliminates need for version suffixes

### 3.5 Search Order Controller Updates

```java
// Update SearchOrderController.java
@GetMapping(path = "/{order_id}", headers = "version=1.0.0")
@ResponseStatus(HttpStatus.OK)
public Optional<SearchOrderResponse> searchOrder(@PathVariable("order_id") @NotNull UUID orderId) {
    return searchOrderService.search(orderId)
            .map(order -> new SearchOrderResponse(
                    order.id(),
                    order.version(),
                    order.currency(),
                    order.amount(),
                    order.lineItems().stream()
                        .map(item -> new LineItemResponse(
                            item.name(),
                            item.quantity(),
                            item.unitPrice(),
                            item.reference()
                        ))
                        .toList()
            ));
}
```

**Rationale**:
- Include line items in search response
- Convert domain `LineItem` to response DTO `LineItemResponse`
- Maintains backward compatibility (existing clients can ignore new field if needed)

### 3.6 Controller Strategy

**Using Spring's Built-in Header-Based Routing (No ApiVersionResolver Needed)**

Spring Boot's `@PostMapping` with `headers` attribute provides built-in version routing. We use method overloading with different parameter types to handle different versions:

```java
// Update PlaceOrderController.java
@RestController
@RequestMapping(path = "/orders")
@RequiredArgsConstructor
@RolesAllowed({"ROLE_MERCHANT", "ROLE_CUSTOMER"})
public class PlaceOrderController {
    
    private final PlaceOrderService placeOrderService;
    private final PlaceOrderCommandMapper commandMapper;
    private final LineItemMapper lineItemMapper;
    
    /**
     * V1 endpoint: Explicit version header required.
     * Method overloading allows same method name with different parameter types.
     */
    @PostMapping(headers = "version=1.0.0")
    @ResponseStatus(HttpStatus.CREATED)
    @Observed(
        name = "http.orders.create",
        contextualName = "POST /orders",
        lowCardinalityKeyValues = {"version", "1.0.0"}
    )
    public PlaceOrderResponse placeOrder(
            @RequestBody @Valid @NotNull PlaceOrderRequestV1 request) {
        
        UUID orderId = UUID.randomUUID();
        var command = commandMapper.toCommand(request, orderId);
        var order = placeOrderService.handle(command);
        
        return new PlaceOrderResponse(
            order.id(),
            order.currency(),
            order.amount()
        );
    }
    
    /**
     * V2 endpoint: Default (no version header required).
     * Spring will route requests without version header to this method.
     * Method overloading with different parameter type (PlaceOrderRequestV2).
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Observed(
        name = "http.orders.create",
        contextualName = "POST /orders",
        lowCardinalityKeyValues = {"version", "2.0.0"}
    )
    public PlaceOrderResponse placeOrder(
            @RequestBody @Valid @NotNull PlaceOrderRequestV2 request) {
        
        UUID orderId = UUID.randomUUID();
        var command = commandMapper.toCommand(request, orderId, lineItemMapper);
        var order = placeOrderService.handle(command);
        
        return new PlaceOrderResponse(
            order.id(),
            order.currency(),
            order.amount()
        );
    }
}
```

**Rationale**:
- **No ApiVersionResolver needed**: Spring's header matching handles routing automatically
- **Method overloading**: Same method name `placeOrder()` with different parameter types (`PlaceOrderRequestV1` vs `PlaceOrderRequestV2`)
- **Default behavior**: `@PostMapping` without `headers` attribute matches requests without version header → defaults to V2
- **Explicit V1**: `@PostMapping(headers = "version=1.0.0")` matches requests with V1 header
- **Version tags in @Observed**: Added `lowCardinalityKeyValues = {"version", "1.0.0"}` and `{"version", "2.0.0"}` for observability
- **Clean separation**: Each version has its own method, making the code explicit and easy to understand

### 3.6 Persistence Layer

#### 3.6.1 Database Schema

```sql
-- Migration: V{timestamp}__add_line_items.sql

-- Create line_items table
CREATE TABLE IF NOT EXISTS orders.line_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price_minor_units BIGINT NOT NULL CHECK (unit_price_minor_units >= 0),
    reference VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders.orders(order_id) ON DELETE CASCADE,
    UNIQUE (order_id, name, reference)  -- Optional: prevent duplicates
);

-- Index for efficient order lookup
CREATE INDEX IF NOT EXISTS idx_line_items_order_id ON orders.line_items(order_id);

-- Note: orders.amount remains for query efficiency (denormalized)
-- Consistency ensured via application logic
```

#### 3.6.2 JPA Entity

```java
// com.ead.payments.orders.OrderLineItemEntity.java
@Entity
@Table(name = "line_items", schema = "orders")
@Data
@NoArgsConstructor
public class OrderLineItemEntity {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderAggregate order;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "unit_price_minor_units", nullable = false)
    private Long unitPrice;
    
    @Column
    private String reference;
    
    public OrderLineItemEntity(OrderAggregate order, LineItem lineItem) {
        this.order = order;
        this.name = lineItem.name();
        this.quantity = lineItem.quantity();
        this.unitPrice = lineItem.unitPrice();
        this.reference = lineItem.reference();
    }
    
    public Long getLineTotal() {
        return unitPrice * quantity;
    }
}
```

**Rationale**:
- Separate entity for persistence concerns
- Cascade delete ensures line items are removed with order
- Optional unique constraint prevents duplicate line items per order

### 3.7 Service Layer Updates

```java
// Update PlaceOrderService.java
@Service
@RequiredArgsConstructor
public class PlaceOrderService {
    
    final OrderAggregateMapper orderAggregateMapper;
    final OrderRepository orderRepository;
    final IssuerService issuerService;
    
    public Order handle(PlaceOrderCommand command) {
        // Cross-entity validation (if needed)
        // Example: Validate order against issuer service limits, customer limits, etc.
        // This is validation that spans multiple entities/aggregates
        
        // Note: If V2 request provided an amount, validation that it matches computed total
        // would happen here (spans request DTO and command). However, since we compute
        // the total in the mapper and the command already has the computed total, this
        // validation is not needed unless we want to validate against a provided amount
        // that was passed separately. For now, we trust the mapper's computation.
        
        // Authorization (unchanged)
        Authorization authorization = issuerService.authorize(command);
        if (authorization instanceof RejectedAuthorization(String status, String statusReason)) {
            throw new IssuerDeclinedException(
                "Authorization was rejected with status: " + status + 
                " and state reason: " + statusReason
            );
        }
        
        // Additional cross-entity validations can go here
        // Example: Check if customer has exceeded order limit
        // Example: Check if total amount exceeds merchant limits
        
        // Create aggregate (validations for single entity happen in OrderAggregate constructor)
        OrderAggregate aggregate = new OrderAggregate(command);
        
        // Save (line items cascade)
        aggregate = orderRepository.save(aggregate);
        
        return orderAggregateMapper.toOrder(aggregate);
    }
}
```

**Rationale**: 
- Service layer remains version-agnostic
- Works with `PlaceOrderCommand` that now includes line items
- **Cross-entity validations**: Only validations that span multiple entities/aggregates go here
- **Single-entity validations**: All validations for OrderAggregate and its properties go in `OrderAggregate` constructor
- No version-specific logic in service

### 3.8 Mapper Updates

```java
// Update OrderAggregateMapper.java
@Mapper(componentModel = "spring")
public interface OrderAggregateMapper {
    
    /**
     * Maps OrderAggregate to Order domain record.
     * MapStruct automatically handles simple field mappings.
     */
    @Mapping(target = "lineItems", source = "lineItems")
    Order toOrder(OrderAggregate orderAggregate);
    
    /**
     * Maps OrderLineItemEntity to LineItem value object.
     * MapStruct automatically generates this mapping.
     */
    @Mapping(target = "name", source = "name")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "unitPrice", source = "unitPrice")
    @Mapping(target = "reference", source = "reference")
    LineItem toLineItem(OrderLineItemEntity entity);
    
    /**
     * Maps list of OrderLineItemEntity to list of LineItem.
     * MapStruct automatically handles list mapping.
     */
    List<LineItem> toLineItems(List<OrderLineItemEntity> entities);
}
```

**Rationale**: 
- **Uses MapStruct**: Leverages MapStruct's automatic mapping generation
- **Type-safe**: Compile-time checking ensures correct mappings
- **Efficient**: Generated code has no reflection overhead
- MapStruct automatically handles the list conversion by calling `toLineItem` for each element

---

## 4. Implementation Steps

### Phase 1: Domain Model (No Breaking Changes)
1. ✅ Create `LineItem` value object
2. ✅ Create `OrderLineItemEntity` JPA entity
3. ✅ Add database migration for `line_items` table
4. ✅ Update `OrderAggregate` to include line items (backward compatible)
5. ✅ Update `Order` domain record to include line items
6. ✅ Update `PlaceOrderCommand` to include line items

### Phase 2: API Versioning Infrastructure
7. ✅ Rename `PlaceOrderRequest` → `PlaceOrderRequestV1`
8. ✅ Create `PlaceOrderRequestV2` and `LineItemRequest`
9. ✅ Create `LineItemMapper` using MapStruct
10. ✅ Create `PlaceOrderCommandMapper` using MapStruct with `uses = {LineItemMapper.class}`
11. ✅ Update `OrderPlacedEvent` to include line items
12. ✅ Update `SearchOrderResponse` to include line items

### Phase 3: Controller Updates
13. ✅ Update `PlaceOrderController` with method overloading (V1 and V2 endpoints)
14. ✅ Add version tags to `@Observed` annotations
15. ✅ Update `OrderAggregateMapper` to map line items using MapStruct
16. ✅ Update `SearchOrderController` to include line items in response
17. ✅ Add validations to `OrderAggregate` constructor (including line items total match)
18. ✅ Add cross-entity validations to `PlaceOrderService` (if needed)

### Phase 4: Testing
15. ✅ Unit tests for `PlaceOrderCommandMapper` (V1 and V2 paths, MapStruct mappings)
16. ✅ Unit tests for `OrderAggregate` constructor validations (including line items total match validation)
17. ✅ Integration tests for V1 endpoint (backward compatibility)
18. ✅ Integration tests for V2 endpoint (with line items)
19. ✅ Integration tests for default version (no header → V2)
20. ✅ **Integration test: Place order with line items → verify OrderPlacedEvent contains line items → GET order → verify line items in response**

### Phase 5: Documentation
19. ✅ Update OpenAPI/Swagger documentation
20. ✅ Add migration guide for API clients
21. ✅ Document versioning strategy

---

## 5. Backward Compatibility Strategy

### 5.1 V1 Clients
- **No changes required**: Existing clients using `version=1.0.0` header continue to work
- **Behavior**: V1 requests create orders with empty line items list (lineItems = [])

### 5.2 New Clients
- **Default behavior**: No version header → V2 (latest)
- **Explicit versioning**: Can still use `version=1.0.0` or `version=2.0.0`

### 5.3 Migration Path
1. Deploy V2 support (V1 still works)
2. Monitor usage: track V1 vs V2 adoption
3. Communicate deprecation timeline (e.g., V1 deprecated in 6 months)
4. Eventually remove V1 endpoint (assembler logic can remain for data migration)

---

## 6. Validation Rules

### 6.1 V1 Validation
- `currency`: required, not null
- `amount`: required, >= 0

### 6.2 V2 Validation
- **DTO-level (Bean Validation)**: Handled by Spring's `@Valid` annotation
  - `currency`: required, not null
  - `lineItems`: required, size 1-100, each item validated
  - `amount`: optional
  - Line item validation:
    - `name`: required, not blank
    - `quantity`: required, >= 1
    - `unitPrice`: required, >= 0
    - `reference`: optional

### 6.3 Domain Validation (OrderAggregate Constructor)
- **Single-entity validations** (happen in `OrderAggregate` constructor):
  - Currency: required, not null
  - Line items: can be empty (for V1 orders) or not empty (for V2 orders)
  - Each line item (if present): quantity > 0, unitPrice >= 0
  - **Line items total must match provided amount** (if amount is provided in command and line items are not empty)
  - Total amount: must be > 0 (from provided amount for V1, or computed from line items for V2)

### 6.4 Service Layer Validation (Cross-Entity)
- **Cross-entity validations** (happen in `PlaceOrderService.handle()`):
  - If amount provided in V2 request, validate it matches computed total (spans request DTO and command)
  - Authorization checks (spans Order and IssuerService)
  - Customer order limits (spans Order and Customer entities)
  - Merchant limits (spans Order and Merchant entities)

---

## 7. Error Handling

### 7.1 Error Codes
- `LINE_ITEMS_EMPTY`: V2 request has empty line items list
- `LINE_ITEM_INVALID_QUANTITY`: quantity <= 0
- `LINE_ITEM_INVALID_UNIT_PRICE`: unitPrice < 0
- `LINE_ITEMS_TOTAL_MISMATCH`: provided amount doesn't match computed total
- `LINE_ITEMS_TOO_MANY`: more than 100 line items
- `MULTI_CURRENCY_NOT_SUPPORTED`: line items have different currencies

### 7.2 Error Response Format
Maintain existing error response format (likely ProblemDetail based on `OrderControlAdvice`).

---

## 8. Testing Strategy

### 8.1 Unit Tests
- `PlaceOrderCommandMapperTest`: 
  - Test V1 → empty line items list mapping
  - Test V2 → real line items mapping using LineItemMapper
  - No validation tests - mapper is pure transformation
- `LineItemMapperTest`:
  - Test MapStruct-generated `toLineItem` and `toLineItems` methods
  - Test mapping from LineItemRequest to LineItem
  - Test list mapping
- `OrderAggregateTest`: 
  - Test line item aggregation, total computation
  - Test constructor validations (including line items total must match provided amount)
  - Test validation failure when line items total doesn't match amount
- `PlaceOrderServiceTest`: Test cross-entity validations (if any)
- `OrderAggregateMapperTest`: Test MapStruct mapping from OrderAggregate to Order (including line items)

### 8.2 Integration Tests
- `PlaceOrderControllerTest`: Test V1 endpoint, V2 endpoint, default version resolution
- Test backward compatibility: existing V1 clients still work
- Test new V2 clients with multiple line items
- **Test: Place order with line items → verify event → GET order → verify response**
  - Place order via POST with line items (V2)
  - Verify `OrderPlacedEvent` is published with correct line items
  - Retrieve order via GET endpoint
  - Verify `SearchOrderResponse` contains the same line items

#### 8.2.1 Example Integration Test: Line Items End-to-End

```java
// PlaceOrderWithLineItemsIntegrationTest.java
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("PlaceOrderWithLineItemsIntegrationTest")
class PlaceOrderWithLineItemsIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Test
    @DisplayName("Should place order with line items and retrieve them via GET When order is created with V2 request")
    void shouldPlaceOrderWithLineItemsAndRetrieveThemViaGetWhenV2Request() throws Exception {
        // Given: V2 request with line items
        PlaceOrderRequestV2 request = new PlaceOrderRequestV2(
            Currency.getInstance("USD"),
            List.of(
                new LineItemRequest("Product A", 2, 1000L, "REF-001"),
                new LineItemRequest("Product B", 1, 2000L, "REF-002")
            ),
            null  // amount not provided, should be computed
        );
        
        // When: Place order
        var placeOrderResult = mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();
        
        PlaceOrderResponse placeResponse = objectMapper.readValue(
            placeOrderResult.getResponse().getContentAsString(),
            PlaceOrderResponse.class
        );
        UUID orderId = placeResponse.id();
        
        // Then: Verify OrderPlacedEvent contains line items
        // (This would typically use Spring Modulith's Scenario or EventListener verification)
        // For example, using @ApplicationModuleTest:
        // scenario.publish(new OrderPlacedEvent(...))
        //   .andWaitForEventOfType(OrderPlacedEvent.class)
        //   .matching(event -> event.getLineItems().size() == 2)
        //   .toArrive();
        
        // When: Retrieve order via GET
        var getOrderResult = mockMvc.perform(get("/orders/{order_id}", orderId)
                .header("version", "1.0.0"))
            .andExpect(status().isOk())
            .andReturn();
        
        SearchOrderResponse searchResponse = objectMapper.readValue(
            getOrderResult.getResponse().getContentAsString(),
            SearchOrderResponse.class
        );
        
        // Then: Verify response contains line items
        assertThat(searchResponse.getLineItems()).hasSize(2);
        assertThat(searchResponse.getLineItems().get(0).getName()).isEqualTo("Product A");
        assertThat(searchResponse.getLineItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(searchResponse.getLineItems().get(0).getUnitPrice()).isEqualTo(1000L);
        assertThat(searchResponse.getLineItems().get(0).getReference()).isEqualTo("REF-001");
        assertThat(searchResponse.getLineItems().get(1).getName()).isEqualTo("Product B");
        assertThat(searchResponse.getLineItems().get(1).getQuantity()).isEqualTo(1);
        assertThat(searchResponse.getLineItems().get(1).getUnitPrice()).isEqualTo(2000L);
        assertThat(searchResponse.getLineItems().get(1).getReference()).isEqualTo("REF-002");
        
        // Verify total matches sum of line items
        Long expectedTotal = (2 * 1000L) + (1 * 2000L); // 4000L
        assertThat(searchResponse.getAmount()).isEqualTo(expectedTotal);
    }
}
```

**Rationale**:
- Tests the complete flow: POST → Event → GET
- Verifies line items are persisted and retrieved correctly
- Validates that line items in event match line items in response
- Ensures total amount matches sum of line items

### 8.3 Migration Tests
- Test database migration: schema created correctly
- Test data persistence: line items saved and retrieved correctly

---

## 9. Risks and Mitigations

### 9.1 Risk: Version Header Parsing Issues
**Mitigation**: Using Spring's built-in header matching eliminates custom parsing logic. Default endpoint (no header) routes to V2.

### 9.2 Risk: Total Mismatch Between Amount and Line Items
**Mitigation**: Validation in service layer (if amount provided in V2 request), domain invariants in OrderAggregate constructor

### 9.3 Risk: Performance with Many Line Items
**Mitigation**: Max 100 line items limit, indexed queries

### 9.4 Risk: Backward Compatibility Breaks
**Mitigation**: Maintain V1 endpoint, thorough integration testing

---

## 10. Future Considerations

### 10.1 Potential Enhancements
- Remove `amount` field from V2 request (compute only)
- Add line item-level discounts
- Support line item metadata
- Add idempotency key support

### 10.2 Deprecation Timeline
- **Month 0-3**: V1 and V2 both supported
- **Month 3-6**: Communicate V1 deprecation
- **Month 6+**: Remove V1 endpoint (keep assembler for data migration)

---

## 11. Summary

This plan introduces line items support while maintaining:
- ✅ Single, unversioned domain model
- ✅ Backward compatibility for existing clients
- ✅ Default to latest version when no header is sent (via Spring's header matching)
- ✅ Clean separation of concerns (versioning at API boundary)
- ✅ Validation in the right place: single-entity in aggregate, cross-entity in service
- ✅ No custom version resolver needed (uses Spring's built-in capabilities)

The key insights:
1. **Versioning is an API concern, not a domain concern**: By using a mapper layer, we translate versioned DTOs into a unified domain model.
2. **Spring's header matching is sufficient**: No need for custom ApiVersionResolver - method overloading with different parameter types handles version routing elegantly.
3. **Validation placement matters**: Single-entity validations belong in the aggregate constructor; cross-entity validations belong in the service layer.
