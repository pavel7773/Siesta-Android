package com.example.catering.data.local.user

import kotlinx.serialization.Serializable

@Serializable
data class SavedUser(val id: Int, val name: String, val phone: String,val address: String)