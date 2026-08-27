package com.homejobs.integration

import com.homejobs.config.AppConfig
import com.homejobs.domain.Job
import com.homejobs.domain.JobNote
import com.homejobs.domain.JobNoteCreateRequest
import com.homejobs.domain.JobStatus
import com.homejobs.domain.JobUpsertRequest
import com.homejobs.module
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

private const val API_KEY = "integration-test-key"

/**
 * A fresh Postgres container per test (an instance, not static, @Container
 * field) trades speed for trivial isolation: no test needs to reason about
 * data another test left behind.
 */
@Testcontainers
class JobApiIntegrationTest {

    @Container
    private val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")
        .withDatabaseName("homejobs_test")
        .withUsername("test")
        .withPassword("test")

    private fun config() = AppConfig(
        httpPort = 0,
        dbUrl = postgres.jdbcUrl,
        dbUser = postgres.username,
        dbPassword = postgres.password,
        apiKey = API_KEY,
        attachmentsDir = "/tmp/homejobs-test-attachments",
        corsAllowedHost = null,
    )

    private fun runApiTest(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
        application { module(config()) }
        block()
    }

    private fun ApplicationTestBuilder.jsonClient() = createClient {
        install(ContentNegotiation) { json() }
    }

    @Test
    fun `health check is unauthenticated`() = runApiTest {
        val response: HttpResponse = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `requests without api key are rejected`() = runApiTest {
        val response = client.get("/api/v1/jobs")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `create then fetch a job round-trips all fields`() = runApiTest {
        val client = jsonClient()
        val created = client.post("/api/v1/jobs") {
            header("Authorization", "Bearer $API_KEY")
            contentType(ContentType.Application.Json)
            setBody(
                JobUpsertRequest(
                    title = "Replace water heater",
                    category = "Plumbing",
                    location = "Basement",
                    vendorName = "Acme Plumbing",
                    quotedCost = 1200.0,
                    actualCost = 1350.0,
                    predictedHours = 4.0,
                    actualHours = 5.5,
                ),
            )
        }
        assertEquals(HttpStatusCode.Created, created.status)
        val createdJob = created.body<Job>()
        assertEquals("Replace water heater", createdJob.title)
        assertEquals(150.0, createdJob.costVariance)
        assertEquals(1.5, createdJob.timeVariance)

        val fetched = client.get("/api/v1/jobs/${createdJob.id}") {
            header("Authorization", "Bearer $API_KEY")
        }
        assertEquals(HttpStatusCode.OK, fetched.status)
        assertEquals(createdJob.id, fetched.body<Job>().id)
    }

    @Test
    fun `fetching a missing job returns 404 with structured error`() = runApiTest {
        val response = client.get("/api/v1/jobs/999999") {
            header("Authorization", "Bearer $API_KEY")
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertTrue(response.body<String>().contains("not_found"))
    }

    @Test
    fun `invalid create request returns 400 with validation details`() = runApiTest {
        val client = jsonClient()
        val response = client.post("/api/v1/jobs") {
            header("Authorization", "Bearer $API_KEY")
            contentType(ContentType.Application.Json)
            setBody(JobUpsertRequest(title = "  "))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.body<String>().contains("validation_error"))
    }

    @Test
    fun `list filters by status and category`() = runApiTest {
        val client = jsonClient()
        suspend fun create(title: String, category: String, status: JobStatus) {
            client.post("/api/v1/jobs") {
                header("Authorization", "Bearer $API_KEY")
                contentType(ContentType.Application.Json)
                setBody(JobUpsertRequest(title = title, category = category, status = status))
            }
        }
        create("Paint fence", "Painting", JobStatus.DONE)
        create("Paint shed", "Painting", JobStatus.QUOTED)
        create("Fix gutter", "Repairs", JobStatus.DONE)

        val response = client.get("/api/v1/jobs") {
            header("Authorization", "Bearer $API_KEY")
            parameter("category", "Painting")
            parameter("status", "DONE")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val jobs = response.body<List<Job>>()
        assertEquals(1, jobs.size)
        assertEquals("Paint fence", jobs.single().title)
    }

    @Test
    fun `update replaces fields and delete removes the job`() = runApiTest {
        val client = jsonClient()
        val created = client.post("/api/v1/jobs") {
            header("Authorization", "Bearer $API_KEY")
            contentType(ContentType.Application.Json)
            setBody(JobUpsertRequest(title = "Original title"))
        }.body<Job>()

        val updated = client.put("/api/v1/jobs/${created.id}") {
            header("Authorization", "Bearer $API_KEY")
            contentType(ContentType.Application.Json)
            setBody(JobUpsertRequest(title = "Updated title", status = JobStatus.DONE))
        }
        assertEquals(HttpStatusCode.OK, updated.status)
        assertEquals("Updated title", updated.body<Job>().title)

        val deleted = client.delete("/api/v1/jobs/${created.id}") {
            header("Authorization", "Bearer $API_KEY")
        }
        assertEquals(HttpStatusCode.NoContent, deleted.status)

        val afterDelete = client.get("/api/v1/jobs/${created.id}") {
            header("Authorization", "Bearer $API_KEY")
        }
        assertEquals(HttpStatusCode.NotFound, afterDelete.status)
    }

    @Test
    fun `notes are created listed and cascade-deleted with the job`() = runApiTest {
        val client = jsonClient()
        val job = client.post("/api/v1/jobs") {
            header("Authorization", "Bearer $API_KEY")
            contentType(ContentType.Application.Json)
            setBody(JobUpsertRequest(title = "Job with notes"))
        }.body<Job>()

        val note = client.post("/api/v1/jobs/${job.id}/notes") {
            header("Authorization", "Bearer $API_KEY")
            contentType(ContentType.Application.Json)
            setBody(JobNoteCreateRequest(body = "Contractor called to reschedule."))
        }
        assertEquals(HttpStatusCode.Created, note.status)
        assertEquals(job.id, note.body<JobNote>().jobId)

        val notes = client.get("/api/v1/jobs/${job.id}/notes") {
            header("Authorization", "Bearer $API_KEY")
        }.body<List<JobNote>>()
        assertEquals(1, notes.size)

        client.delete("/api/v1/jobs/${job.id}") {
            header("Authorization", "Bearer $API_KEY")
        }

        val notesAfterJobDeleted = client.get("/api/v1/jobs/${job.id}/notes") {
            header("Authorization", "Bearer $API_KEY")
        }
        assertEquals(HttpStatusCode.NotFound, notesAfterJobDeleted.status)
    }
}
