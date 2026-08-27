package com.homejobs.routes

import com.homejobs.domain.JobNoteCreateRequest
import com.homejobs.domain.NotFoundException
import com.homejobs.repository.JobNoteRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.jobNoteRoutes(repository: JobNoteRepository) {
    route("/jobs/{id}/notes") {
        get {
            val jobId = call.pathId()
            if (!repository.jobExists(jobId)) throw NotFoundException("Job $jobId not found")
            call.respond(repository.listForJob(jobId))
        }

        post {
            val jobId = call.pathId()
            if (!repository.jobExists(jobId)) throw NotFoundException("Job $jobId not found")
            val request = call.receive<JobNoteCreateRequest>()
            request.validate()
            call.respond(HttpStatusCode.Created, repository.create(jobId, request))
        }

        delete("/{noteId}") {
            val jobId = call.pathId()
            val noteId = call.parameters["noteId"]?.toLongOrNull()
                ?: throw IllegalArgumentException("Invalid noteId")
            if (!repository.delete(jobId, noteId)) throw NotFoundException("Note $noteId not found on job $jobId")
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
