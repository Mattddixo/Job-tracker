package com.homejobs

import com.homejobs.config.AppConfig
import com.homejobs.db.DatabaseFactory
import com.homejobs.plugins.configureErrorHandling
import com.homejobs.plugins.configureLogging
import com.homejobs.plugins.configureRouting
import com.homejobs.plugins.configureSecurity
import com.homejobs.plugins.configureSerialization
import com.homejobs.repository.JobNoteRepository
import com.homejobs.repository.JobRepository
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    val config = AppConfig.fromEnv()
    embeddedServer(Netty, port = config.httpPort, host = "0.0.0.0", module = { module(config) })
        .start(wait = true)
}

fun Application.module(config: AppConfig = AppConfig.fromEnv()) {
    DatabaseFactory.init(config)
    configureSerialization()
    configureLogging()
    configureErrorHandling()
    configureSecurity(config.apiKey)
    configureRouting(JobRepository(), JobNoteRepository())
}
