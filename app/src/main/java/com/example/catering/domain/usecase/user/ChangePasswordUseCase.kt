
package com.example.catering.domain.usecase.user

import com.example.catering.domain.repository.UserRepository
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val userRepository: UserRepository
) {

    suspend fun execute(
        currentPassword: String,
        newPassword: String
    ) {
        userRepository.changePassword(
            currentPassword = currentPassword,
            newPassword = newPassword
        )
    }
}
