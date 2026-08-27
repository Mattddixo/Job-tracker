package com.homejobs.android.data.local.db

import androidx.room.Embedded
import androidx.room.Relation

data class NoteWithPhotos(
    @Embedded val note: JobNoteEntity,
    @Relation(parentColumn = "id", entityColumn = "noteId")
    val photos: List<PhotoEntity>,
)
