package com.openfinds.app.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.openfinds.app.feature.developer.DeveloperSettingsScreen
import com.openfinds.app.feature.devices.DeviceDetailsScreen
import com.openfinds.app.feature.devices.DevicesScreen
import com.openfinds.app.feature.diagnostics.DiagnosticsScreen
import com.openfinds.app.feature.find.FindDeviceScreen
import com.openfinds.app.feature.groups.DeviceGroupsScreen
import com.openfinds.app.feature.history.HistoryScreen
import com.openfinds.app.feature.home.HomeScreen
import com.openfinds.app.feature.notifications.NotificationsScreen
import com.openfinds.app.feature.onboarding.OnboardingViewModel
import com.openfinds.app.feature.onboarding.PermissionsScreen
import com.openfinds.app.feature.onboarding.WelcomeScreen
import com.openfinds.app.feature.pairing.PairDiscoverScreen
import com.openfinds.app.feature.pairing.PairScanQrScreen
import com.openfinds.app.feature.settings.AboutScreen
import com.openfinds.app.feature.settings.ChangelogScreen
import com.openfinds.app.feature.settings.LicensesScreen
import com.openfinds.app.feature.settings.OpenSourceScreen
import com.openfinds.app.feature.settings.PrivacyPolicyScreen
import com.openfinds.app.feature.settings.SecurityScreen
import com.openfinds.app.feature.settings.SettingsScreen
import com.openfinds.app.feature.settings.WhatsNewScreen

private val topLevelRoutes = setOf(OpenFindDestination.Home, OpenFindDestination.Devices, OpenFindDestination.Settings)

@Composable
fun OpenFindNavHost(
    startDestination: OpenFindDestination,
    openedFromPairingNotification: String?,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestinationRoute = backStackEntry?.destination

    LaunchedEffect(openedFromPairingNotification) {
        if (openedFromPairingNotification != null && startDestination == OpenFindDestination.Home) {
            navController.navigate(OpenFindDestination.PairDiscover)
        }
    }

    val showBottomBar = topLevelRoutes.any { currentDestinationRoute.matches(it) }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                OpenFindBottomBar(navController)
            }
        },
    ) { padding ->
        // Only reserve the bottom-bar's own height here — each screen has its own inner
        // Scaffold (top app bar) which already reserves the status bar inset. Applying the
        // outer Scaffold's full `padding` too would double that top inset, showing as a gap
        // above every screen's title.
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
        ) {
            composable<OpenFindDestination.Welcome> {
                val viewModel: OnboardingViewModel = hiltViewModel()
                WelcomeScreen(
                    onGetStarted = {
                        viewModel.onGetStarted {
                            navController.navigate(OpenFindDestination.Permissions) {
                                popUpTo(OpenFindDestination.Welcome) { inclusive = true }
                            }
                        }
                    },
                )
            }
            composable<OpenFindDestination.Permissions> {
                PermissionsScreen(
                    onContinue = {
                        navController.navigate(OpenFindDestination.Home) {
                            popUpTo(OpenFindDestination.Permissions) { inclusive = true }
                        }
                    },
                )
            }
            composable<OpenFindDestination.WhatsNew> {
                WhatsNewScreen(
                    onContinue = {
                        navController.navigate(OpenFindDestination.Home) {
                            popUpTo(OpenFindDestination.WhatsNew) { inclusive = true }
                        }
                    },
                )
            }

            composable<OpenFindDestination.Home> {
                HomeScreen(
                    onDeviceClick = { id -> navController.navigate(OpenFindDestination.DeviceDetails(id)) },
                    onAddDevice = { navController.navigate(OpenFindDestination.PairDiscover) },
                    onSeeAllDevices = { navController.navigate(OpenFindDestination.Devices) },
                )
            }
            composable<OpenFindDestination.Devices> {
                DevicesScreen(
                    onDeviceClick = { id -> navController.navigate(OpenFindDestination.DeviceDetails(id)) },
                    onAddDevice = { navController.navigate(OpenFindDestination.PairDiscover) },
                    onManageGroups = { navController.navigate(OpenFindDestination.DeviceGroups) },
                )
            }
            composable<OpenFindDestination.DeviceDetails> { entry ->
                val route: OpenFindDestination.DeviceDetails = entry.toRoute()
                DeviceDetailsScreen(
                    onBack = { navController.popBackStack() },
                    onFindDevice = { navController.navigate(OpenFindDestination.FindDevice(route.deviceId)) },
                    onForgotten = { navController.popBackStack() },
                    onViewHistory = { navController.navigate(OpenFindDestination.History) },
                )
            }
            composable<OpenFindDestination.FindDevice> {
                FindDeviceScreen(onClose = { navController.popBackStack() })
            }
            composable<OpenFindDestination.DeviceGroups> {
                DeviceGroupsScreen(onBack = { navController.popBackStack() })
            }

            composable<OpenFindDestination.PairDiscover> {
                PairDiscoverScreen(
                    onBack = { navController.popBackStack() },
                    onScanQr = { navController.navigate(OpenFindDestination.PairScanQr) },
                    onPaired = { navController.popBackStack() },
                )
            }
            composable<OpenFindDestination.PairScanQr> {
                PairScanQrScreen(
                    onClose = { navController.popBackStack() },
                    onPaired = { navController.popBackStack(OpenFindDestination.PairDiscover, inclusive = true) },
                )
            }

            composable<OpenFindDestination.History> {
                HistoryScreen(onBack = { navController.popBackStack() })
            }
            composable<OpenFindDestination.Notifications> {
                NotificationsScreen(onBack = { navController.popBackStack() })
            }

            composable<OpenFindDestination.Settings> {
                SettingsScreen(
                    onOpenGroups = { navController.navigate(OpenFindDestination.DeviceGroups) },
                    onOpenHistory = { navController.navigate(OpenFindDestination.History) },
                    onOpenNotifications = { navController.navigate(OpenFindDestination.Notifications) },
                    onOpenSecurity = { navController.navigate(OpenFindDestination.Security) },
                    onOpenDiagnostics = { navController.navigate(OpenFindDestination.Diagnostics) },
                    onOpenDeveloperSettings = { navController.navigate(OpenFindDestination.DeveloperSettings) },
                    onOpenAbout = { navController.navigate(OpenFindDestination.About) },
                    onOpenPrivacy = { navController.navigate(OpenFindDestination.PrivacyPolicy) },
                    onOpenOpenSource = { navController.navigate(OpenFindDestination.OpenSource) },
                    onOpenChangelog = { navController.navigate(OpenFindDestination.Changelog) },
                )
            }
            composable<OpenFindDestination.Security> { SecurityScreen(onBack = { navController.popBackStack() }) }
            composable<OpenFindDestination.Diagnostics> { DiagnosticsScreen(onBack = { navController.popBackStack() }) }
            composable<OpenFindDestination.DeveloperSettings> { DeveloperSettingsScreen(onBack = { navController.popBackStack() }) }
            composable<OpenFindDestination.About> { AboutScreen(onBack = { navController.popBackStack() }) }
            composable<OpenFindDestination.PrivacyPolicy> { PrivacyPolicyScreen(onBack = { navController.popBackStack() }) }
            composable<OpenFindDestination.Licenses> { LicensesScreen(onBack = { navController.popBackStack() }) }
            composable<OpenFindDestination.OpenSource> {
                OpenSourceScreen(
                    onBack = { navController.popBackStack() },
                    onOpenLicenses = { navController.navigate(OpenFindDestination.Licenses) },
                )
            }
            composable<OpenFindDestination.Changelog> { ChangelogScreen(onBack = { navController.popBackStack() }) }
        }
    }
}

