package com.homejobs.android.domain.repository

import kotlinx.coroutines.flow.StateFlow

data class AppSettings(
    val serverUrl: String = "",
    val apiToken: String = "",
) {
    val isConfigured: Boolean get() = serverUrl.isNotBlank() && apiToken.isNotBlank()
}

interface SettingsRepository {
    /** Latest known settings, always available synchronously (e.g. for the network layer). */
    val settings: StateFlow<AppSettings>

    suspend fun updateServerUrl(url: String)
    suspend fun updateApiToken(token: String)
}
