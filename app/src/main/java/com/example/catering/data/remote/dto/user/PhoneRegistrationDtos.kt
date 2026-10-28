package com.example.catering.data.remote.dto.user

data class RegistrationPendingResponse(
    val phone: String,
    val codeSent: Boolean
)

data class VerifyPhoneRequest(
    val phone: String,
    val code: String
)

data class ResendPhoneCodeRequest(
    val phone: String
)