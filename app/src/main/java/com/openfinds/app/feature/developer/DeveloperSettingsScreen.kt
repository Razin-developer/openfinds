package com.openfinds.app.feature.developer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openfinds.app.core.ui.components.BackIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperSettingsScreen(
    onBack: () -> Unit,
    viewModel: DeveloperSettingsViewModel = hiltViewModel(),
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val rawDevices by viewModel.rawDiscoveredDevices.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }
    var showResetOnboardingDialog by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Developer options") }, navigationIcon = { BackIconButton(onBack) }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SwitchRow(
                            "Verbose logging",
                            "Keep DEBUG/VERBOSE log lines in Diagnostics, not just warnings and errors",
                            prefs.verboseLoggingEnabled,
                            viewModel::setVerboseLoggingEnabled,
                        )
                    }
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SwitchRow(
                            "Show raw discovered devices",
                            "List every NSD/UDP beacon seen below, including devices you haven't paired with",
                            prefs.showRawDiscoveredDevices,
                            viewModel::setShowRawDiscoveredDevices,
                        )
                    }
                }
            }

            if (prefs.showRawDiscoveredDevices) {
                item { Text("Raw discovery feed", style = MaterialTheme.typography.titleMedium) }
                if (rawDevices.isEmpty()) {
                    item {
                        Text(
                            "No beacons seen yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(rawDevices) { device ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(device.serviceName, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "${device.host}:${device.port}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            item {
                OutlinedButton(onClick = { showResetOnboardingDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Reset onboarding")
                }
            }
            item {
                OutlinedButton(onClick = { showClearDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Clear local database", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear local database?") },
            text = { Text("Deletes all trusted devices, groups, and history. This can't be undone — you'll need to pair devices again.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearLocalDatabase()
                    showClearDialog = false
                }) { Text("Clear everything") }
            },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("Cancel") } },
        )
    }

    if (showResetOnboardingDialog) {
        AlertDialog(
            onDismissRequest = { showResetOnboardingDialog = false },
            title = { Text("Reset onboarding?") },
            text = { Text("Shows the Welcome and Permissions screens again the next time the app opens.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetOnboarding()
                    showResetOnboardingDialog = false
                }) { Text("Reset") }
            },
            dismissButton = { TextButton(onClick = { showResetOnboardingDialog = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
