package com.example.catering.presentation.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catering.core.network.NetworkMonitor
import com.example.catering.domain.model.User
import com.example.catering.domain.usecase.checkout.ApplyPromoCodeResult
import com.example.catering.domain.usecase.checkout.ApplyPromoCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface PromoCodeStatus {
    data object Idle : PromoCodeStatus
    data class Applied(val message: String) : PromoCodeStatus
    data class Error(val message: String) : PromoCodeStatus
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val applyPromoCodeUseCase: ApplyPromoCodeUseCase,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _phone = MutableStateFlow("")
    val phone = _phone.asStateFlow()

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _deliveryType = MutableStateFlow("Самовывоз")
    val deliveryType = _deliveryType.asStateFlow()

    private val _address = MutableStateFlow("")
    val address = _address.asStateFlow()

    private val _comment = MutableStateFlow("")
    val comment = _comment.asStateFlow()

    private val _promoCode = MutableStateFlow("")
    val promoCode = _promoCode.asStateFlow()

    private val _discount = MutableStateFlow(0L)
    val discount = _discount.asStateFlow()

    private val _promoCodeStatus =
        MutableStateFlow<PromoCodeStatus>(PromoCodeStatus.Idle)

    val promoCodeStatus =
        _promoCodeStatus.asStateFlow()

    // Запоминаем сумму корзины, для которой
    // промокод был успешно применён.
    private var promoAppliedForOrderAmount: Long? = null

    private var activeAccountId: Int? = null

    val isOnline: StateFlow<Boolean> =
        networkMonitor.isOnline.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false
        )

    val canConfirmOrder: StateFlow<Boolean> =
        combine(
            phone,
            name,
            deliveryType,
            address
        ) { ph, nam, delivery, add ->

            val isPhoneValid =
                ph.trim().length >= 10

            val isNameValid =
                nam.trim().isNotBlank()

            val isDeliveryValid =
                delivery == "Самовывоз" ||
                        (
                                delivery == "Доставка" &&
                                        add.trim().isNotBlank()
                                )

            isPhoneValid &&
                    isNameValid &&
                    isDeliveryValid

        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false
        )

    fun updatePhone(newPhoneInput: String) {

        val cleanedPhone =
            newPhoneInput
                .replace(" ", "")
                .replace("-", "")
                .replace("(", "")
                .replace(")", "")

        val hasValidLength =
            cleanedPhone.length < 16

        val hasValidChars =
            cleanedPhone.all { char ->
                char.isDigit() || char == '+'
            }

        val hasOnlyOnePlus =
            cleanedPhone.count { it == '+' } <= 1

        val plusAtStart =
            !cleanedPhone.contains('+') ||
                    cleanedPhone.startsWith('+')

        if (
            hasValidLength &&
            hasValidChars &&
            hasOnlyOnePlus &&
            plusAtStart
        ) {
            _phone.value = cleanedPhone
        }
    }

    fun fillFromUser(
        userName: String,
        userPhone: String,
        userAddress: String
    ) {

        if (_name.value.isBlank()) {
            _name.value = userName
        }

        if (_phone.value.isBlank()) {
            updatePhone(userPhone)
        }

        if (_address.value.isBlank()) {
            _address.value = userAddress
        }
    }

    fun syncAccount(user: User?) {

        val newAccountId =
            user?.id

        // Данные предыдущего аккаунта нельзя
        // показывать следующему пользователю.
        if (
            activeAccountId != null &&
            activeAccountId != newAccountId
        ) {
            clearAccountDraft()
        }

        activeAccountId =
            newAccountId

        // Для того же аккаунта сохраняем ручной ввод.
        // Для нового — заполняем очищенную форму.
        if (user != null) {
            fillFromUser(
                userName = user.name,
                userPhone = user.phone,
                userAddress = user.address
            )
        }
    }

    private fun clearAccountDraft() {

        _name.value = ""
        _phone.value = ""
        _address.value = ""
        _deliveryType.value = "Самовывоз"
        _comment.value = ""

        _promoCode.value = ""
        _discount.value = 0L
        _promoCodeStatus.value =
            PromoCodeStatus.Idle

        promoAppliedForOrderAmount = null
    }

    fun updateName(newName: String) {
        _name.value = newName
    }

    fun updateAddress(newAddress: String) {
        _address.value = newAddress
    }

    fun updateDeliveryType(
        newDeliveryType: String
    ) {
        _deliveryType.value =
            newDeliveryType
    }

    fun updateComment(newComment: String) {

        if (newComment.length <= 150) {
            _comment.value = newComment
        }
    }

    fun updatePromoCode(text: String) {

        _promoCode.value = text

        // Пользователь изменил промокод —
        // предыдущая скидка больше не актуальна.
        _discount.value = 0L

        _promoCodeStatus.value =
            PromoCodeStatus.Idle

        promoAppliedForOrderAmount = null
    }

    fun applyPromoCode(orderAmount: Long) {

        when (
            val result =
                applyPromoCodeUseCase.execute(
                    code = _promoCode.value,
                    orderAmount = orderAmount
                )
        ) {

            is ApplyPromoCodeResult.Success -> {

                _discount.value =
                    result.discount

                _promoCodeStatus.value =
                    PromoCodeStatus.Applied(
                        result.message
                    )

                // Запоминаем сумму корзины,
                // для которой скидка была рассчитана.
                promoAppliedForOrderAmount =
                    orderAmount
            }

            is ApplyPromoCodeResult.Error -> {

                _discount.value = 0L

                _promoCodeStatus.value =
                    PromoCodeStatus.Error(
                        result.message
                    )

                promoAppliedForOrderAmount = null
            }
        }
    }

    fun onOrderAmountChanged(
        orderAmount: Long
    ) {

        val appliedForAmount =
            promoAppliedForOrderAmount
                ?: return

        if (appliedForAmount != orderAmount) {

            // Корзина изменилась после применения
            // промокода, поэтому старая скидка
            // больше не считается действующей.
            _discount.value = 0L

            _promoCodeStatus.value =
                PromoCodeStatus.Idle

            promoAppliedForOrderAmount = null
        }
    }

    fun resetAfterOrder() {

        _comment.value = ""

        _promoCode.value = ""

        _promoCodeStatus.value =
            PromoCodeStatus.Idle

        _discount.value = 0L

        promoAppliedForOrderAmount = null
    }
}