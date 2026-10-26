package com.example.catering.domain.usecase.cart

import com.example.catering.domain.repository.CartRepository
import javax.inject.Inject

class RemoveFromCartUseCase @Inject constructor(private val cartRepository: CartRepository) {

    suspend fun execute(dishId: Int) {
        cartRepository.removeCart(dishId = dishId)
    }
}