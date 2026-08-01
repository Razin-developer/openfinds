package com.openfinds.app.feature.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openfinds.app.core.domain.model.HistoryEvent
import com.openfinds.app.core.domain.model.HistoryEventType
import com.openfinds.app.core.ui.components.BackIconButton
import com.openfinds.app.core.ui.components.EmptyState
import com.openfinds.app.core.ui.format.relativeTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val events by viewModel.events.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = { BackIconButton(onBack) },
                actions = {
                    if (events.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = "Clear history")
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (events.isEmpty()) {
            EmptyState(
                modifier = Modifier.padding(padding),
                icon = Icons.Outlined.History,
                title = "No activity yet",
                message = "Pairing, connection, and find events will show up here.",
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(events, key = { it.id }) { event -> HistoryRow(event) }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear history?") },
            text = { Text("This removes all logged activity. It doesn't forget any devices.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory()
                    showClearDialog = false
                }) { Text("Clear") }
            },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun HistoryRow(event: HistoryEvent) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(36.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(iconFor(event.type), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(labelFor(event), style = MaterialTheme.typography.bodyLarge)
                Text(
                    relativeTime(event.timestampEpochMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun labelFor(event: HistoryEvent): String =
    when (event.type) {
        HistoryEventType.PAIRED -> "Paired with ${event.deviceName}"
        HistoryEventType.FORGOTTEN -> "Forgot ${event.deviceName}"
        HistoryEventType.CONNECTED -> "${event.deviceName} came online"
        HistoryEventType.DISCONNECTED -> "${event.deviceName} went offline"
        HistoryEventType.FIND_TRIGGERED -> "Located ${event.deviceName}" + (event.detail?.let { " ($it)" } ?: "")
        HistoryEventType.RENAMED -> "Renamed ${event.deviceName}"
        HistoryEventType.OTHER -> event.deviceName
    }

private fun iconFor(type: HistoryEventType): ImageVector =
    when (type) {
        HistoryEventType.PAIRED -> Icons.Outlined.PersonAdd
        HistoryEventType.FORGOTTEN -> Icons.Outlined.LinkOff
        HistoryEventType.CONNECTED -> Icons.Outlined.Wifi
        HistoryEventType.DISCONNECTED -> Icons.Outlined.CloudOff
        HistoryEventType.FIND_TRIGGERED -> Icons.Outlined.Bolt
        HistoryEventType.RENAMED -> Icons.Outlined.DriveFileRenameOutline
        HistoryEventType.OTHER -> Icons.Outlined.History
    }
