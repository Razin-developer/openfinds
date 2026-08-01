package com.openfinds.app.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.openfinds.app.core.ui.components.BackIconButton

private data class SecurityFact(val title: String, val body: String)

private val securityFacts =
    listOf(
        SecurityFact(
            "Identity keys",
            "Each device generates its own X25519 keypair. The private key is encrypted at rest with an AES-256-GCM key generated inside, and never extractable from, the Android Keystore.",
        ),
        SecurityFact(
            "Pairing handshake",
            "Pairing runs an X25519 Diffie-Hellman key exchange, then derives a session key with HKDF-HMAC-SHA256, binding it to that specific handshake so it can't be replayed.",
        ),
        SecurityFact(
            "Session encryption",
            "Every message after pairing — ring/vibrate/flash commands, status replies — is encrypted with AES-256-GCM under the derived session key.",
        ),
        SecurityFact(
            "QR vs. PIN pairing",
            "QR pairing trusts the public key embedded in the scanned code; PIN pairing mixes the PIN into key derivation so a completed handshake proves both sides knew it. Full details and known trade-offs are in SECURITY.md.",
        ),
        SecurityFact(
            "No servers, ever",
            "There is no backend to breach. All traffic stays on your local Wi-Fi network, directly between your devices.",
        ),
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Security") }, navigationIcon = { BackIconButton(onBack) }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(securityFacts) { fact ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(fact.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            fact.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
