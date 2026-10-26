package com.example.catering.domain.usecase.cart

import com.example.catering.domain.repository.CartRepository
import javax.inject.Inject

class AddToCartUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {

    suspend fun execute(
        dishId: Int,
        count: Int
    ) {

        if (count <= 0) {
            return
        }

        cartRepository.changeQuantity(
            dishId = dishId,
            delta = count
        )
    }
}
