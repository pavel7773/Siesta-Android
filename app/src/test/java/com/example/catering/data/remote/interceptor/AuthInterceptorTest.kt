package com.example.catering.data.remote.interceptor

import com.example.catering.data.session.TokenProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {

    private lateinit var tokenProvider: TokenProvider
    private lateinit var chain: Interceptor.Chain
    private lateinit var response: Response

    private lateinit var interceptor: AuthInterceptor

    @Before
    fun setup() {

        tokenProvider = mockk()
        chain = mockk()
        response = mockk()

        interceptor = AuthInterceptor(
            tokenProvider = tokenProvider
        )
    }

    @Test
    fun `token exists adds bearer authorization header`() {

        val originalRequest = Request.Builder()
            .url("https://example.com/orders")
            .get()
            .build()

        val requestSlot = slot<Request>()

        every {
            tokenProvider.getToken()
        } returns "test-token-123"

        every {
            chain.request()
        } returns originalRequest

        every {
            chain.proceed(
                capture(requestSlot)
            )
        } returns response

        val result =
            interceptor.intercept(chain)

        val sentRequest =
            requestSlot.captured

        assertEquals(
            "Bearer test-token-123",
            sentRequest.header("Authorization")
        )

        // Interceptor должен добавить auth header,
        // но не менять сам адрес и HTTP method запроса.
        assertEquals(
            originalRequest.url,
            sentRequest.url
        )

        assertEquals(
            originalRequest.method,
            sentRequest.method
        )

        // intercept() должен вернуть тот Response,
        // который пришёл от следующего элемента OkHttp chain.
        assertSame(
            response,
            result
        )

        verify(exactly = 1) {
            tokenProvider.getToken()
        }

        verify(exactly = 1) {
            chain.proceed(any())
        }
    }

    @Test
    fun `missing token sends request without authorization header`() {

        val originalRequest = Request.Builder()
            .url("https://example.com/menu")
            .get()
            .build()

        val requestSlot = slot<Request>()

        every {
            tokenProvider.getToken()
        } returns null

        every {
            chain.request()
        } returns originalRequest

        every {
            chain.proceed(
                capture(requestSlot)
            )
        } returns response

        val result =
            interceptor.intercept(chain)

        val sentRequest =
            requestSlot.captured

        assertNull(
            sentRequest.header("Authorization")
        )

        assertEquals(
            originalRequest.url,
            sentRequest.url
        )

        assertEquals(
            originalRequest.method,
            sentRequest.method
        )

        assertSame(
            response,
            result
        )

        verify(exactly = 1) {
            tokenProvider.getToken()
        }

        verify(exactly = 1) {
            chain.proceed(any())
        }
    }
    @Test
    fun `blank token sends request without authorization header`() {

        val originalRequest = Request.Builder()
            .url("https://example.com/profile")
            .get()
            .build()

        val requestSlot = slot<Request>()

        every {
            tokenProvider.getToken()
        } returns "   "

        every {
            chain.request()
        } returns originalRequest

        every {
            chain.proceed(
                capture(requestSlot)
            )
        } returns response

        val result =
            interceptor.intercept(chain)

        val sentRequest =
            requestSlot.captured

        assertNull(
            sentRequest.header("Authorization")
        )

        assertEquals(
            originalRequest.url,
            sentRequest.url
        )

        assertEquals(
            originalRequest.method,
            sentRequest.method
        )

        assertSame(
            response,
            result
        )

        verify(exactly = 1) {
            tokenProvider.getToken()
        }

        verify(exactly = 1) {
            chain.proceed(any())
        }
    }
}