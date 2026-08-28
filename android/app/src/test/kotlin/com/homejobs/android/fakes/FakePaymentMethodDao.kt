package com.homejobs.android.fakes

import com.homejobs.android.data.local.db.PaymentMethodDao
import com.homejobs.android.data.local.db.PaymentMethodEntity
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.atomic.AtomicLong

class FakePaymentMethodDao : PaymentMethodDao {
    val methodsState = MutableStateFlow<List<PaymentMethodEntity>>(emptyList())
    private val idSeq = AtomicLong(1)

    override fun observeAll() = methodsState

    override suspend fun insert(method: PaymentMethodEntity): Long {
        val id = idSeq.getAndIncrement()
        methodsState.value = methodsState.value + method.copy(id = id)
        return id
    }

    override suspend fun update(id: Long, name: String, maxCredit: Double?) {
        methodsState.value = methodsState.value.map {
            if (it.id == id) it.copy(name = name, maxCredit = maxCredit) else it
        }
    }

    override suspend fun delete(id: Long) {
        methodsState.value = methodsState.value.filterNot { it.id == id }
    }
}
