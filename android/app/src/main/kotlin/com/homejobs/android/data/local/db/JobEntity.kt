package com.homejobs.android.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jobs")
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
    val paymentMethod: String?,
    val createdAt: Long,
    val updatedAt: Long,
)
