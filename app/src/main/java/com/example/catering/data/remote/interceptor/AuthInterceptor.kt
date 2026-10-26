package com.example.catering.data.remote.interceptor

import com.example.catering.data.session.TokenProvider
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider
) : Interceptor {

    override fun intercept(
        chain: Interceptor.Chain
    ): Response {

        val originalRequest =
            chain.request()

        val token =
            tokenProvider.getToken()

        val requestBuilder =
            originalRequest.newBuilder()

        // Пустой или состоящий только из пробелов token
        // считаем отсутствующим.
        if (!token.isNullOrBlank()) {
            requestBuilder.header(
                "Authorization",
                "Bearer $token"
            )
        }

        val newRequest =
            requestBuilder.build()

        return chain.proceed(newRequest)
    }
}