package com.homejobs.android.domain.model

/**
 * A payment option a job can be tied to (a card, cash, a bank account, ...). [maxCredit] is the
 * only thing that distinguishes a card from anything else — there's no separate "is this a
 * card" flag, since presence of a credit limit already says so.
 */
data class PaymentMethod(
    val id: Long,
    val name: String,
    val maxCredit: Double?,
)
