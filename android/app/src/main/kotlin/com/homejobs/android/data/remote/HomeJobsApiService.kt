package com.homejobs.android.data.remote

import com.homejobs.android.data.remote.dto.JobDto
import com.homejobs.android.data.remote.dto.JobNoteCreateRequestDto
import com.homejobs.android.data.remote.dto.JobNoteDto
import com.homejobs.android.data.remote.dto.JobUpsertRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Paths are relative to a placeholder base URL; [com.homejobs.android.data.remote.DynamicBaseUrlInterceptor]
 * rewrites the scheme/host/port (and prepends any path prefix) from the user's configured server URL
 * on every request, so this service never hardcodes an endpoint.
 */
interface HomeJobsApiService {
    @GET("api/v1/jobs")
    suspend fun listJobs(
        @Query("status") status: String? = null,
        @Query("category") category: String? = null,
        @Query("location") location: String? = null,
    ): List<JobDto>

    @GET("api/v1/jobs/{id}")
    suspend fun getJob(@Path("id") id: Long): JobDto

    @POST("api/v1/jobs")
    suspend fun createJob(@Body request: JobUpsertRequestDto): JobDto

    @PUT("api/v1/jobs/{id}")
    suspend fun updateJob(@Path("id") id: Long, @Body request: JobUpsertRequestDto): JobDto

    @DELETE("api/v1/jobs/{id}")
    suspend fun deleteJob(@Path("id") id: Long)

    @GET("api/v1/jobs/{id}/notes")
    suspend fun listNotes(@Path("id") jobId: Long): List<JobNoteDto>

    @POST("api/v1/jobs/{id}/notes")
    suspend fun addNote(@Path("id") jobId: Long, @Body request: JobNoteCreateRequestDto): JobNoteDto

    @DELETE("api/v1/jobs/{id}/notes/{noteId}")
    suspend fun deleteNote(@Path("id") jobId: Long, @Path("noteId") noteId: Long)
}
