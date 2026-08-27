package com.homejobs.plugins

import com.homejobs.domain.ErrorResponse
import com.homejobs.domain.NotFoundException
import com.homejobs.domain.ValidationException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respond
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("ErrorHandling")

fun Application.configureErrorHandling() {
    install(StatusPages) {
        exception<ValidationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(error = "validation_error", message = "Request failed validation", details = cause.errors),
            )
        }
        exception<NotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(error = "not_found", message = cause.message ?: "Resource not found"),
            )
        }
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(error = "bad_request", message = cause.message ?: "Invalid request"),
            )
        }
        exception<Throwable> { call, cause ->
            logger.error("Unhandled exception on ${call.request.path()}", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(error = "internal_error", message = "An unexpected error occurred"),
            )
        }
        status(HttpStatusCode.NotFound) { call, status ->
            if (call.response.isCommitted) return@status
            call.respond(status, ErrorResponse(error = "not_found", message = "No such route: ${call.request.path()}"))
        }
        status(HttpStatusCode.Unauthorized) { call, status ->
            if (call.response.isCommitted) return@status
            call.respond(status, ErrorResponse(error = "unauthorized", message = "Missing or invalid API key"))
        }
    }
}
