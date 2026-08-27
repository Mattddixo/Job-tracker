package com.homejobs.plugins

import com.homejobs.repository.JobNoteRepository
import com.homejobs.repository.JobRepository
import com.homejobs.routes.healthRoutes
import com.homejobs.routes.jobNoteRoutes
import com.homejobs.routes.jobRoutes
import io.ktor.server.application.Application
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting(jobRepository: JobRepository, jobNoteRepository: JobNoteRepository) {
    routing {
        healthRoutes()
        route("/api/v1") {
            jobRoutes(jobRepository)
            jobNoteRoutes(jobNoteRepository)
        }
    }
}
