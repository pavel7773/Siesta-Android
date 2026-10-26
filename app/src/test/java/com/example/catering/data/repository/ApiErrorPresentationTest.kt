package com.example.catering.data.repository

import com.example.catering.data.local.order.OrderLocalDataSource
import com.example.catering.data.local.token.TokenLocalDataSource
import com.example.catering.data.local.user.UserLocalDataSource
import com.example.catering.data.mapper.UserMapper
import com.example.catering.data.remote.order.OrderRemoteDataSource
import com.example.catering.data.remote.user.UserRemoteDataSource
import com.example.catering.data.repository.order.OrderRoomRepository
import com.example.catering.data.repository.user.UserRepositoryImpl
import com.example.catering.data.session.TokenProvider
import com.example.catering.domain.model.*
import com.example.catering.domain.usecase.order.CreateOrderResult
import com.example.catering.domain.usecase.order.CreateOrderUseCase
import com.example.catering.domain.usecase.user.*
import io.mockk.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.test.*

/** Exercises real Repository -> UseCase error paths; no HTTP requests or real orders. */
class ApiErrorPresentationTest {
    private val remote = mockk<UserRemoteDataSource>()
    private val local = mockk<UserLocalDataSource>()
    private val tokens = mockk<TokenLocalDataSource>()
    private val tokenProvider = mockk<TokenProvider>()
    private val users = UserRepositoryImpl(remote, mockk<UserMapper>(), local, tokens, tokenProvider)
    private val orderRemote = mockk<OrderRemoteDataSource>()
    private val orderLocal = mockk<OrderLocalDataSource>()
    private val orders = OrderRoomRepository(orderLocal, orderRemote)

    private fun http(code: Int, body: String = "<html>internal diagnostic</html>") =
        HttpException(Response.error<Any>(code, body.toResponseBody()))

    private suspend fun loginMessage() = assertIs<LoginUserResult.Error>(
        LoginUserUseCase(users).execute("79991234567", "test-password")
    ).message

    private suspend fun registrationMessage() = assertIs<RegisterUserResult.Error>(
        RegisterUserUseCase(users).execute("Тест", "79991234567", "test-password", "test-password")
    ).message

    @Test fun `login 401 describes credentials without changing saved session`() = runTest {
        coEvery { remote.loginUser(any()) } throws http(401)
        assertEquals("Неверный телефон или пароль", loginMessage())
        coVerify(exactly = 0) { tokens.saveToken(any()); local.saveUser(any()) }
        verify { tokenProvider wasNot Called }
    }

    @Test fun `login 400 does not claim that the password is wrong`() = runTest {
        coEvery { remote.loginUser(any()) } throws http(400)
        assertEquals("Проверьте номер телефона и пароль", loginMessage())
    }

    @Test fun `registration conflict offers login or password recovery`() = runTest {
        coEvery { remote.registerUser(any()) } throws http(409)
        assertEquals("Этот номер уже зарегистрирован. Войдите или восстановите пароль", registrationMessage())
    }

    @Test fun `registration preserves known field validation`() = runTest {
        coEvery { remote.registerUser(any()) } throws http(400, "Имя должно содержать от 3 до 100 символов")
        assertEquals("Имя должно содержать от 3 до 100 символов", registrationMessage())
    }

    @Test fun `unknown html json sql and oversized response bodies are not UI text`() = runTest {
        for (body in listOf("<html>HTTP 400</html>", "{\"error\":\"SQL secret\"}",
            "SQLException: internal table", "x".repeat(2000))) {
            coEvery { remote.registerUser(any()) } throws http(400, body)
            assertEquals("Проверьте имя, номер телефона и пароль", registrationMessage())
        }
    }

    @Test fun `server failure and rate limit give actionable Russian messages`() = runTest {
        for (code in listOf(500, 502, 503)) {
            coEvery { remote.loginUser(any()) } throws http(code)
            assertEquals("Сервис временно недоступен. Попробуйте позже", loginMessage())
        }
        coEvery { remote.loginUser(any()) } throws http(429)
        assertEquals("Слишком много попыток. Подождите немного и попробуйте снова", loginMessage())
        coEvery { remote.loginUser(any()) } throws http(403)
        assertEquals("Это действие недоступно для вашего аккаунта", loginMessage())
    }

