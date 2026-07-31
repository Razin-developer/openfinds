package com.openfinds.app.core.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation destinations (Compose Navigation 2.8's `Serializable`
 * route model) for every screen in the app.
 */
sealed interface OpenFindDestination {

    @Serializable data object Splash : OpenFindDestination
    @Serializable data object Welcome : OpenFindDestination
    @Serializable data object OnboardingIntro : OpenFindDestination
    @Serializable data object Permissions : OpenFindDestination

    @Serializable data object Home : OpenFindDestination
    @Serializable data object Devices : OpenFindDestination
    @Serializable data class DeviceDetails(val deviceId: String) : OpenFindDestination
    @Serializable data class FindDevice(val deviceId: String) : OpenFindDestination

    @Serializable data object PairDiscover : OpenFindDestination
    @Serializable data class PairConfirm(val deviceId: String, val method: String) : OpenFindDestination
    @Serializable data object PairScanQr : OpenFindDestination

    @Serializable data object Settings : OpenFindDestination
    @Serializable data object About : OpenFindDestination
    @Serializable data object Licenses : OpenFindDestination
    @Serializable data object PrivacyPolicy : OpenFindDestination

    // Device groups, history log, diagnostics/log export, developer options,
    // and rich notification management are planned for a follow-up phase and
    // intentionally have no route here yet — see ROADMAP.md.
}

/** Top-level destinations shown in the bottom navigation bar. */
enum class TopLevelDestination(val route: OpenFindDestination) {
    HOME(OpenFindDestination.Home),
    DEVICES(OpenFindDestination.Devices),
    SETTINGS(OpenFindDestination.Settings),
}
