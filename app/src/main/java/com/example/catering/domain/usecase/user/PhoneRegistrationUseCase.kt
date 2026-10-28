package com.example.catering.domain.usecase.user

import com.example.catering.domain.model.User
import com.example.catering.domain.model.UserException
import com.example.catering.domain.repository.UserRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

sealed interface VerifyPhoneResult {

    data class Success(
        val user: User
    ) : VerifyPhoneResult

    data class Error(
        val message: String
    ) : VerifyPhoneResult
}

sealed interface ResendPhoneCodeResult {

    data object Success :
        ResendPhoneCodeResult

    data class Error(
        val message: String
    ) : ResendPhoneCodeResult
}

class PhoneRegistrationUseCase @Inject constructor(
    private val userRepository: UserRepository
) {

    suspend fun verify(
        phone: String,
        code: String
    ): VerifyPhoneResult {

        if (!code.matches(Regex("""\d{6}"""))) {
            return VerifyPhoneResult.Error(
                "Введите шестизначный код"
            )
        }

        return try {

            val user =
                userRepository
                    .verifyRegistrationPhone(
                        phone = phone,
                        code = code
                    )

            userRepository.saveUser(user)

            VerifyPhoneResult.Success(user)

        } catch (e: CancellationException) {
            throw e

        } catch (e: Exception) {

            VerifyPhoneResult.Error(
                (e as? UserException)?.message
                    ?: "Не удалось подтвердить телефон"
            )
        }
    }

    suspend fun resend(
        phone: String
    ): ResendPhoneCodeResult {

        return try {

            userRepository
                .resendRegistrationPhoneCode(
                    phone
                )

            ResendPhoneCodeResult.Success

        } catch (e: CancellationException) {
            throw e

        } catch (e: Exception) {

            ResendPhoneCodeResult.Error(
                (e as? UserException)?.message
                    ?: "Не удалось отправить код"
            )
        }
    }
}