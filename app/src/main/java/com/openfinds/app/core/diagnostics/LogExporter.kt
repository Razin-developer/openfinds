package com.openfinds.app.core.diagnostics

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Writes the current log buffer to a cache file and returns a shareable `content://` URI for it. */
@Singleton
class LogExporter
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val logBuffer: LogBuffer,
    ) {
        fun export(): Uri {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val dir = File(context.cacheDir, "logs").apply { mkdirs() }
            val file = File(dir, "openfind-logs-${System.currentTimeMillis()}.txt")

            file.bufferedWriter().use { writer ->
                logBuffer.entries.value.forEach { entry ->
                    val timestamp = dateFormat.format(Date(entry.timestampEpochMillis))
                    val level = priorityLabel(entry.priority)
                    writer.appendLine("$timestamp $level/${entry.tag ?: "OpenFind"}: ${entry.message}")
                }
            }

            return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }

        private fun priorityLabel(priority: Int): String =
            when (priority) {
                android.util.Log.VERBOSE -> "V"
                android.util.Log.DEBUG -> "D"
                android.util.Log.INFO -> "I"
                android.util.Log.WARN -> "W"
                android.util.Log.ERROR -> "E"
                else -> "?"
            }
    }
