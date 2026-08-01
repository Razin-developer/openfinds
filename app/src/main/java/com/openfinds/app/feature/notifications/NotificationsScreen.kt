package com.openfinds.app.feature.notifications

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.openfinds.app.core.background.NotificationChannels
import com.openfinds.app.core.ui.components.BackIconButton

private data class ChannelInfo(val id: String, val title: String, val description: String)

private val channelInfos =
    listOf(
        ChannelInfo(
            NotificationChannels.MONITORING,
            "Background monitoring",
            "The persistent \"OpenFind is watching\" status notification",
        ),
        ChannelInfo(NotificationChannels.PAIRING, "Pairing requests", "When a nearby device wants to pair"),
        ChannelInfo(NotificationChannels.FIND_ALERTS, "Find device alerts", "When someone rings, vibrates, or flashes this phone"),
        ChannelInfo(NotificationChannels.DEVICE_STATUS, "Device status", "Connection changes and low-battery alerts for trusted devices"),
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Scaffold(topBar = { TopAppBar(title = { Text("Notifications") }, navigationIcon = { BackIconButton(onBack) }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    "Each notification type has its own Android channel, so you can silence or customize it independently.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            items(channelInfos, key = { it.id }) { channel ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(channel.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                channel.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TextButton(
                            onClick = {
                                val intent =
                                    Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                        .putExtra(Settings.EXTRA_CHANNEL_ID, channel.id)
                                context.startActivity(intent)
                            },
                        ) { Text("Manage") }
                    }
                }
            }
        }
    }
}
