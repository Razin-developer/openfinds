package com.openfinds.app.core.diagnostics

import timber.log.Timber

/** Mirrors every log line into [LogBuffer] so the in-app Diagnostics screen can show and export it. */
class BufferingTimberTree(private val logBuffer: LogBuffer) : Timber.Tree() {
    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?,
    ) {
        val fullMessage = if (t != null) "$message\n${t.stackTraceToString()}" else message
        logBuffer.add(LogEntry(System.currentTimeMillis(), priority, tag, fullMessage))
    }
}
