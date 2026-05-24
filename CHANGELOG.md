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

### Changed
- Documented the complete-order endpoint in README and agent guides.
- Documented the project preference for specific business-rule exceptions.
- Returned order `version` and lifecycle `status` from both V1 and V2 place-order responses.
- Exposed order lifecycle status in search-order responses.
- Moved terminal-state exceptions into their vertical-slice packages and renamed them with noun-phrase class names.
- Moved repeated order-placement setup into JUnit `@BeforeEach` fixtures where appropriate.
- Added BDD story comments to the new cancel and complete order tests.
- Replaced inherited order setup helper with injected test operation providers.

### Verification
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
