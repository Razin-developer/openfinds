# ADR 0001: Single Gradle module, organized by package

## Status
Superseded by [ADR 0006](0006-split-into-gradle-feature-modules.md) — the project has since been split into `:core:*` and `:feature:*` Gradle modules. This record is kept for historical context.

## Context
OpenFind's spec calls for "feature modules" and a clean, layered architecture. True Gradle multi-module builds (`:core:data`, `:core:network`, `:feature:pairing`, etc.) give real benefits at team scale — parallel builds, enforced dependency direction, independent releasability — but also add real build-graph complexity: more `build.gradle.kts` files, module-boundary API surfaces, and slower initial setup.

## Decision
Ship the first version (v0.1.0) as a single `:app` Gradle module, but organize source strictly by package as if it were already modularized: `core/data`, `core/network`, `core/domain`, `core/crypto`, `core/background`, `core/di`, `core/ui`, `core/navigation`, and one `feature/<name>` package per screen group. Dependency direction between packages mirrors what module boundaries would enforce (UI → ViewModel → domain repository interface → data/network implementation), even though the compiler doesn't yet enforce it.

## Consequences
- Faster to stand up a correct, working build for v0.1.0.
- Splitting into real Gradle modules later (ADR 0006) is a mechanical refactor — move packages into modules, add module `build.gradle.kts` files, wire dependencies — rather than a redesign, because the package boundaries already match the intended module boundaries.
- Trade-off accepted: until the split happens, nothing prevents a package from reaching across a boundary it shouldn't (e.g., a `feature/*` screen importing `core/data` directly instead of going through a repository interface). Code review is the only enforcement mechanism in the meantime.
