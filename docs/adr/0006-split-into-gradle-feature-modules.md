# ADR 0006: Split into Gradle feature modules

## Status
Proposed. Deliberately **not yet executed** — see the note at the end.

## Context
ADR 0001 shipped v0.1.0 as a single `:app` module organized by package, with an explicit plan to split into real Gradle modules once the package boundaries had proven stable. The package structure has since grown (groups, history, diagnostics, developer options, notifications) but still cleanly maps to the module boundaries proposed below.

## Decision
When executed, the codebase should split along the boundaries the package structure already uses:

- `:core:model` — pure Kotlin domain models, no Android dependency
- `:core:crypto` — Keystore + Tink pairing/session crypto
- `:core:data` — Room database, DataStore preferences
- `:core:network` — NSD/UDP/BLE discovery, Ktor P2P protocol
- `:core:background` — foreground service, WorkManager, notifications
- `:core:ui` — shared theme, components, formatting
- `:core:navigation` — the type-safe nav graph
- `:feature:onboarding`, `:feature:pairing`, `:feature:home`, `:feature:devices`, `:feature:find`, `:feature:groups`, `:feature:history`, `:feature:notifications`, `:feature:diagnostics`, `:feature:developer`, `:feature:settings` — one module per screen group
- `:app` — application shell (`Application`, `MainActivity`, DI wiring, manifest, and the concrete nav graph wiring — since the nav graph needs every feature module, it belongs in `:app` rather than `:core:navigation` to avoid a core module depending on features)

Module dependencies should point the same direction Clean Architecture already requires: `feature:*` depends on `core:data`/`core:network`'s repository interfaces and `core:ui`, never the reverse; `core:*` modules never depend on `feature:*`.

## Consequences (if executed)
- Gradle can build/test feature modules in parallel and cache them independently.
- Module `build.gradle.kts` files enforce the dependency direction at the build-graph level, not just by convention/code review.
- Real migration risk: per-module Hilt component wiring, KSP configuration duplication, and moving the nav graph out of a single file all touch a large fraction of the codebase at once.

## Why this hasn't been executed yet
This environment has no Android emulator/device available, so a refactor of this size and blast radius — touching nearly every file's package/module membership and the DI graph — cannot be verified beyond "it compiles." Given a fully working, tested, linted, single-module build already exists, the judgment call was to document the target module layout here rather than risk destabilizing a verified build with changes that can't be exercised on a real device in this session. Tracked in [ROADMAP.md](../../ROADMAP.md) for a follow-up session with device access for proper verification.
