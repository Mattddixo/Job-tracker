package com.homejobs.android.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PhotoDao {
    @Insert
    suspend fun insert(photo: PhotoEntity): Long

    @Query("SELECT * FROM photos WHERE id = :id")
    suspend fun getById(id: Long): PhotoEntity?

    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT filePath FROM photos WHERE noteId = :noteId")
    suspend fun filePathsForNote(noteId: Long): List<String>

    /** Every photo file under every note of a job — used to clean up files before deleting a job. */
    @Query(
        """
        SELECT p.filePath FROM photos p
        INNER JOIN job_notes n ON p.noteId = n.id
        WHERE n.jobId = :jobId
        """,
    )
    suspend fun filePathsForJob(jobId: Long): List<String>
}
