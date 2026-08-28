package com.homejobs.android.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentMethodDao {
    @Query("SELECT * FROM payment_methods ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<PaymentMethodEntity>>

    @Insert
    suspend fun insert(method: PaymentMethodEntity): Long

    @Query("UPDATE payment_methods SET name = :name, maxCredit = :maxCredit WHERE id = :id")
    suspend fun update(id: Long, name: String, maxCredit: Double?)

    @Query("DELETE FROM payment_methods WHERE id = :id")
    suspend fun delete(id: Long)
}
