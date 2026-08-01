package com.openfinds.app.core.diagnostics

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.ArrayDeque
import javax.inject.Inject
import javax.inject.Singleton

data class LogEntry(
    val timestampEpochMillis: Long,
    val priority: Int,
    val tag: String?,
    val message: String,
)

/**
 * A bounded, in-memory ring buffer of recent log lines, feeding the
 * Diagnostics screen and log export. Nothing here ever leaves the device
 * except when the user explicitly taps "Export".
 */
@Singleton
class LogBuffer
    @Inject
    constructor() {
        private val maxEntries = 2000
        private val buffer = ArrayDeque<LogEntry>(maxEntries)

        private val _entries = MutableStateFlow<List<LogEntry>>(emptyList())
        val entries: StateFlow<List<LogEntry>> = _entries.asStateFlow()

        /** When verbose logging is off, only WARN and above are retained. Toggled from Developer options. */
        @Volatile var minPriority: Int = Log.DEBUG

        @Synchronized
        fun add(entry: LogEntry) {
            if (entry.priority < minPriority) return
            if (buffer.size >= maxEntries) buffer.removeFirst()
            buffer.addLast(entry)
            _entries.value = buffer.toList()
        }

        @Synchronized
        fun clear() {
            buffer.clear()
            _entries.value = emptyList()
        }
    }
