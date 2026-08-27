package com.homejobs.android.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import android.content.Context
import com.homejobs.android.domain.repository.AppSettings
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val SERVER_URL = stringPreferencesKey("server_url")
        val API_TOKEN = stringPreferencesKey("api_token")
    }

    val settingsFlow = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            serverUrl = prefs[Keys.SERVER_URL].orEmpty(),
            apiToken = prefs[Keys.API_TOKEN].orEmpty(),
        )
    }

    suspend fun updateServerUrl(url: String) {
        context.settingsDataStore.edit { it[Keys.SERVER_URL] = url }
    }

    suspend fun updateApiToken(token: String) {
        context.settingsDataStore.edit { it[Keys.API_TOKEN] = token }
    }
}
