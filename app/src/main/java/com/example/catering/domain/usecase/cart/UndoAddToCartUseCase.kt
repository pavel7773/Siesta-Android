package com.example.catering.domain.usecase.cart

import com.example.catering.domain.repository.CartRepository
import javax.inject.Inject

class UndoAddToCartUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {

    suspend fun execute(
        dishId: Int
    ) {
        cartRepository.changeQuantity(
            dishId = dishId,
            delta = -1
        )
    }
}