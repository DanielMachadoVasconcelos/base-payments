# Object Tree - Orders with Line Items

## Overview
This document shows the complete object hierarchy and relationships in the orders system with line items support.

---

## API Layer (Request DTOs)

```
com.ead.payments.orders.place.request/
├── PlaceOrderRequestV1 (record)
│   ├── Currency currency
│   └── Long amount
│
├── PlaceOrderRequestV2 (record)
│   ├── Currency currency
│   ├── List<LineItemRequest> lineItems
│   └── Long amount (optional)
│
└── LineItemRequest (record)
    ├── String name
    ├── int quantity
    ├── Long unitPrice
    └── String reference (nullable)
```

---

## API Layer (Response DTOs)

```
com.ead.payments.orders.place.response/
├── PlaceOrderResponseV1 (class)
│   ├── UUID id
│   ├── Currency currency
│   └── Long amount
│
└── PlaceOrderResponseV2 (class)
    ├── UUID id
    ├── Currency currency
    ├── Long amount
    └── List<LineItemResponse> lineItems
        └── (uses com.ead.payments.orders.response.LineItemResponse)

com.ead.payments.orders.search/
└── SearchOrderResponse (class)
    ├── UUID id
    ├── Long version
    ├── Currency currency
    ├── Long amount
    └── List<LineItemResponse> lineItems
        └── (uses com.ead.payments.orders.response.LineItemResponse)

com.ead.payments.orders.response/
└── LineItemResponse (record) ⭐ SINGLE SOURCE OF TRUTH
    ├── String name
    ├── int quantity
    ├── Long unitPrice
    └── String reference (nullable)
```

---

## Mapping Layer (MapStruct Mappers)

```
com.ead.payments.orders.place.mapping/
├── LineItemMapper
│   ├── LineItem toLineItem(LineItemRequest) 
│   └── List<LineItem> toLineItems(List<LineItemRequest>)
│
├── PlaceOrderCommandMapper (uses LineItemMapper)
│   ├── PlaceOrderCommand toCommand(PlaceOrderRequestV1, UUID)
│   │   └── maps to PlaceOrderCommand with empty lineItems
│   └── PlaceOrderCommand toCommand(PlaceOrderRequestV2, UUID)
│       └── maps to PlaceOrderCommand with computed amount from lineItems
│
└── PlaceOrderResponseMapper (uses LineItemResponseMapper)
    ├── PlaceOrderResponseV1 toResponseV1(Order)
    └── PlaceOrderResponseV2 toResponseV2(Order)
        └── uses LineItemResponseMapper.from() for lineItems

com.ead.payments.orders.search.mapping/
└── SearchOrderResponseMapper (uses LineItemResponseMapper)
    └── SearchOrderResponse from(Order)
        └── uses LineItemResponseMapper.from() for lineItems

com.ead.payments.orders.mapping/
└── LineItemResponseMapper ⭐ SINGLE MAPPER FOR LineItemResponse
    ├── LineItemResponse from(LineItem)
    └── List<LineItemResponse> from(List<LineItem>)

com.ead.payments.orders/
└── OrderAggregateMapper
    ├── Order toOrder(OrderAggregate)
    ├── LineItem toLineItem(OrderLineItemEntity)
    └── List<LineItem> toLineItems(List<OrderLineItemEntity>)
```

---

## Command Layer

```
com.ead.payments.orders.place/
└── PlaceOrderCommand (record)
    ├── UUID id
    ├── Currency currency
    ├── Long amount (for backward compatibility)
    └── List<LineItem> lineItems
```

---

## Domain Layer (Value Objects & Domain Records)

```
com.ead.payments.orders/
├── LineItem (record) ⭐ DOMAIN VALUE OBJECT
│   ├── String name
│   ├── int quantity
│   ├── Long unitPrice
│   ├── String reference (nullable)
│   └── Long lineTotal() [computed: unitPrice * quantity]
│
└── Order (record) ⭐ DOMAIN RECORD
    ├── UUID id
    ├── long version
    ├── OrderStatus status
    ├── Currency currency
    ├── Long amount
    └── List<LineItem> lineItems
```

