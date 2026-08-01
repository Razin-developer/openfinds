package com.openfinds.app.screenshots

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Devices
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.openfinds.app.core.domain.model.ConnectionState
import com.openfinds.app.core.domain.model.TrustedDevice
import com.openfinds.app.core.ui.components.EmptyState
import com.openfinds.app.core.ui.theme.OpenFindTheme
import com.openfinds.app.feature.devices.DeviceListItem
import com.openfinds.app.feature.onboarding.WelcomeScreen
import org.junit.Rule
import org.junit.Test

/**
 * JVM-rendered screenshot tests (no emulator needed) covering the app's
 * core visual style for light and dark theme. Run `./gradlew recordPaparazzi`
 * to regenerate the golden images under app/src/test/snapshots.
 */
class ScreenshotTest {
    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6)

    private val sampleDevice =
        TrustedDevice(
            id = "1",
            displayName = "Pixel 8 Pro",
            nickname = null,
            avatarColorArgb = 0xFF5B6CFF.toInt(),
            publicKeyBase64 = "key",
            lastKnownHost = "192.168.1.10",
            lastKnownPort = 47331,
            pairedAtEpochMillis = 0,
            lastSeenEpochMillis = System.currentTimeMillis(),
            connectionState = ConnectionState.ONLINE,
        )

    @Test
    fun welcomeScreen_light() {
        paparazzi.snapshot {
            OpenFindTheme(darkTheme = false) { WelcomeScreen(onGetStarted = {}) }
        }
    }

    @Test
    fun welcomeScreen_dark() {
        paparazzi.snapshot {
            OpenFindTheme(darkTheme = true) { WelcomeScreen(onGetStarted = {}) }
        }
    }

    @Test
    fun emptyState_noTrustedDevices() {
        paparazzi.snapshot {
            OpenFindTheme(darkTheme = false) {
                EmptyState(
                    icon = Icons.Outlined.Devices,
                    title = "No trusted devices yet",
                    message = "Pair your first device to see it here.",
                )
            }
        }
    }

    @Test
    fun deviceListItem_online() {
        paparazzi.snapshot {
            OpenFindTheme(darkTheme = false) { DeviceListItem(device = sampleDevice, onClick = {}) }
        }
    }
}
