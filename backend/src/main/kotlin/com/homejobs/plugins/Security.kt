package com.homejobs.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.request.path
import io.ktor.server.request.header
import io.ktor.server.response.respond

/**
 * Single-user API-key auth: every request under the /api path must carry
 * `Authorization: Bearer API_KEY` matching the server's configured key.
 *
 * A hand-rolled intercept (rather than Ktor's Authentication plugin or JWT) is
 * intentional: for a single household user there is no session and no user
 * table, and nothing a JWT's claims or expiry would buy over one static
 * secret compared directly.
 */
fun Application.configureSecurity(apiKey: String) {
    intercept(ApplicationCallPipeline.Plugins) {
        val path = call.request.path()
        if (!path.startsWith("/api/")) return@intercept

        val header = call.request.header("Authorization")
        val token = header?.removePrefix("Bearer ")?.trim()
        if (token != apiKey) {
            call.respond(HttpStatusCode.Unauthorized)
            finish()
        }
    }
}
