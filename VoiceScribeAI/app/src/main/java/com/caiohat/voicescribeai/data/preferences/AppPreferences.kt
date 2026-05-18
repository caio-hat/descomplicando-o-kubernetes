package com.caiohat.voicescribeai.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.caiohat.voicescribeai.domain.models.AIProvider
import com.caiohat.voicescribeai.domain.models.WhisperModelSize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "voice_scribe_prefs")

class AppPreferences(private val context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "voice_scribe_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private val KEY_AI_PROVIDER = stringPreferencesKey("ai_provider")
        private val KEY_WHISPER_MODEL = stringPreferencesKey("whisper_model")
        private val KEY_GEMMA_DOWNLOADED = booleanPreferencesKey("gemma_downloaded")
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"
    }

    val aiProvider: Flow<AIProvider> = context.dataStore.data.map { prefs ->
        val name = prefs[KEY_AI_PROVIDER] ?: AIProvider.GEMINI_API.name
        AIProvider.valueOf(name)
    }

    val whisperModelSize: Flow<WhisperModelSize> = context.dataStore.data.map { prefs ->
        val name = prefs[KEY_WHISPER_MODEL] ?: WhisperModelSize.BASE.name
        WhisperModelSize.valueOf(name)
    }

    val isGemmaDownloaded: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_GEMMA_DOWNLOADED] ?: false
    }

    suspend fun setAiProvider(provider: AIProvider) {
        context.dataStore.edit { it[KEY_AI_PROVIDER] = provider.name }
    }

    suspend fun setWhisperModelSize(size: WhisperModelSize) {
        context.dataStore.edit { it[KEY_WHISPER_MODEL] = size.name }
    }

    suspend fun setGemmaDownloaded(downloaded: Boolean) {
        context.dataStore.edit { it[KEY_GEMMA_DOWNLOADED] = downloaded }
    }

    fun getGeminiApiKey(): String = encryptedPrefs.getString(KEY_GEMINI_API_KEY, "") ?: ""

    fun setGeminiApiKey(key: String) {
        encryptedPrefs.edit().putString(KEY_GEMINI_API_KEY, key).apply()
    }
}
