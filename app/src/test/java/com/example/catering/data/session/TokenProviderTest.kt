package com.example.catering.data.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class TokenProviderTest {

    private lateinit var tokenProvider: TokenProvider

    @Before
    fun setup() {
        // Здесь создаём настоящий TokenProvider.
        // Mock не нужен, потому что именно его поведение мы тестируем.
        tokenProvider = TokenProvider()
    }

    @Test
    fun `new token provider has no token`() {

        val result = tokenProvider.getToken()

        assertNull(result)
    }

    @Test
    fun `set token stores token`() {

        tokenProvider.setToken(
            "token-123"
        )

        assertEquals(
            "token-123",
            tokenProvider.getToken()
        )
    }

    @Test
    fun `setting new token replaces previous token`() {

        tokenProvider.setToken(
            "old-token"
        )

        tokenProvider.setToken(
            "new-token"
        )

        assertEquals(
            "new-token",
            tokenProvider.getToken()
        )
    }

    @Test
    fun `clear token removes current token`() {

        tokenProvider.setToken(
            "token-123"
        )

        tokenProvider.clearToken()

        assertNull(
            tokenProvider.getToken()
        )
    }
}