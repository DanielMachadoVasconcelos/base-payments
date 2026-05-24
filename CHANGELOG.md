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

### Verification
- Passed: `./gradlew testClasses`
- Passed: `./gradlew test --tests 'com.ead.payments.architecture.*'`
- Blocked: `./gradlew test --tests '*CompleteOrderControllerTest'` compiled, then failed during Spring context startup because PostgreSQL on `localhost:5432` was unavailable and the local Docker daemon could not be reached.
- Not run: full `./gradlew test` due to the same infrastructure blocker.

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
