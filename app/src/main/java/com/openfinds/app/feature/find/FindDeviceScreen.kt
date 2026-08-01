package com.openfinds.app.feature.find

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun FindDeviceScreen(
    onClose: () -> Unit,
    viewModel: FindDeviceViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.primary) {
        Box(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = {
                    viewModel.stopFind()
                    onClose()
                },
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Stop finding", tint = MaterialTheme.colorScheme.onPrimary)
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                when (state.phase) {
                    FindPhase.CONNECTING -> {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.height(16.dp))
                        Text("Reaching ${state.device?.name ?: "device"}…", color = MaterialTheme.colorScheme.onPrimary)
                    }
                    FindPhase.FAILED -> {
                        Text(
                            "Couldn't reach ${state.device?.name ?: "this device"}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            state.errorMessage ?: "Make sure it's on the same Wi-Fi network.",
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(24.dp))
                        OutlinedButton(onClick = onClose) { Text("Close") }
                    }
                    FindPhase.ACTIVE, FindPhase.STOPPED -> {
                        PulseRing()
                        Spacer(Modifier.height(32.dp))
                        Text(
                            state.device?.name ?: "Device",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Text(
                            if (state.phase == FindPhase.STOPPED) "Stopped" else "Ringing & vibrating",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        )
                        Spacer(Modifier.height(40.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            FilledIconToggleButton(checked = state.ringing, onCheckedChange = {}, enabled = false) {
                                Icon(Icons.Filled.VolumeUp, contentDescription = "Ringing")
                            }
                            FilledIconToggleButton(checked = state.vibrating, onCheckedChange = {}, enabled = false) {
                                Icon(Icons.Filled.Vibration, contentDescription = "Vibrating")
                            }
                            FilledIconToggleButton(checked = state.flashing, onCheckedChange = { viewModel.toggleFlash() }) {
                                Icon(Icons.Filled.FlashlightOn, contentDescription = "Flashlight")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PulseRing() {
    val transition = rememberInfiniteTransition(label = "find-pulse")
    val scale by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Reverse),
        label = "scale",
    )
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier =
                Modifier
                    .size((180 * scale).dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
        )
        Box(
            modifier =
                Modifier
                    .size(120.dp)
                    .background(Color.White.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.MyLocation, contentDescription = null, tint = Color.White, modifier = Modifier.size(56.dp))
        }
    }
}
