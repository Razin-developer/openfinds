package com.openfinds.app.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import com.openfinds.app.BuildConfig
import com.openfinds.app.core.ui.components.BackIconButton

private data class OssLibrary(val name: String, val license: String, val url: String)

private val ossLibraries =
    listOf(
        OssLibrary("Jetpack Compose", "Apache License 2.0", "https://developer.android.com/jetpack/compose"),
        OssLibrary("Kotlin Coroutines", "Apache License 2.0", "https://github.com/Kotlin/kotlinx.coroutines"),
        OssLibrary("Kotlinx Serialization", "Apache License 2.0", "https://github.com/Kotlin/kotlinx.serialization"),
        OssLibrary("Dagger Hilt", "Apache License 2.0", "https://github.com/google/dagger"),
        OssLibrary("Room", "Apache License 2.0", "https://developer.android.com/jetpack/androidx/releases/room"),
        OssLibrary("Google Tink", "Apache License 2.0", "https://github.com/tink-crypto/tink"),
        OssLibrary("Timber", "Apache License 2.0", "https://github.com/JakeWharton/timber"),
        OssLibrary("CameraX", "Apache License 2.0", "https://developer.android.com/training/camerax"),
        OssLibrary("ZXing core", "Apache License 2.0", "https://github.com/zxing/zxing"),
        OssLibrary("Coil", "Apache License 2.0", "https://github.com/coil-kt/coil"),
        OssLibrary("Accompanist Permissions", "Apache License 2.0", "https://github.com/google/accompanist"),
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("About") }, navigationIcon = { BackIconButton(onBack) }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
            Text("OpenFind", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
            Text(
                "OpenFind is a privacy-first, local-network device finder. It has no servers, " +
                    "no accounts, and no analytics — every feature works entirely over your own Wi-Fi, " +
                    "peer to peer, between devices you've explicitly paired.",
                style = MaterialTheme.typography.bodyMedium,
            )
            androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
            Text(
                "OpenFind is free and open source under the Apache License 2.0.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Privacy policy") }, navigationIcon = { BackIconButton(onBack) }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            PrivacySection(
                "No servers, no accounts",
                "OpenFind does not have a backend. There is nothing to sign up for and nothing that could be breached remotely, because there is no remote system at all.",
            )
            PrivacySection(
                "Data never leaves your Wi-Fi",
                "Device discovery, pairing, and every command (ring, vibrate, flash, status) travel directly between your devices over your local network, encrypted end-to-end. None of it is sent to the internet.",
            )
            PrivacySection(
                "What's stored on this device",
                "Trusted device records (name, nickname, public key, last-seen time) are stored locally in an encrypted-at-rest database. You can forget any device at any time from its details screen, which deletes its record immediately.",
            )
            PrivacySection(
                "No analytics or crash reporting",
                "OpenFind does not collect usage analytics or send crash reports anywhere. Diagnostic logs stay on-device and are only shared if you explicitly export them.",
            )
        }
    }
}

@Composable
private fun PrivacySection(
    title: String,
    body: String,
) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Open-source licenses") }, navigationIcon = { BackIconButton(onBack) }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(ossLibraries) { library ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(library.name, style = MaterialTheme.typography.titleSmall)
                        Text(
                            library.license,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
