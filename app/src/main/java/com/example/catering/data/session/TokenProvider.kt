package com.example.catering.data.session

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenProvider @Inject constructor() {
    private var token: String? = null

    fun setToken(newToken: String) {
        token = newToken
    }

    fun getToken(): String? {
        return token
    }

    fun clearToken() {
        token = null
    }
}