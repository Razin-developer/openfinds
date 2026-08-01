package com.openfinds.app.feature.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private data class WelcomeHighlight(val icon: ImageVector, val title: String, val body: String)

private val highlights =
    listOf(
        WelcomeHighlight(
            Icons.Outlined.WifiTethering,
            "No cloud, ever",
            "Devices talk directly over your Wi-Fi — nothing leaves your network.",
        ),
        WelcomeHighlight(
            Icons.Outlined.Lock,
            "Encrypted pairing",
            "Every device you trust is verified with a QR code or PIN, then end-to-end encrypted.",
        ),
        WelcomeHighlight(Icons.Outlined.Bolt, "Instant find", "Ring, vibrate, or flash a trusted phone the moment you need it."),
    )

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(PaddingValues(horizontal = 28.dp, vertical = 40.dp)),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "OpenFind",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Find your trusted devices — privately, locally, instantly.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(40.dp))
                highlights.forEach { highlight ->
                    HighlightRow(highlight)
                    Spacer(Modifier.height(24.dp))
                }
            }

            Button(
                onClick = onGetStarted,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("Get started", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun HighlightRow(highlight: WelcomeHighlight) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier =
                Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(highlight.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(highlight.title, style = MaterialTheme.typography.titleMedium)
            Text(
                highlight.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
