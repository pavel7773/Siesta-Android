package com.example.catering.data.remote.dto.user

data class RegisterRequest(
    val name: String,
    val phone: String,
    val password: String
)