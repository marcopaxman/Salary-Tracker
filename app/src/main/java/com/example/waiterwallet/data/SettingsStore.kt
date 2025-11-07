package com.example.waiterwallet.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime

private const val SETTINGS_NAME = "waiter_wallet_settings"
val Context.dataStore by preferencesDataStore(name = SETTINGS_NAME)

object SettingsKeys {
    val COMMISSION_PERCENT = doublePreferencesKey("commission_percent")
    val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
    val REMINDER_HOUR = intPreferencesKey("reminder_hour")
    val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
}

class SettingsStore(private val context: Context) {
    val commissionPercent: Flow<Double> = context.dataStore.data.map { it[SettingsKeys.COMMISSION_PERCENT] ?: 0.01 }
    val reminderEnabled: Flow<Boolean> = context.dataStore.data.map { it[SettingsKeys.REMINDER_ENABLED] ?: true }
    val reminderTime: Flow<LocalTime> = context.dataStore.data.map {
        val h = it[SettingsKeys.REMINDER_HOUR] ?: 22
        val m = it[SettingsKeys.REMINDER_MINUTE] ?: 0
        LocalTime.of(h, m)
    }

    suspend fun setCommissionPercent(value: Double) {
        context.dataStore.edit { it[SettingsKeys.COMMISSION_PERCENT] = value }
    }

    suspend fun setReminder(enabled: Boolean, time: LocalTime) {
        context.dataStore.edit {
            it[SettingsKeys.REMINDER_ENABLED] = enabled
            it[SettingsKeys.REMINDER_HOUR] = time.hour
            it[SettingsKeys.REMINDER_MINUTE] = time.minute
        }
    }
}
