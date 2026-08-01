package com.openfinds.app.core.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class AppThemeMode { SYSTEM, LIGHT, DARK }

data class AppPreferences(
    val onboardingCompleted: Boolean = false,
    val permissionsAcknowledged: Boolean = false,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val backgroundMonitoringEnabled: Boolean = true,
    val autoReconnectEnabled: Boolean = true,
    val deviceDisplayName: String = "",
    val verboseLoggingEnabled: Boolean = false,
    val showRawDiscoveredDevices: Boolean = false,
    val lastSeenWhatsNewVersionCode: Int = 0,
)

@Singleton
class UserPreferencesRepository
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        private object Keys {
            val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
            val PERMISSIONS_ACKNOWLEDGED = booleanPreferencesKey("permissions_acknowledged")
            val THEME_MODE = stringPreferencesKey("theme_mode")
            val BACKGROUND_MONITORING_ENABLED = booleanPreferencesKey("background_monitoring_enabled")
            val AUTO_RECONNECT_ENABLED = booleanPreferencesKey("auto_reconnect_enabled")
            val DEVICE_DISPLAY_NAME = stringPreferencesKey("device_display_name")
            val VERBOSE_LOGGING_ENABLED = booleanPreferencesKey("verbose_logging_enabled")
            val SHOW_RAW_DISCOVERED_DEVICES = booleanPreferencesKey("show_raw_discovered_devices")
            val LAST_SEEN_WHATS_NEW_VERSION_CODE = intPreferencesKey("last_seen_whats_new_version_code")
        }

        val preferences: Flow<AppPreferences> =
            dataStore.data.map { prefs ->
                AppPreferences(
                    onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
                    permissionsAcknowledged = prefs[Keys.PERMISSIONS_ACKNOWLEDGED] ?: false,
                    themeMode =
                        prefs[Keys.THEME_MODE]?.let { runCatching { AppThemeMode.valueOf(it) }.getOrNull() }
                            ?: AppThemeMode.SYSTEM,
                    backgroundMonitoringEnabled = prefs[Keys.BACKGROUND_MONITORING_ENABLED] ?: true,
                    autoReconnectEnabled = prefs[Keys.AUTO_RECONNECT_ENABLED] ?: true,
                    deviceDisplayName = prefs[Keys.DEVICE_DISPLAY_NAME] ?: "",
                    verboseLoggingEnabled = prefs[Keys.VERBOSE_LOGGING_ENABLED] ?: false,
                    showRawDiscoveredDevices = prefs[Keys.SHOW_RAW_DISCOVERED_DEVICES] ?: false,
                    lastSeenWhatsNewVersionCode = prefs[Keys.LAST_SEEN_WHATS_NEW_VERSION_CODE] ?: 0,
                )
            }

        suspend fun setOnboardingCompleted(completed: Boolean) {
            dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
        }

        suspend fun setPermissionsAcknowledged(acknowledged: Boolean) {
            dataStore.edit { it[Keys.PERMISSIONS_ACKNOWLEDGED] = acknowledged }
        }

        suspend fun setThemeMode(mode: AppThemeMode) {
            dataStore.edit { it[Keys.THEME_MODE] = mode.name }
        }

        suspend fun setBackgroundMonitoringEnabled(enabled: Boolean) {
            dataStore.edit { it[Keys.BACKGROUND_MONITORING_ENABLED] = enabled }
        }

        suspend fun setAutoReconnectEnabled(enabled: Boolean) {
            dataStore.edit { it[Keys.AUTO_RECONNECT_ENABLED] = enabled }
        }

        suspend fun setDeviceDisplayName(name: String) {
            dataStore.edit { it[Keys.DEVICE_DISPLAY_NAME] = name }
        }

        suspend fun setVerboseLoggingEnabled(enabled: Boolean) {
            dataStore.edit { it[Keys.VERBOSE_LOGGING_ENABLED] = enabled }
        }

        suspend fun setShowRawDiscoveredDevices(enabled: Boolean) {
            dataStore.edit { it[Keys.SHOW_RAW_DISCOVERED_DEVICES] = enabled }
        }

        suspend fun setLastSeenWhatsNewVersionCode(versionCode: Int) {
            dataStore.edit { it[Keys.LAST_SEEN_WHATS_NEW_VERSION_CODE] = versionCode }
        }
    }
