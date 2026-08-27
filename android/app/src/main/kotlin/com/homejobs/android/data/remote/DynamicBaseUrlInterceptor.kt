package com.homejobs.android.data.remote

import com.homejobs.android.domain.repository.SettingsRepository
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

/**
 * The server URL and API token are user-configurable at runtime (Settings screen), so Retrofit
 * is built once against a placeholder base URL and this interceptor swaps in the real
 * scheme/host/port (preserving any reverse-proxy path prefix, e.g. `https://host/homejobs`) and
 * attaches the bearer token, on every request.
 */
class DynamicBaseUrlInterceptor @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val settings = settingsRepository.settings.value
        if (!settings.isConfigured) {
            throw IOException("Server URL and API token are not configured yet. Set them in Settings.")
        }

        val configuredBase = settings.serverUrl.toHttpUrlOrNull()
            ?: throw IOException("Configured server URL '${settings.serverUrl}' is not a valid URL.")

        val original = chain.request()
        val newUrl = original.url.newBuilder()
            .scheme(configuredBase.scheme)
            .host(configuredBase.host)
            .port(configuredBase.port)
            .encodedPath(configuredBase.encodedPath.trimEnd('/') + original.url.encodedPath)
            .build()

        val newRequest = original.newBuilder()
            .url(newUrl)
            .header("Authorization", "Bearer ${settings.apiToken}")
            .build()

        return chain.proceed(newRequest)
    }
}
