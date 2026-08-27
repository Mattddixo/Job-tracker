package com.homejobs.android.data.local.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** [filePath] points at a file this app copied into its own private storage — see PhotoStorage. */
@Entity(
    tableName = "photos",
    foreignKeys = [
        ForeignKey(
            entity = JobNoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("noteId")],
)
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val noteId: Long,
    val filePath: String,
    val createdAt: Long,
)
