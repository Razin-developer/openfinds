package com.openfinds.app.core.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation destinations (Compose Navigation 2.8's `Serializable`
 * route model) for every screen in the app. The splash screen itself is
 * handled by `androidx.core.splashscreen` before Compose content is set, so
 * it has no route here.
 */
sealed interface OpenFindDestination {
    @Serializable data object Welcome : OpenFindDestination

    @Serializable data object Permissions : OpenFindDestination

    @Serializable data object Home : OpenFindDestination

    @Serializable data object Devices : OpenFindDestination

    @Serializable data class DeviceDetails(val deviceId: String) : OpenFindDestination

    @Serializable data class FindDevice(val deviceId: String) : OpenFindDestination

    @Serializable data object DeviceGroups : OpenFindDestination

    @Serializable data object PairDiscover : OpenFindDestination

    @Serializable data object PairScanQr : OpenFindDestination

    @Serializable data object History : OpenFindDestination

    @Serializable data object Notifications : OpenFindDestination

    @Serializable data object Settings : OpenFindDestination

    @Serializable data object Security : OpenFindDestination

    @Serializable data object Diagnostics : OpenFindDestination

    @Serializable data object DeveloperSettings : OpenFindDestination

    @Serializable data object About : OpenFindDestination

    @Serializable data object Licenses : OpenFindDestination

    @Serializable data object PrivacyPolicy : OpenFindDestination

    @Serializable data object OpenSource : OpenFindDestination

    @Serializable data object Changelog : OpenFindDestination

    @Serializable data object WhatsNew : OpenFindDestination
}

/** Top-level destinations shown in the bottom navigation bar. */
enum class TopLevelDestination(val route: OpenFindDestination) {
    HOME(OpenFindDestination.Home),
    DEVICES(OpenFindDestination.Devices),
    SETTINGS(OpenFindDestination.Settings),
}
