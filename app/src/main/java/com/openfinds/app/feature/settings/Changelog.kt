package com.openfinds.app.feature.settings

/**
 * Mirrors CHANGELOG.md for in-app display. Keep this in sync whenever a new
 * version is released — see CONTRIBUTING.md.
 */
data class ChangelogEntry(val version: String, val date: String, val highlights: List<String>)

val changelogEntries =
    listOf(
        ChangelogEntry(
            version = "0.1.0",
            date = "2026-07-31",
            highlights =
                listOf(
                    "Onboarding, permission education, and encrypted QR/PIN pairing",
                    "Local-network discovery via NSD, UDP beacon, and BLE",
                    "Device dashboard: battery, storage, RAM, uptime, last seen",
                    "Ring, vibrate, and flashlight find mode",
                    "Device groups, activity history, and diagnostics/log export",
                    "Background monitoring with auto-reconnect and boot start",
                ),
        ),
    )
