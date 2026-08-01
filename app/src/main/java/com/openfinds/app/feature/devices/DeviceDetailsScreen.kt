package com.openfinds.app.feature.devices

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryFull
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openfinds.app.core.ui.components.BackIconButton
import com.openfinds.app.core.ui.format.formatBytes
import com.openfinds.app.core.ui.format.formatDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailsScreen(
    onBack: () -> Unit,
    onFindDevice: () -> Unit,
    onForgotten: () -> Unit,
    onViewHistory: () -> Unit,
    viewModel: DeviceDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showRenameDialog by remember { mutableStateOf(false) }
    var showForgetDialog by remember { mutableStateOf(false) }
    var groupMenuOpen by remember { mutableStateOf(false) }
    val device = uiState.device

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(device?.name ?: "Device") },
                navigationIcon = { BackIconButton(onBack) },
                actions = {
                    IconButton(onClick = { showForgetDialog = true }) {
                        Icon(Icons.Outlined.DeleteOutline, contentDescription = "Forget device")
                    }
                },
            )
        },
    ) { padding ->
        if (device == null) {
            androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                CircularProgressIndicator(modifier = Modifier.padding(32.dp))
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Button(onClick = onFindDevice, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.MyLocation, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Find this device")
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Status", style = MaterialTheme.typography.titleMedium)
                            TextButton(onClick = { viewModel.refreshStatus() }) {
                                Text(if (uiState.isRefreshing) "Refreshing…" else "Refresh")
                            }
                        }

                        val snapshot = uiState.snapshot
                        when {
                            uiState.isRefreshing && snapshot == null -> CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                            uiState.errorMessage != null && snapshot == null ->
                                Text(
                                    uiState.errorMessage ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            snapshot != null -> {
                                Spacer(Modifier.height(12.dp))
                                StatRow(
                                    Icons.Outlined.BatteryFull,
                                    "Battery",
                                    "${snapshot.batteryPercent}%" + if (snapshot.isCharging) " (charging)" else "",
                                )
                                LinearProgressIndicator(
                                    progress = { snapshot.batteryPercent / 100f },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                )

                                StatRow(
                                    Icons.Outlined.Storage,
                                    "Storage",
                                    "${formatBytes(snapshot.storageUsedBytes)} / ${formatBytes(snapshot.storageTotalBytes)}",
                                )
                                LinearProgressIndicator(
                                    progress = {
                                        (snapshot.storageUsedBytes.toFloat() / snapshot.storageTotalBytes.toFloat()).coerceIn(
                                            0f,
                                            1f,
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                )

                                StatRow(
                                    Icons.Outlined.Memory,
                                    "Memory",
                                    "${formatBytes(snapshot.ramUsedBytes)} / ${formatBytes(snapshot.ramTotalBytes)}",
                                )
                                LinearProgressIndicator(
                                    progress = { (snapshot.ramUsedBytes.toFloat() / snapshot.ramTotalBytes.toFloat()).coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                )

                                StatRow(Icons.Outlined.Timer, "Uptime", formatDuration(snapshot.uptimeMillis))
                            }
                        }
                    }
                }
            }

            item {
                val photoPicker =
                    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                        if (uri != null) viewModel.setAvatarImage(uri.toString())
                    }
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        DeviceAvatar(device = device, size = 56.dp)
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Avatar", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Pick a photo, or leave it as your device's colored initial",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TextButton(
                            onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        ) { Text("Choose") }
                    }
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Nickname", style = MaterialTheme.typography.titleMedium)
                            TextButton(onClick = { showRenameDialog = true }) { Text("Edit") }
                        }
                        Text(device.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Group", style = MaterialTheme.typography.titleMedium)
                            Box {
                                TextButton(onClick = { groupMenuOpen = true }) {
                                    Text(uiState.groups.firstOrNull { it.id == device.groupId }?.name ?: "None")
                                }
                                DropdownMenu(expanded = groupMenuOpen, onDismissRequest = { groupMenuOpen = false }) {
                                    DropdownMenuItem(
                                        text = { Text("None") },
                                        onClick = {
                                            viewModel.assignGroup(null)
                                            groupMenuOpen = false
                                        },
                                    )
                                    uiState.groups.forEach { group ->
                                        DropdownMenuItem(
                                            text = { Text(group.name) },
                                            onClick = {
                                                viewModel.assignGroup(group.id)
                                                groupMenuOpen = false
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Activity history", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = onViewHistory) { Text("View") }
                    }
                }
            }
        }
    }

    if (showRenameDialog && device != null) {
        var nickname by remember { mutableStateOf(device.nickname ?: "") }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename device") },
            text = { OutlinedTextField(value = nickname, onValueChange = { nickname = it }, singleLine = true) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.rename(nickname)
                    showRenameDialog = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") } },
        )
    }

    if (showForgetDialog) {
        AlertDialog(
            onDismissRequest = { showForgetDialog = false },
            title = { Text("Forget this device?") },
            text = { Text("You'll need to pair again to reconnect.") },
            confirmButton = {
                TextButton(onClick = {
                    showForgetDialog = false
                    viewModel.forget(onForgotten)
                }) { Text("Forget") }
            },
            dismissButton = { TextButton(onClick = { showForgetDialog = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun StatRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Row {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
