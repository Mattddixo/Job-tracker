package com.homejobs.config

/**
 * All runtime configuration comes from environment variables so the container
 * can be configured entirely via docker-compose / .env, with no secrets in source control.
 */
data class AppConfig(
    val httpPort: Int,
    val dbUrl: String,
    val dbUser: String,
    val dbPassword: String,
    val apiKey: String,
    val attachmentsDir: String,
    val corsAllowedHost: String?,
) {
    companion object {
        fun fromEnv(): AppConfig {
            fun env(name: String, default: String? = null): String =
                System.getenv(name) ?: default
                    ?: error("Missing required environment variable: $name")

            return AppConfig(
                httpPort = env("HTTP_PORT", "8080").toInt(),
                dbUrl = env("DATABASE_URL"),
                dbUser = env("DATABASE_USER"),
                dbPassword = env("DATABASE_PASSWORD"),
                apiKey = env("API_KEY"),
                attachmentsDir = env("ATTACHMENTS_DIR", "/data/attachments"),
                corsAllowedHost = System.getenv("CORS_ALLOWED_HOST"),
            )
        }
    }
}
