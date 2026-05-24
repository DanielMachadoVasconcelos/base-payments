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

- No feature entries yet.

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