@Composable
private fun OpenFindBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    NavigationBar {
        NavigationBarItem(
            selected = currentDestination.matches(OpenFindDestination.Home),
            onClick = { navController.navigateToTopLevel(OpenFindDestination.Home) },
            icon = {
                Icon(
                    if (currentDestination.matches(OpenFindDestination.Home)) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home",
                )
            },
            label = { Text("Home") },
        )
        NavigationBarItem(
            selected = currentDestination.matches(OpenFindDestination.Devices),
            onClick = { navController.navigateToTopLevel(OpenFindDestination.Devices) },
            icon = {
                Icon(
                    if (currentDestination.matches(OpenFindDestination.Devices)) Icons.Filled.Devices else Icons.Outlined.Devices,
                    contentDescription = "Devices",
                )
            },
            label = { Text("Devices") },
        )
        NavigationBarItem(
            selected = currentDestination.matches(OpenFindDestination.Settings),
            onClick = { navController.navigateToTopLevel(OpenFindDestination.Settings) },
            icon = {
                Icon(
                    if (currentDestination.matches(OpenFindDestination.Settings)) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = "Settings",
                )
            },
            label = { Text("Settings") },
        )
    }
}

private fun NavHostController.navigateToTopLevel(destination: OpenFindDestination) {
    navigate(destination) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * Whether this destination (or a parent in its hierarchy) matches [route].
 * Compose Navigation's own `hasRoute<T>()` resolves to an internal overload
 * from this module, so route matching is done directly against the
 * serialized route's qualified class name instead.
 */
private fun NavDestination?.matches(route: OpenFindDestination): Boolean =
    this?.hierarchy?.any { it.route == route::class.qualifiedName } ?: false
