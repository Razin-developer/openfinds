package com.openfinds.app.feature.devices

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.openfinds.app.core.domain.model.ConnectionState
import com.openfinds.app.core.domain.model.TrustedDevice
import com.openfinds.app.core.ui.components.PulsingDot

@Composable
fun DeviceListItem(
    device: TrustedDevice,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DeviceAvatar(device = device)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f, fill = true)) {
                Text(device.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    statusLabel(device),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (device.connectionState == ConnectionState.ONLINE) {
                PulsingDot(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

private fun statusLabel(device: TrustedDevice): String =
    when (device.connectionState) {
        ConnectionState.ONLINE -> "Online now"
        ConnectionState.CONNECTING -> "Connecting…"
        ConnectionState.OFFLINE ->
            device.lastSeenEpochMillis?.let { lastSeen ->
                "Last seen " + com.openfinds.app.core.ui.format.relativeTime(lastSeen)
            } ?: "Never connected"
    }
