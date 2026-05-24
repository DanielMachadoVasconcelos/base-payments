# Vertical Slice Prompt Compatibility Note

This file is kept for tools or agents that still look for the old Junie prompt.

Use the current AI-agent docs instead:

- [Architecture and design](agents/ARCHITECTURE_AND_DESIGN.md)
- [Module interactions](agents/MODULE_INTERACTIONS.md)
- [Skills and rules](agents/SKILLS_AND_RULES.md)

## Copy-Ready Instruction

When generating code for `base-payments`, follow the vertical-slice architecture in `docs/agents/ARCHITECTURE_AND_DESIGN.md` and the naming/package rules in `docs/agents/SKILLS_AND_RULES.md`.

Core reminder:

```text
com.ead.payments.<feature>[.<usecase>]
```

Do not create layer packages such as:

```text
com.ead.payments.service
com.ead.payments.controller
com.ead.payments.repository
com.ead.payments.common.utils
```

Cross-module collaboration should prefer Spring Modulith events. Treat the ArchUnit tests in `src/test/java/com/ead/payments/architecture` as the final rule source.
