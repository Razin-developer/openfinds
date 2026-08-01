package com.openfinds.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.openfinds.app.core.background.DeviceMonitorService
import com.openfinds.app.core.background.PairingNotifier
import com.openfinds.app.core.navigation.OpenFindNavHost
import com.openfinds.app.core.ui.theme.OpenFindTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var pendingPairingDeviceName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var isReady by mutableStateOf(false)
        splashScreen.setKeepOnScreenCondition { !isReady }

        handleIntent(intent)

        setContent {
            OpenFindTheme {
                val mainViewModel: MainActivityViewModel = viewModel()
                val startState by mainViewModel.startDestination.collectAsStateWithLifecycle()
                val resolved = startState
                isReady = resolved != null
                if (resolved != null) {
                    OpenFindNavHost(startDestination = resolved, openedFromPairingNotification = pendingPairingDeviceName)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Discovery, pairing, and being-found all depend on DeviceMonitorService's P2P
        // listen socket and NSD/UDP/BLE advertising actually running — start it here so
        // that's true the moment the app is opened, not only after a reboot or Wi-Fi
        // change (the only other triggers). onStartCommand is idempotent, so repeated
        // calls across app foreground/background cycles are safe.
        ContextCompat.startForegroundService(this, Intent(this, DeviceMonitorService::class.java))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == PairingNotifier.ACTION_OPEN_PAIRING) {
            pendingPairingDeviceName = intent.getStringExtra(PairingNotifier.EXTRA_DEVICE_NAME)
        }
    }
}