---

## Persistence Layer (JPA Entities)

```
com.ead.payments.orders/
├── OrderAggregate (JPA Entity) ⭐ AGGREGATE ROOT
│   ├── @Id UUID id
│   ├── @Version Long version
│   ├── OrderStatus status
│   ├── Currency currency
│   ├── Long amount
│   └── @OneToMany List<OrderLineItemEntity> lineItems
│       └── (cascade = ALL, orphanRemoval = true)
│
└── OrderLineItemEntity (JPA Entity)
    ├── @Id UUID id
    ├── @ManyToOne OrderAggregate order
    ├── String name
    ├── Integer quantity
    ├── Long unitPrice
    ├── String reference
    └── Long getLineTotal() [computed: unitPrice * quantity]
```

---

## Flow Diagram

### Place Order Flow (V1 - No Line Items)

```
1. HTTP POST /orders (with header: version=1.0.0)
   └── PlaceOrderRequestV1
       │
       ▼
2. PlaceOrderCommandMapper.toCommand(PlaceOrderRequestV1, UUID)
   └── PlaceOrderCommand
       │   ├── currency: from request
       │   ├── amount: from request
       │   └── lineItems: empty list []
       │
       ▼
3. PlaceOrderService.handle(PlaceOrderCommand)
   └── new OrderAggregate(command)
       │   └── creates OrderAggregate with empty lineItems
       │
       ▼
4. OrderAggregateMapper.toOrder(OrderAggregate)
   └── Order (domain record)
       │   └── lineItems: empty list []
       │
       ▼
5. PlaceOrderResponseMapper.toResponseV1(Order)
   └── PlaceOrderResponseV1
       └── (no lineItems in response)
```

### Place Order Flow (V2 - With Line Items)

```
1. HTTP POST /orders (no version header = default V2)
   └── PlaceOrderRequestV2
       │   ├── currency
       │   ├── lineItems: List<LineItemRequest>
       │   └── amount: optional
       │
       ▼
2. PlaceOrderCommandMapper.toCommand(PlaceOrderRequestV2, UUID)
   │   ├── uses LineItemMapper.toLineItems() 
   │   │   └── converts List<LineItemRequest> → List<LineItem>
   │   │
   │   └── computes amount from lineItems (if not provided)
   │
   └── PlaceOrderCommand
       │   ├── currency: from request
       │   ├── amount: computed from lineItems
       │   └── lineItems: List<LineItem> (domain objects)
       │
       ▼
3. PlaceOrderService.handle(PlaceOrderCommand)
   └── new OrderAggregate(command)
       │   ├── validates lineItems
       │   ├── creates OrderLineItemEntity for each LineItem
       │   ├── computes total from lineItems
       │   └── validates amount matches computed total
       │
       └── OrderAggregate (JPA Entity)
           └── lineItems: List<OrderLineItemEntity>
           │
           ▼
4. OrderAggregateMapper.toOrder(OrderAggregate)
   │   ├── uses OrderAggregateMapper.toLineItems()
   │   │   └── converts List<OrderLineItemEntity> → List<LineItem>
   │   │
   └── Order (domain record)
       └── lineItems: List<LineItem>
       │
       ▼
5. PlaceOrderResponseMapper.toResponseV2(Order)
   │   ├── uses LineItemResponseMapper.from()
   │   │   └── converts List<LineItem> → List<LineItemResponse>
   │   │
   └── PlaceOrderResponseV2
       └── lineItems: List<LineItemResponse>
```

### Search Order Flow

