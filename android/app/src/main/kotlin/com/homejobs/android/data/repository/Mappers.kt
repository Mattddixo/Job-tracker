package com.homejobs.android.data.repository

import com.homejobs.android.data.local.db.JobEntity
import com.homejobs.android.data.local.db.JobNoteEntity
import com.homejobs.android.data.remote.dto.JobDto
import com.homejobs.android.data.remote.dto.JobNoteDto
import com.homejobs.android.data.remote.dto.JobNoteCreateRequestDto
import com.homejobs.android.data.remote.dto.JobUpsertRequestDto
import com.homejobs.android.domain.model.Job
import com.homejobs.android.domain.model.JobNote
import com.homejobs.android.domain.model.JobStatus
import com.homejobs.android.domain.model.JobUpsertInput
import com.homejobs.android.domain.model.PaymentStatus

fun JobDto.toEntity() = JobEntity(
    id = id,
    title = title,
    category = category,
    location = location,
    vendorName = vendorName,
    vendorContact = vendorContact,
    status = status,
    quotedCost = quotedCost,
    actualCost = actualCost,
    predictedHours = predictedHours,
    actualHours = actualHours,
    scheduledDate = scheduledDate,
    completedDate = completedDate,
    warrantyExpiry = warrantyExpiry,
    paymentStatus = paymentStatus,
    paymentMethod = paymentMethod,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

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
    paymentMethod = paymentMethod,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun JobUpsertInput.toRequestDto() = JobUpsertRequestDto(
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
    paymentMethod = paymentMethod,
)

fun JobNoteDto.toEntity() = JobNoteEntity(id = id, jobId = jobId, timestamp = timestamp, body = body)

fun JobNoteEntity.toDomain() = JobNote(id = id, jobId = jobId, timestamp = timestamp, body = body)

fun String.toNoteRequestDto() = JobNoteCreateRequestDto(body = this)
