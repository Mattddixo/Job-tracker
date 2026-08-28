package com.homejobs.android.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [JobEntity::class, JobNoteEntity::class, PhotoEntity::class, PaymentMethodEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun jobDao(): JobDao
    abstract fun jobNoteDao(): JobNoteDao
    abstract fun photoDao(): PhotoDao
    abstract fun paymentMethodDao(): PaymentMethodDao
}