```
1. HTTP GET /orders/{order_id}
   │
   ▼
2. SearchOrderService.search(UUID)
   └── OrderRepository.findById(UUID)
       └── OrderAggregate (JPA Entity)
           │
           ▼
3. OrderAggregateMapper.toOrder(OrderAggregate)
   │   ├── uses OrderAggregateMapper.toLineItems()
   │   │   └── converts List<OrderLineItemEntity> → List<LineItem>
   │   │
   └── Order (domain record)
       └── lineItems: List<LineItem>
       │
       ▼
4. SearchOrderResponseMapper.from(Order)
   │   ├── uses LineItemResponseMapper.from()
   │   │   └── converts List<LineItem> → List<LineItemResponse>
   │   │
   └── SearchOrderResponse
       └── lineItems: List<LineItemResponse>
```

---

## Key Relationships

### 1. LineItemResponse (Single Source of Truth)
- **Location**: `com.ead.payments.orders.response.LineItemResponse`
- **Used by**:
  - `PlaceOrderResponseV2.lineItems`
  - `SearchOrderResponse.lineItems`
- **Mapped by**: `LineItemResponseMapper.from(LineItem)`

### 2. LineItem (Domain Value Object)
- **Location**: `com.ead.payments.orders.LineItem`
- **Used in**:
  - `Order.lineItems` (domain record)
  - `PlaceOrderCommand.lineItems` (command)
  - `OrderPlacedEvent.lineItems` (event)
- **Mapped from**:
  - `LineItemRequest` → `LineItem` (via `LineItemMapper`)
  - `OrderLineItemEntity` → `LineItem` (via `OrderAggregateMapper`)

### 3. OrderLineItemEntity (Persistence Entity)
- **Location**: `com.ead.payments.orders.OrderLineItemEntity`
- **Relationship**: `@ManyToOne OrderAggregate`
- **Stored in**: `orders.line_items` table
- **Created from**: `LineItem` (in `OrderAggregate` constructor)

### 4. Mapping Chain
```
LineItemRequest → LineItemMapper → LineItem
LineItem → OrderAggregate constructor → OrderLineItemEntity
OrderLineItemEntity → OrderAggregateMapper → LineItem
LineItem → LineItemResponseMapper → LineItemResponse
```

---

## Package Structure

```
com.ead.payments.orders/
├── [Domain Layer]
│   ├── LineItem.java (record)
│   ├── Order.java (record)
│   ├── OrderAggregate.java (JPA Entity)
│   └── OrderLineItemEntity.java (JPA Entity)
│
├── place/
│   ├── [Command]
│   │   └── PlaceOrderCommand.java
│   │
│   ├── request/
│   │   ├── PlaceOrderRequestV1.java
│   │   ├── PlaceOrderRequestV2.java
│   │   └── LineItemRequest.java
│   │
│   ├── response/
│   │   ├── PlaceOrderResponseV1.java
│   │   └── PlaceOrderResponseV2.java
│   │
│   └── mapping/
│       ├── LineItemMapper.java
│       ├── PlaceOrderCommandMapper.java
│       └── PlaceOrderResponseMapper.java
│
├── search/
│   ├── SearchOrderResponse.java
│   └── mapping/
│       └── SearchOrderResponseMapper.java
│
├── response/
│   └── LineItemResponse.java ⭐ SINGLE SOURCE
│
└── mapping/
    ├── OrderAggregateMapper.java
    └── LineItemResponseMapper.java ⭐ SINGLE MAPPER
```

---

## Summary

1. **API Boundary**: Versioned request/response DTOs (`V1`, `V2`)
2. **Domain Model**: Single, unversioned domain model (`Order`, `LineItem`)
3. **Persistence**: JPA entities (`OrderAggregate`, `OrderLineItemEntity`)
4. **Mapping**: MapStruct mappers convert between layers
5. **LineItemResponse**: Single shared response DTO used by both place and search endpoints
6. **LineItemResponseMapper**: Single mapper for converting `LineItem` → `LineItemResponse`
