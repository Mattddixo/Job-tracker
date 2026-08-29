package com.homejobs.android.data.local.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "jobs",
    foreignKeys = [
        ForeignKey(
            entity = PaymentMethodEntity::class,
            parentColumns = ["id"],
            childColumns = ["paymentMethodId"],
            // Deleting a payment method must never delete a job — it just falls back to
            // "Unassigned" (see StatsScreen), unlike the CASCADE used for a job's own children
            // (notes/photos), which really should disappear with the job.
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("paymentMethodId")],
)
data class JobEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String?,
    val location: String?,
    val vendorName: String?,
    val vendorContact: String?,
    val status: String,
    val quotedCost: Double?,
    val actualCost: Double?,
    val predictedHours: Double?,
    val actualHours: Double?,
    val scheduledDate: String?,
    val completedDate: String?,
    val warrantyExpiry: String?,
    val paymentStatus: String,
    val paymentMethodId: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    // Opaque id of the Job Jar task this job is linked to, however the link was established
    // (this job was sent there, that task was sent here, or the two were linked directly via
    // the picker). No @ForeignKey — it points into a different app's database entirely, not a
    // table in this one.
    val linkedJobJarId: Long? = null,
)
