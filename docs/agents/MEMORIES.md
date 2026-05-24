# MEMORIES

This file stores durable context for future AI agents. Update it when you learn something stable about the user's preferences, the project, or handoff state.

Do not use this as a substitute for reading the repo. Use it as a compass.

## User Preferences

- The user cares deeply about this project as a case study.
- The user explicitly wants future AI/IA agents to understand the project, not only patch it.
- The user values planning before creating major structure.
- The user wants candid documentation that names inconsistencies and gotchas.
- The user prefers AI-optimized Markdown with clear rules, examples, and good/bad code snippets.
- The user wants architecture, design, tests, module interactions, skills/rules, and memories documented for future agents.
- The user expects agents to preserve their work and not revert unrelated changes.
- The user wants a root changelog where shipped work is recorded with date, application version, changes, and verification.
- The user strongly prefers meaningful, specific domain exceptions over generic reusable exceptions for business-rule failures.
- The user prefers common test fixtures to live in JUnit/Spring setup methods instead of being repeated in every test method.

## Project Intent

- `base-payments` is a modular Spring Boot payments/order case study.
- The architecture should teach vertical slices, Spring Modulith, domain events, local observability, and architecture tests.
- Documentation should help future agents act with context and restraint.
- The codebase should remain approachable to developers encountering it for the first time.

## Stable Technical Memories

- Java version is 25, from `build.gradle`.
- Spring Boot version is 4.0.6, from `build.gradle`.
- Spring Modulith version is 2.0.6, from `build.gradle`.
- Local infrastructure lives in `compose.yaml`.
- CI infrastructure lives in `docker-ci.yml`.
- CI workflow lives in `.github/workflows/workflow.yml`.
- Architecture rules live in `src/test/java/com/ead/payments/architecture`.
- Integration tests usually extend `SpringBootIntegrationTest`.
- JSON should be `snake_case`.
- Test methods must match `should.*When.*` and include `@DisplayName`.
- Feature work that will ship to `master` should update `CHANGELOG.md`; use `build.gradle` for the application version.

## Current Gotchas

- Older documentation used to conflict with Gradle by mentioning Java 24 and Spring Boot 3.x. The new agent docs should now point back to Java 25 and Spring Boot 4.0.6.
- The worktree may include a user-driven Jackson package move from `json` to `configurations`. Do not revert it.
- `EventsController` currently has hardcoded fallback event responses when stored events are missing. Treat this as a known rough edge.
- The worktree may contain generated/local artifacts such as `.gradle-user-home/`, `BOOT-INF/`, `META-INF/`, `.DS_Store`, and `terraform/`.

## Handoff Notes Template

When finishing meaningful work, add a short note here only if it will remain useful:

```text
YYYY-MM-DD - Agent note:
- What changed:
- Why it matters:
- Verification:
- Follow-up:
```

## Agent Notes

2026-05-24 - Agent documentation system created:
- What changed: Added root `AGENTS.md`, refreshed `README.md`, and created `docs/agents/` guides.
- Why it matters: Future agents now have a documented entrypoint, project atlas, architecture guide, interaction map, testing playbook, rules, and memories.
- Verification: Documentation consistency and link review should be run after edits.
- Follow-up: Keep this memory file current when durable preferences or repo gotchas are discovered.

2026-05-24 - Complete-order endpoint behavior chosen:
- What changed: Planned and implemented `PUT /orders/{order_id}/complete`.
- Why it matters: Completion is idempotent for already completed orders, cancelled orders cannot be completed, and first completion publishes `OrderCompletedEvent`.
- Verification: See `CHANGELOG.md` for the current verification status.
- Follow-up: Move the changelog entry from `Unreleased` into a dated release entry when this ships to `master`.

2026-05-24 - Specific terminal-state exceptions:
- What changed: Replaced generic order state transition handling with `CannotCompleteCancelledOrderException` and `CannotCancelCompletedOrderException`.
- Why it matters: The code now speaks the business rule directly in DDD language.
- Verification: See `CHANGELOG.md` for the current verification status.
- Follow-up: Use specific exceptions for future business-rule failures.

2026-05-24 - Test fixture setup preference:
- What changed: Moved repeated order placement into JUnit `@BeforeEach` setup where each scenario needs a placed order.
- Why it matters: Tests read as behavior checks instead of setup scripts.
- Verification: See `CHANGELOG.md` for the current verification status.
- Follow-up: Use nested test scopes when only part of a class needs the fixture.
