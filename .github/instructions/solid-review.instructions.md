---
applyTo: "**/*.java"
---

# SOLID-focused Java review

When reviewing Java changes, use the repository's `solid` skill as a reasoning framework. Review for correctness first, then assess whether the change is understandable, cohesive, and consistent with the existing architecture. SOLID principles are prompts for finding concrete risks, not a checklist that every class must satisfy mechanically.

## Review workflow

1. Read the full diff and enough neighboring code to understand callers, lifecycle, invariants, and existing conventions. Check relevant tests and, for shared contracts, the related documentation or fixtures.
2. Trace the changed behavior through its call sites. Look for regressions, invalid state transitions, lost errors, thread/lifecycle issues, and mismatches between implementation and requirements.
3. Use the principles below to identify maintainability problems only when they have a specific, plausible consequence for this change.
4. Report only actionable findings. For each, cite the exact file and line, explain the scenario and impact, and suggest a focused correction. Order findings by severity; omit praise, speculative concerns, and stylistic preferences.
5. Do not edit code or broaden the review into a refactor unless asked. If validation is appropriate, run the smallest relevant test first; use JDK 25 and the Maven Wrapper for Java checks. State clearly when a check could not run or failed for an unrelated/environmental reason.

## SOLID review prompts

- **Single Responsibility:** Does this change give a class or method a second independent reason to change, or mix UI, domain rules, persistence, and transport? Distinguish genuine responsibility conflicts from normal orchestration and small cohesive helpers.
- **Open/Closed:** Does a new case require fragile edits across unrelated conditionals or duplicate behavior? Prefer extension points only when there is a real anticipated variation; do not request abstractions for hypothetical future cases.
- **Liskov Substitution:** If inheritance or an interface implementation changes, can every implementation still honor the established contract, including nullability, exceptions, side effects, and lifecycle?
- **Interface Segregation:** Does a changed interface force consumers to depend on unrelated operations, or make implementations provide meaningless methods? Consider the actual consumers before recommending a split.
- **Dependency Inversion:** Does high-level policy become coupled to a low-level detail in a way that makes behavior difficult to test or change? Follow existing module boundaries; do not introduce interfaces solely to satisfy the principle.

Also look for excessive nesting, unclear names, hidden side effects, duplicated business rules, and error handling that masks failures. Prefer the smallest design that fits this codebase. Do not enforce arbitrary class/method line limits, a fixed number of fields, or value-object wrappers for every primitive: Java records and DTOs are appropriate data carriers, and domain types should follow demonstrated project conventions.

## Repository-specific checks

- `src/common` must remain independent of JavaFX and server frameworks. DTO, enum, REST, or WebSocket changes may also require updates to `docs/architecture/contracts/`, schemas, fixtures, and tests.
- JavaFX navigation and theme operations belong on the FX Application Thread. Check controller lifecycle and cleanup when listeners, timers, overlays, or cached views change.
- User/admin UI changes should stay with their feature package and matching FXML/CSS/resources; new routes belong in `UserViews` or `AdminViews`.
- The server is currently a scaffold and desktop clients use deterministic mock data. Do not treat roadmap or design-document behavior as implemented unless the code confirms it.
- Use `./mvnw -pl <module> -am test` or `verify` for a focused Java check; use `./mvnw clean verify` for the required full Java validation. The optional JavaFX UI suite requires a display or Xvfb; see `.github/copilot-instructions.md` for environment caveats.
