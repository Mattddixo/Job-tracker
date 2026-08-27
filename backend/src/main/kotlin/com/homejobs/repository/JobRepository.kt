package com.homejobs.repository

import com.homejobs.db.JobsTable
import com.homejobs.domain.Job
import com.homejobs.domain.JobFilter
import com.homejobs.domain.JobSortField
import com.homejobs.domain.JobStatus
import com.homejobs.domain.JobUpsertRequest
import com.homejobs.domain.PaymentStatus
import com.homejobs.domain.SortDirection
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

class JobRepository {

    suspend fun create(req: JobUpsertRequest): Job = newSuspendedTransaction(Dispatchers.IO) {
        val now = Instant.now()
        val id = JobsTable.insert {
            it[title] = req.title
            it[category] = req.category
            it[location] = req.location
            it[vendorName] = req.vendorName
            it[vendorContact] = req.vendorContact
            it[status] = req.status.name.lowercase()
            it[quotedCost] = req.quotedCost?.toBigDecimal()
            it[actualCost] = req.actualCost?.toBigDecimal()
            it[predictedHours] = req.predictedHours?.toBigDecimal()
            it[actualHours] = req.actualHours?.toBigDecimal()
            it[scheduledDate] = req.scheduledDate?.let(LocalDate::parse)
            it[completedDate] = req.completedDate?.let(LocalDate::parse)
            it[warrantyExpiry] = req.warrantyExpiry?.let(LocalDate::parse)
            it[paymentStatus] = req.paymentStatus.name.lowercase()
            it[paymentMethod] = req.paymentMethod
            it[createdAt] = now
            it[updatedAt] = now
        }[JobsTable.id]
        JobsTable.selectAll().where { JobsTable.id eq id }.single().toJob()
    }

    suspend fun findById(id: Long): Job? = newSuspendedTransaction(Dispatchers.IO) {
        JobsTable.selectAll().where { JobsTable.id eq id }.singleOrNull()?.toJob()
    }

    suspend fun update(id: Long, req: JobUpsertRequest): Job? = newSuspendedTransaction(Dispatchers.IO) {
        val updated = JobsTable.update({ JobsTable.id eq id }) {
            it[title] = req.title
            it[category] = req.category
            it[location] = req.location
            it[vendorName] = req.vendorName
            it[vendorContact] = req.vendorContact
            it[status] = req.status.name.lowercase()
            it[quotedCost] = req.quotedCost?.toBigDecimal()
            it[actualCost] = req.actualCost?.toBigDecimal()
            it[predictedHours] = req.predictedHours?.toBigDecimal()
            it[actualHours] = req.actualHours?.toBigDecimal()
            it[scheduledDate] = req.scheduledDate?.let(LocalDate::parse)
            it[completedDate] = req.completedDate?.let(LocalDate::parse)
            it[warrantyExpiry] = req.warrantyExpiry?.let(LocalDate::parse)
            it[paymentStatus] = req.paymentStatus.name.lowercase()
            it[paymentMethod] = req.paymentMethod
            it[updatedAt] = Instant.now()
        }
        if (updated == 0) null else JobsTable.selectAll().where { JobsTable.id eq id }.single().toJob()
    }

    suspend fun delete(id: Long): Boolean = newSuspendedTransaction(Dispatchers.IO) {
        JobsTable.deleteWhere { with(it) { JobsTable.id eq id } } > 0
    }

    suspend fun list(filter: JobFilter): List<Job> = newSuspendedTransaction(Dispatchers.IO) {
        var query = JobsTable.selectAll()

        val conditions = with(SqlExpressionBuilder) {
            buildList {
                filter.status?.let { add(JobsTable.status eq it.name.lowercase()) }
                filter.category?.let { add(JobsTable.category eq it) }
                filter.location?.let { add(JobsTable.location eq it) }
            }
        }
        if (conditions.isNotEmpty()) {
            query = query.where { conditions.reduce { a, b -> a and b } }
        }

        val sortOrder = if (filter.sortDir == SortDirection.ASC) SortOrder.ASC else SortOrder.DESC
        val sortExpression: Expression<*> = with(SqlExpressionBuilder) {
            when (filter.sortBy) {
                JobSortField.CREATED_AT -> JobsTable.createdAt
                JobSortField.UPDATED_AT -> JobsTable.updatedAt
                JobSortField.SCHEDULED_DATE -> JobsTable.scheduledDate
                JobSortField.COMPLETED_DATE -> JobsTable.completedDate
                JobSortField.TITLE -> JobsTable.title
                JobSortField.COST_VARIANCE -> JobsTable.actualCost.minus(JobsTable.quotedCost)
                JobSortField.TIME_VARIANCE -> JobsTable.actualHours.minus(JobsTable.predictedHours)
            }
        }

        query.orderBy(sortExpression to sortOrder).toList().map { it.toJob() }
    }

    private fun ResultRow.toJob(): Job = Job(
        id = this[JobsTable.id],
        title = this[JobsTable.title],
        category = this[JobsTable.category],
        location = this[JobsTable.location],
        vendorName = this[JobsTable.vendorName],
        vendorContact = this[JobsTable.vendorContact],
        status = JobStatus.valueOf(this[JobsTable.status].uppercase()),
        quotedCost = this[JobsTable.quotedCost]?.toDouble(),
        actualCost = this[JobsTable.actualCost]?.toDouble(),
        predictedHours = this[JobsTable.predictedHours]?.toDouble(),
        actualHours = this[JobsTable.actualHours]?.toDouble(),
        scheduledDate = this[JobsTable.scheduledDate]?.toString(),
        completedDate = this[JobsTable.completedDate]?.toString(),
        warrantyExpiry = this[JobsTable.warrantyExpiry]?.toString(),
        paymentStatus = PaymentStatus.valueOf(this[JobsTable.paymentStatus].uppercase()),
        paymentMethod = this[JobsTable.paymentMethod],
        createdAt = this[JobsTable.createdAt].toString(),
        updatedAt = this[JobsTable.updatedAt].toString(),
    )
}

private fun Double.toBigDecimal(): BigDecimal = BigDecimal.valueOf(this)
