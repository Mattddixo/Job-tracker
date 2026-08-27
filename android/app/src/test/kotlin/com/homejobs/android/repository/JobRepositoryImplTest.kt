package com.homejobs.android.repository

import app.cash.turbine.test
import com.homejobs.android.data.local.photo.PhotoStorage
import com.homejobs.android.data.repository.JobRepositoryImpl
import com.homejobs.android.domain.model.JobFilter
import com.homejobs.android.domain.model.JobStatus
import com.homejobs.android.domain.model.JobUpsertInput
import com.homejobs.android.fakes.FakeJobDao
import com.homejobs.android.fakes.FakeJobNoteDao
import com.homejobs.android.fakes.FakePhotoDao
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class JobRepositoryImplTest {

    private lateinit var jobDao: FakeJobDao
    private lateinit var jobNoteDao: FakeJobNoteDao
    private lateinit var photoDao: FakePhotoDao
    private lateinit var photoStorage: PhotoStorage
    private lateinit var repository: JobRepositoryImpl

    @Before
    fun setUp() {
        jobDao = FakeJobDao()
        photoDao = FakePhotoDao()
        jobNoteDao = FakeJobNoteDao(photoDao)
        photoStorage = mockk(relaxed = true)
        repository = JobRepositoryImpl(jobDao, jobNoteDao, photoDao, photoStorage)
    }

    @Test
    fun `createJob assigns an id and caches it`() = runTest {
        val job = repository.createJob(JobUpsertInput(title = "New job", status = JobStatus.QUOTED))

        assertEquals("New job", job.title)
        repository.observeJob(job.id).test {
            assertEquals("New job", awaitItem()?.title)
        }
    }

    @Test
    fun `updateJob preserves the original createdAt`() = runTest {
        val created = repository.createJob(JobUpsertInput(title = "Original"))

        val updated = repository.updateJob(created.id, JobUpsertInput(title = "Renamed"))

        assertEquals(created.createdAt, updated.createdAt)
        assertEquals("Renamed", updated.title)
    }

    @Test
    fun `deleteJob removes the job and its photo files`() = runTest {
        val job = repository.createJob(JobUpsertInput(title = "To delete"))
        val note = repository.addNote(job.id, "note", listOf("/data/photo1.jpg"))

        repository.deleteJob(job.id)

        repository.observeJob(job.id).test {
            assertNull(awaitItem())
        }
        verify { photoStorage.deleteFile("/data/photo1.jpg") }
        assertTrue(note.photos.isNotEmpty()) // sanity: the note really had a photo to clean up
    }

    @Test
    fun `addNote and observeNotes round-trip including photos`() = runTest {
        val job = repository.createJob(JobUpsertInput(title = "Job with notes"))

        repository.addNote(job.id, "Contractor called", listOf("/data/before.jpg"))

        repository.observeNotes(job.id).test {
            val notes = awaitItem()
            assertEquals(1, notes.size)
            assertEquals("Contractor called", notes.single().body)
            assertEquals(listOf("/data/before.jpg"), notes.single().photos.map { it.filePath })
        }
    }

    @Test
    fun `deleteNote removes it and deletes its photo files`() = runTest {
        val job = repository.createJob(JobUpsertInput(title = "Job with notes"))
        val note = repository.addNote(job.id, "note", listOf("/data/photo1.jpg", "/data/photo2.jpg"))

        repository.deleteNote(job.id, note.id)

        repository.observeNotes(job.id).test {
            assertTrue(awaitItem().isEmpty())
        }
        verify { photoStorage.deleteFile("/data/photo1.jpg") }
        verify { photoStorage.deleteFile("/data/photo2.jpg") }
    }

    @Test
    fun `addPhotoToNote appends to an existing note`() = runTest {
        val job = repository.createJob(JobUpsertInput(title = "Job"))
        val note = repository.addNote(job.id, "note")

        repository.addPhotoToNote(note.id, "/data/added-later.jpg")

        repository.observeNotes(job.id).test {
            val photos = awaitItem().single().photos
            assertEquals(1, photos.size)
            assertEquals("/data/added-later.jpg", photos.single().filePath)
        }
    }

    @Test
    fun `deletePhoto removes it from the note and deletes the file`() = runTest {
        val job = repository.createJob(JobUpsertInput(title = "Job"))
        val note = repository.addNote(job.id, "note", listOf("/data/photo.jpg"))
        val photoId = note.photos.single().id

        repository.deletePhoto(photoId)

        repository.observeNotes(job.id).test {
            assertTrue(awaitItem().single().photos.isEmpty())
        }
        verify { photoStorage.deleteFile("/data/photo.jpg") }
    }

    @Test
    fun `observeJobs filters by status`() = runTest {
        repository.createJob(JobUpsertInput(title = "Fix roof", status = JobStatus.DONE))
        repository.createJob(JobUpsertInput(title = "Paint fence", status = JobStatus.QUOTED))

        repository.observeJobs(JobFilter(status = JobStatus.DONE)).test {
            val jobs = awaitItem()
            assertEquals(1, jobs.size)
            assertEquals("Fix roof", jobs.single().title)
        }
    }
}
