package com.openfinds.app.feature.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openfinds.app.core.data.datastore.AppThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenGroups: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSecurity: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onOpenDeveloperSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenOpenSource: () -> Unit,
    onOpenChangelog: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    var nameDraft by remember(prefs.deviceDisplayName) { mutableStateOf(prefs.deviceDisplayName) }
    val context = LocalContext.current

    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SettingsSection(title = "This device") {
                    OutlinedTextField(
                        value = nameDraft,
                        onValueChange = { nameDraft = it },
                        label = { Text("Display name shown to other devices") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    TextButton(
                        onClick = { viewModel.setDeviceDisplayName(nameDraft) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Save name") }
                }
            }

            item {
                SettingsSection(title = "Appearance") {
                    AppThemeMode.entries.forEach { mode ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            RadioButton(selected = prefs.themeMode == mode, onClick = { viewModel.setThemeMode(mode) })
                            Text(mode.name.lowercase().replaceFirstChar(Char::uppercase))
                        }
                    }
                }
            }

            item {
                SettingsSection(title = "Background & connectivity") {
                    SettingsSwitchRow(
                        title = "Background monitoring",
                        subtitle = "Stay discoverable and reachable while the app is closed",
                        checked = prefs.backgroundMonitoringEnabled,
                        onCheckedChange = viewModel::setBackgroundMonitoringEnabled,
                    )
                    HorizontalDivider()
                    SettingsSwitchRow(
                        title = "Auto reconnect",
                        subtitle = "Periodically check on trusted devices to keep last-seen accurate",
                        checked = prefs.autoReconnectEnabled,
                        onCheckedChange = viewModel::setAutoReconnectEnabled,
                    )
                    if (isIgnoringBatteryOptimizationsSupported()) {
                        HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Battery optimization", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "Exempt OpenFind so background monitoring isn't killed to save battery",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            TextButton(onClick = { requestIgnoreBatteryOptimizations(context) }) { Text("Fix") }
                        }
                    }
                    HorizontalDivider()
                    SettingsLinkRow("Notifications", onOpenNotifications)
                }
            }

            item {
                SettingsSection(title = "Organize") {
                    SettingsLinkRow("Device groups", onOpenGroups)
                    HorizontalDivider()
                    SettingsLinkRow("Activity history", onOpenHistory)
                }
            }

            item {
                SettingsSection(title = "About") {
                    SettingsLinkRow("Security", onOpenSecurity)
                    HorizontalDivider()
                    SettingsLinkRow("Diagnostics", onOpenDiagnostics)
                    HorizontalDivider()
                    SettingsLinkRow("Developer options", onOpenDeveloperSettings)
                    HorizontalDivider()
                    SettingsLinkRow("About OpenFind", onOpenAbout)
                    HorizontalDivider()
                    SettingsLinkRow("Privacy policy", onOpenPrivacy)
                    HorizontalDivider()
                    SettingsLinkRow("Open source & licenses", onOpenOpenSource)
                    HorizontalDivider()
                    SettingsLinkRow("Changelog", onOpenChangelog)
                }
            }
        }
    }
}

private fun isIgnoringBatteryOptimizationsSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

private fun requestIgnoreBatteryOptimizations(context: Context) {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    if (powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true) {
        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)))
        return
    }
    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${context.packageName}"))
    context.startActivity(intent)
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
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
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsLinkRow(
    title: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TextButton(onClick = onClick) { Text(title) }
    }
}
