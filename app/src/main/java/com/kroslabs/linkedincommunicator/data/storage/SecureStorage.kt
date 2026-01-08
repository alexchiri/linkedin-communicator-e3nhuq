package com.kroslabs.linkedincommunicator.data.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.kroslabs.linkedincommunicator.logging.DebugLogger

class SecureStorage(context: Context) {
    private val tag = "SecureStorage"

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_API_KEY = "claude_api_key"
    }

    fun saveApiKey(apiKey: String) {
        DebugLogger.d(tag, "Saving API key")
        sharedPreferences.edit().putString(KEY_API_KEY, apiKey).apply()
    }

    fun getApiKey(): String? {
        return sharedPreferences.getString(KEY_API_KEY, null)
    }

    fun hasApiKey(): Boolean {
        return getApiKey()?.isNotBlank() == true
    }

    fun clearApiKey() {
        DebugLogger.d(tag, "Clearing API key")
        sharedPreferences.edit().remove(KEY_API_KEY).apply()
    }
}
