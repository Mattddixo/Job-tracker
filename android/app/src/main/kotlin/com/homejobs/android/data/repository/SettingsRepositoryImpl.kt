package com.homejobs.android.data.repository

import com.homejobs.android.data.local.datastore.SettingsDataStore
import com.homejobs.android.di.ApplicationScope
import com.homejobs.android.domain.repository.AppSettings
import com.homejobs.android.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: SettingsDataStore,
    @ApplicationScope scope: CoroutineScope,
) : SettingsRepository {

    override val settings: StateFlow<AppSettings> = dataStore.settingsFlow.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = AppSettings(),
    )

    override suspend fun updateServerUrl(url: String) = dataStore.updateServerUrl(url.trim().trimEnd('/'))

    override suspend fun updateApiToken(token: String) = dataStore.updateApiToken(token.trim())
}
