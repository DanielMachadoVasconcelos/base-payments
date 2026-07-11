# Agent Documentation Map

This folder is the AI-agent operating manual for `base-payments`.

Start at the root [AGENTS.md](../../AGENTS.md), then use this map to choose the right file for the task in front of you.

## Navigation

| File | Use it when you need to know |
| --- | --- |
| [PROJECT_ATLAS.md](PROJECT_ATLAS.md) | What this project is, where things live, and which files are source of truth |
| [ARCHITECTURE_AND_DESIGN.md](ARCHITECTURE_AND_DESIGN.md) | How to design features that fit the Spring Modulith and vertical-slice style |
| [MODULE_INTERACTIONS.md](MODULE_INTERACTIONS.md) | How orders, products, inventory, purchases, issuer, database, Kafka, and events interact |
| [TESTING_PLAYBOOK.md](TESTING_PLAYBOOK.md) | How to run, write, and debug tests in this repository |
| [SKILLS_AND_RULES.md](SKILLS_AND_RULES.md) | Agent rules, naming constraints, good/bad code snippets, and generation prompts |
| [MEMORIES.md](MEMORIES.md) | Durable user preferences, project goals, gotchas, and handoff notes |
| [CHANGELOG.md](../../CHANGELOG.md) | Feature and release history for work shipping to `master` |

## Agent Operating Loop

1. Ground yourself in the repo before editing.
2. Check `git status --short` and protect user changes.
3. Read the relevant guide in this folder.
4. Make the smallest coherent change.
5. Run the narrowest useful verification.
6. Update docs or memories if you discovered durable project truth.

## Fast Facts

- Java: 25 with preview features.
- Spring Boot: 4.1.0.
- Main package: `com.ead.payments`.
- Local infrastructure: `compose.yaml`.
- CI infrastructure: `docker-ci.yml` plus `.github/workflows/workflow.yml`.
- Architecture rules: `src/test/java/com/ead/payments/architecture`.
- Integration test base: `src/test/java/com/ead/payments/SpringBootIntegrationTest.java`.
- Feature log: `CHANGELOG.md`.

## Default Commands

```bash
docker compose up -d
./gradlew test
```

For documentation-only changes, a link and consistency review is usually enough. Do not imply application tests passed unless you actually ran them.
