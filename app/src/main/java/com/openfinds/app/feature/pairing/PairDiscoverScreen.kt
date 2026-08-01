package com.openfinds.app.feature.pairing

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
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openfinds.app.core.domain.model.DiscoveredDevice
import com.openfinds.app.core.network.PairingOutcome
import com.openfinds.app.core.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairDiscoverScreen(
    onBack: () -> Unit,
    onScanQr: () -> Unit,
    onPaired: () -> Unit,
    viewModel: PairDiscoverViewModel = hiltViewModel(),
) {
    val nearbyDevices by viewModel.nearbyDevices.collectAsStateWithLifecycle()
    val bleNearbySignal by viewModel.bleNearbySignal.collectAsStateWithLifecycle()
    val activePin by viewModel.activePin.collectAsStateWithLifecycle()
    val myQrPayload by viewModel.myQrPayload.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val incomingRequest by viewModel.incomingPairingRequests.collectAsStateWithLifecycle()

    var pinEntryTarget by remember { mutableStateOf<DiscoveredDevice?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pair a device") },
                navigationIcon = { com.openfinds.app.core.ui.components.BackIconButton(onBack) },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onScanQr, modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Icon(Icons.Outlined.QrCode, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Scan a QR code")
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { viewModel.loadMyQrPayload() }, modifier = Modifier.weight(1f)) {
                    Text("Show my QR")
                }
                OutlinedButton(onClick = { viewModel.startPinPairing() }, modifier = Modifier.weight(1f)) {
                    Text("Show my PIN")
                }
            }

            if (bleNearbySignal) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "A nearby device was also detected over Bluetooth.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(24.dp))
            Text("Nearby devices", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (nearbyDevices.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Wifi,
                    title = "Searching for devices…",
                    message = "Make sure the other device has OpenFind open and is on the same Wi-Fi network.",
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(nearbyDevices, key = { it.host + it.port }) { device ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column {
                                    Text(device.serviceName, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        device.host,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                TextButton(onClick = { pinEntryTarget = device }) { Text("Pair") }
                            }
                        }
                    }
                }
            }
        }
    }

    activePin?.let { pin ->
        AlertDialog(
            onDismissRequest = { viewModel.stopPinPairing() },
            title = { Text("Your pairing PIN") },
            text = {
                Column {
                    Text("Enter this on the other device within a minute:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        pin,
                        style = MaterialTheme.typography.displaySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.stopPinPairing() }) { Text("Done") } },
        )
    }

    myQrPayload?.let { payload ->
        AlertDialog(
            onDismissRequest = { viewModel.clearMyQrPayload() },
            title = { Text("Your pairing QR code") },
            text = {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    QrCodeImage(content = payload)
                    Spacer(Modifier.height(12.dp))
                    Text("Scan this from the other device", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.clearMyQrPayload() }) { Text("Done") } },
        )
    }

    pinEntryTarget?.let { device ->
        var enteredPin by rememberSaveable { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { pinEntryTarget = null },
            title = { Text("Enter PIN from ${device.serviceName}") },
            text = {
                OutlinedTextField(
                    value = enteredPin,
                    onValueChange = { if (it.length <= 6) enteredPin = it.filter(Char::isDigit) },
                    label = { Text("6-digit PIN") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    enabled = enteredPin.length == 6,
                    onClick = {
                        viewModel.pairWithPin(device, enteredPin)
                        pinEntryTarget = null
                    },
                ) { Text("Pair") }
            },
            dismissButton = { TextButton(onClick = { pinEntryTarget = null }) { Text("Cancel") } },
        )
    }

    incomingRequest?.let { request ->
        AlertDialog(
            onDismissRequest = { viewModel.rejectIncoming(request) },
            title = { Text("Pairing request") },
            text = { Text("${request.fromDeviceName} wants to pair with this device. Accept?") },
            confirmButton = { TextButton(onClick = { viewModel.acceptIncoming(request) }) { Text("Accept") } },
            dismissButton = { TextButton(onClick = { viewModel.rejectIncoming(request) }) { Text("Reject") } },
        )
    }

    when (val state = uiState) {
        is PairingUiState.Connecting ->
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Pairing…") },
                text = { CircularProgressIndicator() },
                confirmButton = {},
            )
        is PairingUiState.Result -> {
            val outcome = state.outcome
            AlertDialog(
                onDismissRequest = { viewModel.resetUiState() },
                title = { Text(if (outcome is PairingOutcome.Success) "Paired!" else "Pairing failed") },
                text = {
                    Text(
                        when (outcome) {
                            is PairingOutcome.Success -> "${outcome.peerDeviceName} is now a trusted device."
                            is PairingOutcome.Failure -> outcome.reason
                        },
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.resetUiState()
                            if (outcome is PairingOutcome.Success) onPaired()
                        },
                    ) { Text("OK") }
                },
            )
        }
        PairingUiState.Idle -> Unit
    }
}
