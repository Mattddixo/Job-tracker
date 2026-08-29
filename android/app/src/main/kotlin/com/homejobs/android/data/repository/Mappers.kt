package com.homejobs.android.data.repository

import com.homejobs.android.data.local.db.JobEntity
import com.homejobs.android.data.local.db.NoteWithPhotos
import com.homejobs.android.data.local.db.PaymentMethodEntity
import com.homejobs.android.data.local.db.PhotoEntity
import com.homejobs.android.domain.model.Job
import com.homejobs.android.domain.model.JobNote
import com.homejobs.android.domain.model.JobStatus
import com.homejobs.android.domain.model.JobUpsertInput
import com.homejobs.android.domain.model.PaymentMethod
import com.homejobs.android.domain.model.PaymentStatus
import com.homejobs.android.domain.model.Photo

fun JobEntity.toDomain() = Job(
    id = id,
    title = title,
    category = category,
    location = location,
    vendorName = vendorName,
    vendorContact = vendorContact,
    status = JobStatus.valueOf(status),
    quotedCost = quotedCost,
    actualCost = actualCost,
    predictedHours = predictedHours,
    actualHours = actualHours,
    scheduledDate = scheduledDate,
    completedDate = completedDate,
    warrantyExpiry = warrantyExpiry,
    paymentStatus = PaymentStatus.valueOf(paymentStatus),
    paymentMethodId = paymentMethodId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    linkedJobJarId = linkedJobJarId,
)

fun JobUpsertInput.toEntity(id: Long = 0, createdAt: Long, updatedAt: Long) = JobEntity(
    id = id,
    title = title,
    category = category,
    location = location,
    vendorName = vendorName,
    vendorContact = vendorContact,
    status = status.name,
    quotedCost = quotedCost,
    actualCost = actualCost,
    predictedHours = predictedHours,
    actualHours = actualHours,
    scheduledDate = scheduledDate,
    completedDate = completedDate,
    warrantyExpiry = warrantyExpiry,
    paymentStatus = paymentStatus.name,
    paymentMethodId = paymentMethodId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    linkedJobJarId = linkedJobJarId,
)

fun PaymentMethodEntity.toDomain() = PaymentMethod(id = id, name = name, maxCredit = maxCredit)

fun PhotoEntity.toDomain() = Photo(id = id, noteId = noteId, filePath = filePath, createdAt = createdAt)

fun NoteWithPhotos.toDomain() = JobNote(
    id = note.id,
    jobId = note.jobId,
    timestamp = note.timestamp,
    body = note.body,
    photos = photos.map { it.toDomain() },
)
