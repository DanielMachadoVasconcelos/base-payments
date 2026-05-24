# Testing Playbook

Use this guide when adding, changing, or debugging tests.

## Test Stack

- JUnit 5.
- Spring Boot test support.
- MockMvc for HTTP/controller flows.
- WireMock Spring Boot for issuer stubs.
- Spring Security test support.
- ArchUnit for architecture rules.
- JaCoCo for coverage reports.

## Commands

For integration tests locally:

```bash
docker compose up -d
./gradlew test
```

For a CI-like infrastructure subset:

```bash
docker compose -f docker-ci.yml up -d
./gradlew test -Djava.compiler.args="--enable-preview" --parallel
```

For documentation-only changes, do not claim tests passed unless they were actually run. A file/link review is usually enough.

## Integration Test Base

Controller/integration tests should usually extend:

```java
class PlaceOrdersControllerTest extends SpringBootIntegrationTest {
}
```

`SpringBootIntegrationTest` provides:

- `@SpringBootTest`
- `@AutoConfigureMockMvc`
- `@ActiveProfiles("integration-test")`
- WireMock server named `issuer-service`
- `issuerService()` helper for stubbing issuer responses

## Issuer Stubbing

Good:

```java
CorrelationId expectedCorrelationId = CorrelationId.random();

TestMocks.setup(issuerService())
        .toAcceptTheAuthorizationWith(expectedCorrelationId);

mockMvc.perform(post("/orders")
        .header("X-Correlation-ID", expectedCorrelationId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)));
```

Why this is good:

- The stub and request share the same correlation id.
- The external issuer behavior is explicit in the test.
- The test exercises the HTTP edge and issuer boundary together.

Bad:

```java
mockMvc.perform(post("/orders")
        .content(objectMapper.writeValueAsString(request)));
```

Why this is bad for issuer-backed flows:

- It hides the external dependency.
- WireMock matching may fail or test behavior may become accidental.
- It loses correlation-id coverage.

## Security In Tests

Use `@WithMockUser` for secured controller tests:

```java
@Test
@WithMockUser(username = "user", roles = "USER")
@DisplayName("Should allow to place an order when line items are provided")
void shouldAllowToPlaceAnOrderWhenLineItemsAreProvided() {
}
```

Use role intent from the controller:

- Order endpoints: `ROLE_MERCHANT` or `ROLE_CUSTOMER`.
- Product creation: `ROLE_ADMIN`.

## Naming Rules

Test methods must match:

```text
should.*When.*
```

And they must have `@DisplayName`.

Good:

```java
@Test
@DisplayName("Should reject order when amount is negative")
void shouldRejectOrderWhenAmountIsNegative() {
}
```

Bad:

```java
@Test
void rejectsNegativeAmount() {
}
```

The bad example fails the architecture convention.

## JSON Assertions

JSON uses `snake_case`.

Good:

```java
response.andExpect(jsonPath("$.line_items[0].unit_price", is(1000)));
```

Bad:

```java
response.andExpect(jsonPath("$.lineItems[0].unitPrice", is(1000)));
```

## Architecture Tests

Architecture tests live in `src/test/java/com/ead/payments/architecture`.

They enforce:

- Allowed class name suffixes.
- English/ASCII class names.
- No underscores in type names.
- Suffix-to-annotation conventions.
- No layer-named packages such as `service`, `repository`, or `controller`.
- Services do not depend on controllers.
- Repositories do not depend on controllers or services.
- Listener components do not depend on repositories.
- Events are immutable value-style classes.
- Test method naming and `@DisplayName`.

Before inventing a new class role, check these tests first.

## Adding A Controller Test

Checklist:

- Extend `SpringBootIntegrationTest` unless a narrower slice test is enough.
- Put shared fixture creation in `@BeforeEach` when every test in that scope needs it.
- Use a `@Nested` test class when only a subset of scenarios shares expensive setup.
- Autowire `MockMvc`.
- Autowire the Jackson `ObjectMapper`.
- Add `@WithMockUser` or explicit HTTP Basic auth.
- Stub external issuer behavior if the flow calls issuer authorization.
- Send headers required by the controller, especially `version`.
- Assert status, important fields, and `snake_case` JSON names.
- Keep test data local to the test.

## Failure Triage

| Symptom | First place to check |
| --- | --- |
| Cannot connect to database | `docker compose ps`, PostgreSQL on `localhost:5432` |
| Kafka startup failure | Kafka health in `compose.yaml` or `docker-ci.yml` |
| WireMock issuer mismatch | Correlation id and mappings under `wiremock/mappings` |
| Unexpected 401/403 | `@WithMockUser`, role annotations, `SecurityConfiguration` |
| JSON field missing | `snake_case` configuration and response DTO shape |
| ArchUnit suffix failure | `ClassesNamesConventionTest` |
| Test method naming failure | `TestMethodsConventionTest` |
