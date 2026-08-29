package com.homejobs.android.domain.repository

import com.homejobs.android.domain.model.Job
import com.homejobs.android.domain.model.JobFilter
import com.homejobs.android.domain.model.JobNote
import com.homejobs.android.domain.model.JobUpsertInput
import com.homejobs.android.domain.model.PaymentMethod
import kotlinx.coroutines.flow.Flow

/**
 * Room is the only store — there's no backend to sync with, so every read is a live Flow off the
 * local database and every write returns as soon as it's committed there.
 */
interface JobRepository {
    fun observeJobs(filter: JobFilter): Flow<List<Job>>
    fun observeJob(id: Long): Flow<Job?>
    fun observeNotes(jobId: Long): Flow<List<JobNote>>

    suspend fun createJob(input: JobUpsertInput): Job
    suspend fun updateJob(id: Long, input: JobUpsertInput): Job
    suspend fun deleteJob(id: Long)

    /** Sets (or clears, with null) which Job Jar task this job is linked to. */
    suspend fun setLinkedJobJarId(id: Long, linkedJobJarId: Long?)

    /** [photoPaths] are app-private file paths already copied in by PhotoStorage. */
    suspend fun addNote(jobId: Long, body: String, photoPaths: List<String> = emptyList()): JobNote
    suspend fun updateNote(noteId: Long, body: String)
    suspend fun deleteNote(jobId: Long, noteId: Long)

    suspend fun addPhotoToNote(noteId: Long, filePath: String)
    suspend fun deletePhoto(photoId: Long)

    fun observePaymentMethods(): Flow<List<PaymentMethod>>
    suspend fun addPaymentMethod(name: String, maxCredit: Double?): PaymentMethod
    suspend fun updatePaymentMethod(id: Long, name: String, maxCredit: Double?)
    suspend fun deletePaymentMethod(id: Long)
}
