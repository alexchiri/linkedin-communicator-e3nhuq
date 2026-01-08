package com.kroslabs.linkedincommunicator.data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    companion object {
        val MARKDOWN_MODE_KEY = booleanPreferencesKey("markdown_mode")
        val CLOUD_SYNC_ENABLED_KEY = booleanPreferencesKey("cloud_sync_enabled")
        val LAST_SYNC_TIMESTAMP_KEY = longPreferencesKey("last_sync_timestamp")
    }

    val markdownMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[MARKDOWN_MODE_KEY] ?: false
    }

    val cloudSyncEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[CLOUD_SYNC_ENABLED_KEY] ?: true
    }

    val lastSyncTimestamp: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[LAST_SYNC_TIMESTAMP_KEY] ?: 0L
    }

    suspend fun setMarkdownMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MARKDOWN_MODE_KEY] = enabled
        }
    }

    suspend fun setCloudSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CLOUD_SYNC_ENABLED_KEY] = enabled
        }
    }

    suspend fun setLastSyncTimestamp(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SYNC_TIMESTAMP_KEY] = timestamp
        }
    }
}
