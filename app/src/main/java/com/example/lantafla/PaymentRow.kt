package com.example.lantafla

data class PaymentRow(
    val month: Int,
    val payment: Long,
    val interest: Long,
    val principal: Long,
    val inflation: Long,    // verðbætur; 0 for non-indexed loans
    val balance: Long
)
