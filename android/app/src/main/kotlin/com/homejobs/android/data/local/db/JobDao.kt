package com.homejobs.android.data.local.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Query(
        """
        SELECT * FROM jobs
        WHERE (:status IS NULL OR status = :status)
          AND (:category IS NULL OR category = :category)
          AND (:location IS NULL OR location = :location)
        """,
    )
    fun observeJobs(status: String?, category: String?, location: String?): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE id = :id")
    fun observeJob(id: Long): Flow<JobEntity?>

    @Upsert
    suspend fun upsert(job: JobEntity)

    @Upsert
    suspend fun upsertAll(jobs: List<JobEntity>)

    @Query("DELETE FROM jobs WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM jobs")
    suspend fun clearAll()

    @Query("DELETE FROM jobs WHERE id NOT IN (:keepIds)")
    suspend fun deleteNotIn(keepIds: List<Long>)

    /**
     * Full refresh: brings the cache in line with the server's current job list, including
     * deletions. Uses [deleteNotIn] rather than [clearAll] + insert so jobs that still exist
     * keep their row identity, which means their cached notes (FK cascade-delete) are only
     * dropped for jobs that were actually removed server-side.
     */
    @Transaction
    suspend fun replaceAll(jobs: List<JobEntity>) {
        deleteNotIn(jobs.map { it.id })
        upsertAll(jobs)
    }
}
