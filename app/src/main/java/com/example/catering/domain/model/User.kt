package com.example.catering.domain.model

data class User(
    val id: Int = 0,
    val name: String,
    val phone: String,
    val address: String = "",
)
