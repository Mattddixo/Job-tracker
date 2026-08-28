package com.homejobs.android.data.repository

import com.homejobs.android.data.local.db.JobDao
import com.homejobs.android.data.local.db.JobNoteDao
import com.homejobs.android.data.local.db.JobNoteEntity
import com.homejobs.android.data.local.db.PaymentMethodDao
import com.homejobs.android.data.local.db.PaymentMethodEntity
import com.homejobs.android.data.local.db.PhotoDao
import com.homejobs.android.data.local.db.PhotoEntity
import com.homejobs.android.data.local.photo.PhotoStorage
import com.homejobs.android.domain.model.Job
import com.homejobs.android.domain.model.JobFilter
import com.homejobs.android.domain.model.JobNote
import com.homejobs.android.domain.model.JobSortField
import com.homejobs.android.domain.model.JobUpsertInput
import com.homejobs.android.domain.model.PaymentMethod
import com.homejobs.android.domain.model.Photo
import com.homejobs.android.domain.model.SortDirection
import com.homejobs.android.domain.repository.JobRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JobRepositoryImpl @Inject constructor(
    private val jobDao: JobDao,
    private val jobNoteDao: JobNoteDao,
    private val photoDao: PhotoDao,
    private val photoStorage: PhotoStorage,
    private val paymentMethodDao: PaymentMethodDao,
) : JobRepository {

    override fun observeJobs(filter: JobFilter): Flow<List<Job>> =
        jobDao.observeJobs(
            status = filter.status?.name,
            category = filter.category,
            location = filter.location,
        ).map { entities -> entities.map { it.toDomain() }.sortedWith(comparator(filter)) }

    override fun observeJob(id: Long): Flow<Job?> = jobDao.observeJob(id).map { it?.toDomain() }

    override fun observeNotes(jobId: Long): Flow<List<JobNote>> =
        jobNoteDao.observeNotesWithPhotos(jobId).map { notes -> notes.map { it.toDomain() } }

    override suspend fun createJob(input: JobUpsertInput): Job {
        val now = System.currentTimeMillis()
        val entity = input.toEntity(createdAt = now, updatedAt = now)
        val id = jobDao.insert(entity)
        return entity.copy(id = id).toDomain()
    }

    override suspend fun updateJob(id: Long, input: JobUpsertInput): Job {
        val existing = jobDao.observeJob(id).first()
        val createdAt = existing?.createdAt ?: System.currentTimeMillis()
        val entity = input.toEntity(id = id, createdAt = createdAt, updatedAt = System.currentTimeMillis())
        jobDao.update(entity)
        return entity.toDomain()
    }

    override suspend fun deleteJob(id: Long) {
        val photoPaths = photoDao.filePathsForJob(id)
        jobDao.delete(id)
        photoPaths.forEach { photoStorage.deleteFile(it) }
    }

    override suspend fun addNote(jobId: Long, body: String, photoPaths: List<String>): JobNote {
        val now = System.currentTimeMillis()
        val noteId = jobNoteDao.insert(JobNoteEntity(jobId = jobId, timestamp = now, body = body))
        val photos = photoPaths.map { path ->
            val photoId = photoDao.insert(PhotoEntity(noteId = noteId, filePath = path, createdAt = now))
            Photo(id = photoId, noteId = noteId, filePath = path, createdAt = now)
        }
        return JobNote(id = noteId, jobId = jobId, timestamp = now, body = body, photos = photos)
    }

    override suspend fun updateNote(noteId: Long, body: String) {
        jobNoteDao.updateBody(noteId, body)
    }

    override suspend fun deleteNote(jobId: Long, noteId: Long) {
        val photoPaths = photoDao.filePathsForNote(noteId)
        jobNoteDao.delete(noteId)
        photoPaths.forEach { photoStorage.deleteFile(it) }
    }

    override suspend fun addPhotoToNote(noteId: Long, filePath: String) {
        photoDao.insert(PhotoEntity(noteId = noteId, filePath = filePath, createdAt = System.currentTimeMillis()))
    }

    override suspend fun deletePhoto(photoId: Long) {
        val photo = photoDao.getById(photoId) ?: return
        photoDao.delete(photoId)
        photoStorage.deleteFile(photo.filePath)
    }

    override fun observePaymentMethods(): Flow<List<PaymentMethod>> =
        paymentMethodDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun addPaymentMethod(name: String, maxCredit: Double?): PaymentMethod {
        val entity = PaymentMethodEntity(name = name, maxCredit = maxCredit, createdAt = System.currentTimeMillis())
        val id = paymentMethodDao.insert(entity)
        return entity.copy(id = id).toDomain()
    }

    override suspend fun updatePaymentMethod(id: Long, name: String, maxCredit: Double?) {
        paymentMethodDao.update(id, name, maxCredit)
    }

    override suspend fun deletePaymentMethod(id: Long) {
        paymentMethodDao.delete(id)
    }

    private fun comparator(filter: JobFilter): Comparator<Job> {
        val base: Comparator<Job> = when (filter.sortBy) {
            JobSortField.CREATED_AT -> compareBy { it.createdAt }
            JobSortField.UPDATED_AT -> compareBy { it.updatedAt }
            JobSortField.SCHEDULED_DATE -> compareBy(nullsFirst()) { it.scheduledDate }
            JobSortField.COMPLETED_DATE -> compareBy(nullsFirst()) { it.completedDate }
            JobSortField.TITLE -> compareBy { it.title.lowercase() }
            JobSortField.COST_VARIANCE -> compareBy(nullsFirst()) { it.costVariance }
            JobSortField.TIME_VARIANCE -> compareBy(nullsFirst()) { it.timeVariance }
        }
        return if (filter.sortDir == SortDirection.DESC) base.reversed() else base
    }
}
