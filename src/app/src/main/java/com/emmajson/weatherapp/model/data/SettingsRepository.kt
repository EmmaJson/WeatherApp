package com.emmajson.weatherapp.model.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

// Extension property for DataStore
private val Context.dataStore by preferencesDataStore(name = "settings")

object SettingsPreferences {
    val REFRESH_RATE = intPreferencesKey("refresh_rate")
    val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
    val FORECAST_DAYS = intPreferencesKey("forecast_days") // New preference
}

class SettingsRepository(private val context: Context) {

    // Get refresh rate as a Flow
    val refreshRate: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[SettingsPreferences.REFRESH_RATE] ?: 5
    }

    // Get dark mode setting as a Flow
    val darkModeEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SettingsPreferences.DARK_MODE_ENABLED] ?: false // Default to false
    }

    // Get forecast days as a Flow
    val forecastDays: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[SettingsPreferences.FORECAST_DAYS] ?: 7 // Default to 7 days
    }

    // Save forecast days
    suspend fun setForecastDays(days: Int) {
        println("Saving forecast days: $days")
        context.dataStore.edit { preferences ->
            preferences[SettingsPreferences.FORECAST_DAYS] = days
        }
    }

    // Save refresh rate
    suspend fun setRefreshRate(rate: Int) {
        println("Saving refresh rate: $rate")
        context.dataStore.edit { preferences ->
            preferences[SettingsPreferences.REFRESH_RATE] = rate
        }
    }

    // Save dark mode preference
    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SettingsPreferences.DARK_MODE_ENABLED] = enabled
        }
    }
}
