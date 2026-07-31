package com.openfinds.app.feature.pairing

import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openfinds.app.core.network.PairingOutcome
import java.util.concurrent.Executors

@Composable
fun PairScanQrScreen(
    onClose: () -> Unit,
    onPaired: () -> Unit,
    viewModel: PairDiscoverViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasDecoded by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val onDecodedState = rememberUpdatedState<(QrPairingPayload) -> Unit> { payload ->
        viewModel.pairWithQrPayload(payload)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    val cameraExecutor = Executors.newSingleThreadExecutor()
                    val providerFuture = ProcessCameraProvider.getInstance(ctx)
                    providerFuture.addListener({
                        val cameraProvider = providerFuture.get()
                        val preview = Preview.Builder().build().also { it.surfaceProvider = surfaceProvider }
                        val analysis = ImageAnalysis.Builder()
                            .setTargetResolution(Size(1280, 720))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(
                                    cameraExecutor,
                                    QrScannerAnalyzer { text ->
                                        if (!hasDecoded) {
                                            QrPairingPayload.decode(text)?.let { payload ->
                                                hasDecoded = true
                                                onDecodedState.value(payload)
                                            }
                                        }
                                    },
                                )
                            }
                        runCatching {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                analysis,
                            )
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
        )

        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopStart)) {
                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
            }
            Text(
                "Point your camera at the other device's pairing QR code",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
            )
        }
    }

    when (val state = uiState) {
        is PairingUiState.Connecting -> AlertDialog(
            onDismissRequest = {},
            title = { Text("Pairing…") },
            text = { CircularProgressIndicator() },
            confirmButton = {},
        )
        is PairingUiState.Result -> {
            val outcome = state.outcome
            AlertDialog(
                onDismissRequest = { viewModel.resetUiState(); hasDecoded = false },
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
                            if (outcome is PairingOutcome.Success) onPaired() else hasDecoded = false
                        },
                    ) { Text("OK") }
                },
            )
        }
        PairingUiState.Idle -> Unit
    }
}
