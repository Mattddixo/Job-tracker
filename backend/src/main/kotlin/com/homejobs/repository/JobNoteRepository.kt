package com.homejobs.repository

import com.homejobs.db.JobNotesTable
import com.homejobs.db.JobsTable
import com.homejobs.domain.JobNote
import com.homejobs.domain.JobNoteCreateRequest
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant

class JobNoteRepository {

    suspend fun jobExists(jobId: Long): Boolean = newSuspendedTransaction(Dispatchers.IO) {
        JobsTable.selectAll().where { JobsTable.id eq jobId }.limit(1).any()
    }

    suspend fun create(jobId: Long, req: JobNoteCreateRequest): JobNote = newSuspendedTransaction(Dispatchers.IO) {
        val id = JobNotesTable.insert {
            it[JobNotesTable.jobId] = jobId
            it[timestamp] = Instant.now()
            it[body] = req.body
        }[JobNotesTable.id]
        JobNotesTable.selectAll().where { JobNotesTable.id eq id }.single().toJobNote()
    }

    suspend fun listForJob(jobId: Long): List<JobNote> = newSuspendedTransaction(Dispatchers.IO) {
        JobNotesTable.selectAll()
            .where { JobNotesTable.jobId eq jobId }
            .orderBy(JobNotesTable.timestamp to SortOrder.DESC)
            .map { it.toJobNote() }
    }

    suspend fun delete(jobId: Long, noteId: Long): Boolean = newSuspendedTransaction(Dispatchers.IO) {
        JobNotesTable.deleteWhere {
            with(it) { (JobNotesTable.id eq noteId) and (JobNotesTable.jobId eq jobId) }
        } > 0
    }

    private fun ResultRow.toJobNote(): JobNote = JobNote(
        id = this[JobNotesTable.id],
        jobId = this[JobNotesTable.jobId],
        timestamp = this[JobNotesTable.timestamp].toString(),
        body = this[JobNotesTable.body],
    )
}
