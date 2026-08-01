# ADR 0005: One buffering Timber tree feeds Diagnostics in both debug and release

## Status
Accepted.

## Context
The spec requires a Diagnostics screen with log export, and separately says release builds must not leak data anywhere. Early on, release builds planted a Timber tree that only forwarded WARN+ to `android.util.Log` and had no connection to an in-app diagnostics view.

## Decision
Plant a single `BufferingTimberTree` in both debug and release builds, backed by an in-memory ring buffer (`LogBuffer`, capped at 2000 entries) that never writes anywhere except device-local memory (and, on export, a device-local cache file shared only via an explicit user-initiated share sheet). A `minPriority` field on `LogBuffer`, toggled by the "Verbose logging" switch in Developer options, controls whether DEBUG/VERBOSE lines are kept (default: WARN+ only, matching the old release behavior).

## Consequences
- The Diagnostics screen has real data in every build variant, not just debug.
- No log content ever leaves the device automatically — export is always an explicit user action (`Intent.ACTION_SEND` via `FileProvider`), consistent with SECURITY.md/PRIVACY.md's "nothing leaves this device automatically" guarantee.
- Verbose logging is off by default in both debug and release, to avoid unnecessarily retaining chatty DEBUG-level logs in memory; users/developers who need it can flip it on in Developer options.
