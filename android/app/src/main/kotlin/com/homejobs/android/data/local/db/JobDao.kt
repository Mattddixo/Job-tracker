package com.homejobs.android.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
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

    @Insert
    suspend fun insert(job: JobEntity): Long

    @Update
    suspend fun update(job: JobEntity)

    // Targeted UPDATE rather than a full @Update — this fires from the cross-app link flow,
    // which only ever knows the two ids involved, not the rest of the job's current fields.
    @Query("UPDATE jobs SET linkedJobJarId = :linkedJobJarId WHERE id = :id")
    suspend fun setLinkedJobJarId(id: Long, linkedJobJarId: Long?)

    @Query("DELETE FROM jobs WHERE id = :id")
    suspend fun delete(id: Long)
}
