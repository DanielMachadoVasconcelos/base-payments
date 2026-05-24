# Code Style And Design Guidelines

This file is kept as a compatibility entrypoint for older references.

The current source of truth has moved to the AI-agent documentation set:

- [Agent entrypoint](../AGENTS.md)
- [Architecture and design](agents/ARCHITECTURE_AND_DESIGN.md)
- [Skills and rules](agents/SKILLS_AND_RULES.md)
- [Testing playbook](agents/TESTING_PLAYBOOK.md)

## Current Build Facts

Do not use older guidance that mentions Java 24 or Spring Boot 3.x for this repository.

Current facts from `build.gradle`:

- Java 25.
- Spring Boot 4.0.6.
- Spring Modulith 2.0.6.

## Short Rule Set

- Package by business feature and use case.
- Keep controllers thin.
- Keep services focused on orchestration.
- Keep aggregates responsible for invariants and domain events.
- Keep external calls behind gateways and clients.
- Keep logging, security, tracing, and observability outside business logic.
- Treat `src/test/java/com/ead/payments/architecture` as executable architecture policy.

For detailed good/bad examples, use [agents/SKILLS_AND_RULES.md](agents/SKILLS_AND_RULES.md).
