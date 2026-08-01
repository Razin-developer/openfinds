package com.openfinds.app.feature.onboarding

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

private data class PermissionGroup(val icon: ImageVector, val title: String, val rationale: String, val permissions: List<String>)

private fun permissionGroups(): List<PermissionGroup> =
    buildList {
        add(
            PermissionGroup(
                icon = Icons.Outlined.Wifi,
                title = "Find devices on your Wi-Fi",
                rationale = "Lets OpenFind discover other OpenFind phones on the same network. Nothing is sent outside your Wi-Fi.",
                permissions =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        listOf(Manifest.permission.NEARBY_WIFI_DEVICES)
                    } else {
                        listOf(Manifest.permission.ACCESS_FINE_LOCATION)
                    },
            ),
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(
                PermissionGroup(
                    icon = Icons.Outlined.Bluetooth,
                    title = "Bluetooth-assisted discovery",
                    rationale = "Speeds up finding nearby trusted devices even before they join the same Wi-Fi network.",
                    permissions =
                        listOf(
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_ADVERTISE,
                        ),
                ),
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(
                PermissionGroup(
                    icon = Icons.Outlined.Notifications,
                    title = "Pairing & find alerts",
                    rationale = "So you're notified when a device wants to pair, or when someone is trying to find this phone.",
                    permissions = listOf(Manifest.permission.POST_NOTIFICATIONS),
                ),
            )
        }
        add(
            PermissionGroup(
                icon = Icons.Outlined.CameraAlt,
                title = "Scan pairing QR codes",
                rationale = "Only used while you're actively scanning a QR code to pair a new device.",
                permissions = listOf(Manifest.permission.CAMERA),
            ),
        )
    }

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsScreen(
    onContinue: () -> Unit,
    viewModel: PermissionsViewModel = hiltViewModel(),
) {
    val groups = permissionGroups()
    val allPermissions = groups.flatMap { it.permissions }
    val permissionsState = rememberMultiplePermissionsState(allPermissions)

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Text("Before we start", style = MaterialTheme.typography.headlineSmall)
            Text(
                "OpenFind only asks for what each feature needs, and explains why up front.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
            )

            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(groups) { group -> PermissionCard(group) }
            }

            Button(
                onClick = {
                    if (permissionsState.allPermissionsGranted) {
                        viewModel.onPermissionsAcknowledged(onContinue)
                    } else {
                        permissionsState.launchMultiplePermissionRequest()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp).padding(top = 16.dp),
            ) {
                Text(if (permissionsState.allPermissionsGranted) "Continue" else "Grant permissions")
            }

            if (!permissionsState.allPermissionsGranted) {
                TextButton(
                    onClick = { viewModel.onPermissionsAcknowledged(onContinue) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Skip for now — you can grant these later in Settings")
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(group: PermissionGroup) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(group.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text(group.title, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(6.dp))
            Text(group.rationale, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