    @Test fun `connection errors never expose hostnames or socket diagnostics`() = runTest {
        for (error in listOf(IOException("Unable to resolve host internal"), SocketTimeoutException("socket details"))) {
            coEvery { remote.loginUser(any()) } throws error
            assertEquals("Не удалось подключиться к серверу", loginMessage())
        }
        coEvery { remote.loginUser(any()) } throws http(504)
        assertEquals("Сервер не ответил вовремя. Попробуйте ещё раз", loginMessage())
    }

    @Test fun `unknown parsing exception is not exposed`() = runTest {
        coEvery { remote.loginUser(any()) } throws IllegalArgumentException("Expected BEGIN_OBJECT at token")
        assertEquals("Не удалось выполнить действие. Попробуйте позже", loginMessage())
    }

    @Test fun `profile 401 is session expiry and does not save local data`() = runTest {
        coEvery { remote.updateProfile(any()) } throws http(401)
        val result = UpdateUserNameUseCase(users).execute(User(1, "Тест", "79991234567", ""), "Имя")
        assertEquals("Сессия истекла. Войдите снова", assertIs<UpdateUserNameResult.Error>(result).message)
        coVerify(exactly = 0) { local.saveUser(any()) }
    }

    @Test fun `profile validation reaches use case without technical status`() = runTest {
        coEvery { remote.updateProfile(any()) } throws http(400, "Адрес не должен превышать 255 символов")
        val result = UpdateUserAddressUseCase(users).execute(User(1, "Тест", "79991234567", ""), "адрес")
        assertEquals("Адрес не должен превышать 255 символов", assertIs<UpdateUserAddressResult.Error>(result).message)
    }

    private fun order() = Order(
        id = 0, name = "Тест", phone = "79991234567", comment = "", deliveryType = "Самовывоз", address = "",
        products = listOf(CartItem(Dish(1, "Тест", 500, "", "", 100, 100, 1.0, 1.0, 1.0, "Тест"), 1)),
        totalAmount = 500
    )

    private suspend fun orderMessage() = assertIs<CreateOrderResult.Error>(
        CreateOrderUseCase(orders).execute(order(), "test-idempotency-key", "TEST")
    ).message

    @Test fun `order promo validation is readable and preserves request key`() = runTest {
        coEvery { orderRemote.createOrder(any(), any()) } throws http(400, "Промокод больше не активен")
        assertEquals("Промокод больше не активен", orderMessage())
        coVerify(exactly = 1) { orderRemote.createOrder(match { it.promoCode == "TEST" }, "test-idempotency-key") }
        coVerify(exactly = 0) { orderLocal.saveOrder(any()) }
    }

    @Test fun `unknown order errors and failure after submit do not expose internals`() = runTest {
        coEvery { orderRemote.createOrder(any(), any()) } throws http(400, "Некорректный Idempotency-Key")
        assertEquals("Проверьте состав корзины, данные доставки и промокод", orderMessage())
        coEvery { orderRemote.createOrder(any(), any()) } throws http(500)
        assertEquals("Сервис временно недоступен. Проверьте историю заказов перед повторной попыткой", orderMessage())
    }

    @Test fun `cancel conflict accepts only known user messages`() = runTest {
        coEvery { orderRemote.cancelOrder(any()) } throws http(409, "Заказ уже нельзя отменить")
        assertEquals("Заказ уже нельзя отменить", assertFailsWith<OrderException.Conflict> { orders.cancelOrder(1) }.message)
        coEvery { orderRemote.cancelOrder(any()) } throws http(409, "<html>database internal error</html>")
        assertEquals("Действие с заказом сейчас невозможно. Обновите историю заказов",
            assertFailsWith<OrderException.Conflict> { orders.cancelOrder(1) }.message)
        coVerify(exactly = 0) { orderLocal.updateOrderStatus(any(), any()) }
    }

    @Test fun `cancellation is rethrown unchanged through both repository paths`() = runTest {
        val cancelled = CancellationException("cancelled")
        coEvery { remote.loginUser(any()) } throws cancelled
        assertSame(cancelled, assertFailsWith<CancellationException> { loginMessage() })
        coEvery { orderRemote.createOrder(any(), any()) } throws cancelled
        assertSame(cancelled, assertFailsWith<CancellationException> { orderMessage() })
    }
}
