package com.homejobs.routes

import com.homejobs.domain.JobFilter
import com.homejobs.domain.JobSortField
import com.homejobs.domain.JobStatus
import com.homejobs.domain.JobUpsertRequest
import com.homejobs.domain.NotFoundException
import com.homejobs.domain.SortDirection
import com.homejobs.repository.JobRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.jobRoutes(repository: JobRepository) {
    route("/jobs") {
        get {
            val filter = JobFilter(
                status = call.request.queryParameters["status"]?.let { parseEnumOrThrow<JobStatus>(it, "status") },
                category = call.request.queryParameters["category"],
                location = call.request.queryParameters["location"],
                sortBy = call.request.queryParameters["sortBy"]?.let { parseEnumOrThrow<JobSortField>(it, "sortBy") }
                    ?: JobSortField.CREATED_AT,
                sortDir = call.request.queryParameters["sortDir"]?.let { parseEnumOrThrow<SortDirection>(it, "sortDir") }
                    ?: SortDirection.DESC,
            )
            call.respond(repository.list(filter))
        }

        post {
            val request = call.receive<JobUpsertRequest>()
            request.validate()
            val job = repository.create(request)
            call.respond(HttpStatusCode.Created, job)
        }

        route("/{id}") {
            get {
                val id = call.pathId()
                val job = repository.findById(id) ?: throw NotFoundException("Job $id not found")
                call.respond(job)
            }

            put {
                val id = call.pathId()
                val request = call.receive<JobUpsertRequest>()
                request.validate()
                val job = repository.update(id, request) ?: throw NotFoundException("Job $id not found")
                call.respond(job)
            }

            delete {
                val id = call.pathId()
                if (!repository.delete(id)) throw NotFoundException("Job $id not found")
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

private inline fun <reified T : Enum<T>> parseEnumOrThrow(value: String, paramName: String): T =
    enumValues<T>().firstOrNull { it.name.equals(value, ignoreCase = true) }
        ?: throw IllegalArgumentException("Invalid $paramName: '$value'. Allowed: ${enumValues<T>().joinToString { it.name }}")

internal fun io.ktor.server.application.ApplicationCall.pathId(): Long {
    val raw = parameters["id"] ?: throw IllegalArgumentException("Missing id path parameter")
    return raw.toLongOrNull() ?: throw IllegalArgumentException("Invalid id: '$raw'")
}
