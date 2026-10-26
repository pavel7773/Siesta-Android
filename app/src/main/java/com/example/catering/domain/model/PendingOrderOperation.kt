package com.example.catering.domain.model

data class PendingOrderOperation(
    val userId: Int,
    val idempotencyKey: String,
    val orderFingerprint: String
)