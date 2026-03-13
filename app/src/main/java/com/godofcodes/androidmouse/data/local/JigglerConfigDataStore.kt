package com.godofcodes.androidmouse.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.godofcodes.androidmouse.domain.model.JigglerConfig
import com.godofcodes.androidmouse.domain.model.JigglerPattern
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("jiggler_prefs")

@Singleton
class JigglerConfigDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val ENABLED = booleanPreferencesKey("enabled")
        val INTERVAL_MS = longPreferencesKey("interval_ms")
        val PATTERN = stringPreferencesKey("pattern")
        val MOVE_RANGE = intPreferencesKey("move_range")
    }

    val config: Flow<JigglerConfig> = context.dataStore.data.map { prefs ->
        JigglerConfig(
            enabled = prefs[Keys.ENABLED] ?: false,
            intervalMs = prefs[Keys.INTERVAL_MS] ?: 30_000L,
            pattern = prefs[Keys.PATTERN]?.let { runCatching { JigglerPattern.valueOf(it) }.getOrNull() }
                ?: JigglerPattern.RANDOM,
            moveRange = prefs[Keys.MOVE_RANGE] ?: 10,
        )
    }

    suspend fun save(config: JigglerConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ENABLED] = config.enabled
            prefs[Keys.INTERVAL_MS] = config.intervalMs
            prefs[Keys.PATTERN] = config.pattern.name
            prefs[Keys.MOVE_RANGE] = config.moveRange
        }
    }
}
