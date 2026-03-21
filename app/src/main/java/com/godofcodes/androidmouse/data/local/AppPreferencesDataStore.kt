package com.godofcodes.androidmouse.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appPrefsDataStore by preferencesDataStore("app_prefs")

@Singleton
class AppPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val LAST_DEVICE_ADDRESS = stringPreferencesKey("last_device_address")
        val JIGGLER_TOOLTIP_SHOWN = booleanPreferencesKey("jiggler_tooltip_shown")
    }

    val lastDeviceAddress: Flow<String?> = context.appPrefsDataStore.data
        .map { it[Keys.LAST_DEVICE_ADDRESS] }

    val jigglerTooltipShown: Flow<Boolean> = context.appPrefsDataStore.data
        .map { it[Keys.JIGGLER_TOOLTIP_SHOWN] ?: false }

    suspend fun saveLastDevice(address: String) {
        context.appPrefsDataStore.edit { it[Keys.LAST_DEVICE_ADDRESS] = address }
    }

    suspend fun markJigglerTooltipShown() {
        context.appPrefsDataStore.edit { it[Keys.JIGGLER_TOOLTIP_SHOWN] = true }
    }

    fun getJigglerEnabledForDevice(address: String): Flow<Boolean> =
        context.appPrefsDataStore.data.map { prefs ->
            prefs[booleanPreferencesKey("jiggler_device_$address")] ?: false
        }

    suspend fun saveJigglerEnabledForDevice(address: String, enabled: Boolean) {
        context.appPrefsDataStore.edit { prefs ->
            prefs[booleanPreferencesKey("jiggler_device_$address")] = enabled
        }
    }
}
