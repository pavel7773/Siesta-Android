package com.example.catering.data.remote.dto.user

data class AuthResponse(
    val userResponse: UserResponse,
    val token: String
)