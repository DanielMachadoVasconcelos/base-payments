# Changelog

This file records features and notable system changes for `base-payments`.

Use this log when feature work is ready to ship to `master`. Feature branches may add entries under `Unreleased`; when the work ships, move those entries into a dated release section.

## Entry Format

Each shipped entry should include:

- Date: `YYYY-MM-DD`.
- Application version: read from `build.gradle`.
- Changes: concise user/system-visible changes.
- Verification: tests or checks run before shipping.

## Unreleased

Application version: `0.0.1-SNAPSHOT`

### Added
- Added idempotent order completion with `PUT /orders/{order_id}/complete`.
- Added `OrderCompletedEvent` for first-time `PLACED -> COMPLETED` transitions.
- Added specific terminal-state exceptions for completing cancelled orders and cancelling completed orders.
- Added semantic API-version validation for the existing `version` header, with V2 as the default and unsupported versions rejected early.

### Changed
- Upgraded Spring Boot from 4.0.6 to 4.1.0, Spring Modulith from 2.0.6 to 2.1.0, and Spring Cloud from 2025.1.1 to 2025.1.2.
- Replaced the manually assembled issuer HTTP proxy with Spring Boot's named service-client group, including managed timeouts, redirects, JSON conversion, and correlation-header forwarding.
- Made unchanged V1 HTTP contracts compatible with supported V2/default requests through baseline version mappings.
- Replaced the custom async tracing decorator configuration with Spring Boot 4.1 context propagation.
- Updated collection-element validation and HTTP 422 status APIs for the dependency versions managed by Boot 4.1.
- Removed ignored legacy configuration keys and kept the existing Flyway 12.6.2 override to avoid a downgrade.
- Documented the complete-order endpoint in README and agent guides.
- Documented the project preference for specific business-rule exceptions.
- Returned order `version` and lifecycle `status` from both V1 and V2 place-order responses.
- Exposed order lifecycle status in search-order responses.
- Moved terminal-state exceptions into their vertical-slice packages and renamed them with noun-phrase class names.
- Moved repeated order-placement setup into JUnit `@BeforeEach` fixtures where appropriate.
- Added BDD story comments to the new cancel and complete order tests.
- Replaced inherited order setup helper with injected test operation providers.

### Verification

#### Spring Boot 4.1 upgrade (2026-07-11)

- Passed: `./gradlew --no-daemon clean bootJar --warning-mode all`
- Passed for the Spring Boot 4.1 upgrade: `./gradlew --no-daemon compileJava --rerun-tasks --warning-mode all`
- Passed: `./gradlew --no-daemon testClasses --warning-mode all` using the project's existing test structure; no test cases were added or modified for this upgrade.
- Passed: the existing architecture and logging tests on Spring Boot 4.1.
- The full database and Kafka integration suite was not completed because Docker was unavailable.

#### Earlier feature verification (2026-05-24)

- Passed: `git diff --check -- CHANGELOG.md README.md docs/agents/PROJECT_ATLAS.md src/main/java/com/ead/payments/orders/place src/test/java/com/ead/payments/orders/place`
- Passed: `./gradlew testClasses`
- Passed: `./gradlew test --tests 'com.ead.payments.architecture.*'`
- Passed: `./gradlew test --tests '*CompleteOrderControllerTest' --tests '*CancelOrderControllerTest' --tests '*SearchOrderControllerTest'`
- Passed: `./gradlew --no-daemon cleanTest test --tests '*PlaceOrdersControllerTest'`
- Passed: `./gradlew --no-daemon cleanTest test --tests '*PlaceOrdersControllerTest' --tests '*CompleteOrderControllerTest' --tests '*CancelOrderControllerTest'`
- Passed: `./gradlew --no-daemon test`

## Release Template

```markdown
## YYYY-MM-DD - 0.0.1-SNAPSHOT

### Added
- Feature or capability added.

### Changed
- Existing behavior changed.

### Fixed
- Bug fixed.

### Verification
- `./gradlew test`
```
