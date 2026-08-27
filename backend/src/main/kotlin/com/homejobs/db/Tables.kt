package com.homejobs.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp

object JobsTable : Table("jobs") {
    val id = long("id").autoIncrement()
    val title = text("title")
    val category = text("category").nullable()
    val location = text("location").nullable()
    val vendorName = text("vendor_name").nullable()
    val vendorContact = text("vendor_contact").nullable()
    val status = text("status")
    val quotedCost = decimal("quoted_cost", 12, 2).nullable()
    val actualCost = decimal("actual_cost", 12, 2).nullable()
    val predictedHours = decimal("predicted_hours", 8, 2).nullable()
    val actualHours = decimal("actual_hours", 8, 2).nullable()
    val scheduledDate = date("scheduled_date").nullable()
    val completedDate = date("completed_date").nullable()
    val warrantyExpiry = date("warranty_expiry").nullable()
    val paymentStatus = text("payment_status")
    val paymentMethod = text("payment_method").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}

object JobNotesTable : Table("job_notes") {
    val id = long("id").autoIncrement()
    val jobId = long("job_id").references(JobsTable.id)
    val timestamp = timestamp("timestamp")
    val body = text("body")

    override val primaryKey = PrimaryKey(id)
}

object AttachmentsTable : Table("attachments") {
    val id = long("id").autoIncrement()
    val jobId = long("job_id").references(JobsTable.id)
    val fileName = text("file_name")
    val label = text("label").nullable()
    val takenAt = timestamp("taken_at").nullable()
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}
