
package com.example.catering.presentation.checkout

import com.example.catering.core.network.NetworkMonitor
import com.example.catering.domain.model.User
import com.example.catering.domain.usecase.checkout.ApplyPromoCodeUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CheckoutViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): CheckoutViewModel {
        val networkMonitor = mockk<NetworkMonitor>()

        every {
            networkMonitor.isOnline
        } returns flowOf(true)

        return CheckoutViewModel(
            applyPromoCodeUseCase = ApplyPromoCodeUseCase(),
            networkMonitor = networkMonitor
        )
    }


    @Test
    fun `repeated autofill preserves edited recipient`() {
        val viewModel = createViewModel()

        val anna = User(
            id = 1,
            name = "Анна",
            phone = "+79991111111",
            address = "Адрес Анны"
        )

        viewModel.syncAccount(anna)

        viewModel.updateName("Получатель заказа")

        // Тот же аккаунт после обновления профиля.
        viewModel.syncAccount(
            anna.copy(address = "Новый адрес Анны")
        )

        assertEquals(
            "Получатель заказа",
            viewModel.name.value
        )
    }

    @Test
    fun `new account must not inherit previous recipient`() {
        val viewModel = createViewModel()

        viewModel.syncAccount(
            User(
                id = 1,
                name = "Анна",
                phone = "+79991111111",
                address = "Адрес Анны"
            )
        )

        viewModel.updateComment("Позвонить Анне")
        viewModel.updateDeliveryType("Доставка")
        viewModel.updatePromoCode("SALE20")
        viewModel.applyPromoCode(2000L)

        // Прямая смена пользователя A на B.
        viewModel.syncAccount(
            User(
                id = 2,
                name = "Борис",
                phone = "+79992222222",
                address = "Адрес Бориса"
            )
        )

        assertEquals("Борис", viewModel.name.value)
        assertEquals("+79992222222", viewModel.phone.value)
        assertEquals("Адрес Бориса", viewModel.address.value)
        assertEquals("", viewModel.comment.value)
        assertEquals("Самовывоз", viewModel.deliveryType.value)
        assertEquals("", viewModel.promoCode.value)
        assertEquals(0L, viewModel.discount.value)
        assertEquals(
            PromoCodeStatus.Idle,
            viewModel.promoCodeStatus.value
        )
    }

    @Test
    fun `logout clears private checkout data`() {
        val viewModel = createViewModel()

        viewModel.syncAccount(
            User(
                id = 1,
                name = "Анна",
                phone = "+79991111111",
                address = "Адрес Анны"
            )
        )

        viewModel.updateComment("Личный комментарий")
        viewModel.updateDeliveryType("Доставка")

        // Успешный logout: currentUser стал null.
        viewModel.syncAccount(null)

        assertEquals("", viewModel.name.value)
        assertEquals("", viewModel.phone.value)
        assertEquals("", viewModel.address.value)
        assertEquals("", viewModel.comment.value)
        assertEquals("Самовывоз", viewModel.deliveryType.value)
    }

    @Test
    fun `changing order amount clears applied promo discount`() {

        val viewModel = createViewModel()

        viewModel.updatePromoCode("SALE20")

        viewModel.applyPromoCode(
            orderAmount = 2000L
        )

        // SALE20 = 20% от 2000 ₽.
        assertEquals(
            400L,
            viewModel.discount.value
        )

        assertEquals(
            PromoCodeStatus.Applied(
                "Промокод успешно применен!"
            ),
            viewModel.promoCodeStatus.value
        )

        // Корзина изменилась после применения промокода.
        viewModel.onOrderAmountChanged(
            orderAmount = 1000L
        )

        // Старая скидка больше не действительна.
        assertEquals(
            0L,
            viewModel.discount.value
        )

        assertEquals(
            PromoCodeStatus.Idle,
            viewModel.promoCodeStatus.value
        )

        // Сам текст оставляем,
        // чтобы пользователь мог применить код повторно.
        assertEquals(
            "SALE20",
            viewModel.promoCode.value
        )
    }
}
