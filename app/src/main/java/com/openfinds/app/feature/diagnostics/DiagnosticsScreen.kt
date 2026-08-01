package com.openfinds.app.feature.diagnostics

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openfinds.app.core.diagnostics.LogEntry
import com.openfinds.app.core.ui.components.BackIconButton
import com.openfinds.app.core.ui.components.EmptyState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    onBack: () -> Unit,
    viewModel: DiagnosticsViewModel = hiltViewModel(),
) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagnostics") },
                navigationIcon = { BackIconButton(onBack) },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.exportLogs { uri ->
                                val shareIntent =
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                context.startActivity(Intent.createChooser(shareIntent, "Export OpenFind logs"))
                            }
                        },
                    ) { Icon(Icons.Outlined.Share, contentDescription = "Export logs") }
                    IconButton(onClick = { viewModel.clearLogs() }) {
                        Icon(Icons.Outlined.DeleteOutline, contentDescription = "Clear logs")
                    }
                },
            )
        },
    ) { padding ->
        if (entries.isEmpty()) {
            EmptyState(
                modifier = Modifier.padding(padding),
                icon = Icons.Outlined.Description,
                title = "No logs yet",
                message = "Diagnostic output will appear here as you use the app.",
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                reverseLayout = true,
            ) {
                items(entries, key = { it.timestampEpochMillis.toString() + it.message.hashCode() }) { entry ->
                    LogLine(entry)
                }
            }
        }
    }
}

private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

@Composable
private fun LogLine(entry: LogEntry) {
    val color =
        when (entry.priority) {
            Log.ERROR -> MaterialTheme.colorScheme.error
            Log.WARN -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
    Text(
        text = "${timeFormat.format(
            Date(entry.timestampEpochMillis),
        )} ${priorityLabel(entry.priority)}/${entry.tag ?: "OpenFind"}: ${entry.message}",
        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
        color = color,
    )
}

private fun priorityLabel(priority: Int): String =
    when (priority) {
        Log.VERBOSE -> "V"
        Log.DEBUG -> "D"
        Log.INFO -> "I"
        Log.WARN -> "W"
        Log.ERROR -> "E"
        else -> "?"
    }
