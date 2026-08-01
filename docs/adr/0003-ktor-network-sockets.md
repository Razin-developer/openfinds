# ADR 0003: Ktor Network sockets instead of raw java.net sockets

## Status
Accepted.

## Context
OpenFind's P2P protocol needs a length-prefixed, bidirectional TCP channel with full control over framing — not an HTTP client. The original prototype used `java.net.Socket`/`ServerSocket` directly with blocking `InputStream`/`OutputStream` calls wrapped in `withContext(Dispatchers.IO)`.

## Decision
Migrate to `io.ktor.network.sockets` (the `ktor-network` artifact), which provides coroutine-native, suspending TCP sockets (`aSocket(selectorManager).tcp().connect(...)` / `.bind(...)`) with `ByteReadChannel`/`ByteWriteChannel` for I/O. This fits the project's tech stack (Ktor was already specified) and removes the need to manually wrap every blocking call in `withContext(Dispatchers.IO)`.

## Consequences
- All socket I/O (`Framing`, `HandshakeExecutor`, `P2pSession`, `P2pConnectionManager`) is now suspend-native, composing directly with the rest of the coroutine-based codebase.
- One `SelectorManager` is shared per `P2pConnectionManager` instance and lives for the process's lifetime; it's not explicitly closed on `stop()` since the manager is a `@Singleton` that outlives any single listen/connect cycle.
- Framing now uses Ktor's own `readInt`/`writeInt` byte-order convention consistently on both ends, rather than `java.io.DataInputStream`/`DataOutputStream`'s convention — this is an internal wire-format detail with no external compatibility requirement, since both peers always run the same OpenFind build.
