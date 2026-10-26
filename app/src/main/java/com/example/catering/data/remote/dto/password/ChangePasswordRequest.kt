package com.example.catering.data.remote.dto.password

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)
