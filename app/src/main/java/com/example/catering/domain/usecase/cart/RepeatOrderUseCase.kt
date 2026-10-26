package com.example.catering.domain.usecase.cart

import com.example.catering.domain.model.CartEntry
import com.example.catering.domain.model.Order
import com.example.catering.domain.repository.CartRepository
import com.example.catering.domain.usecase.dish.GetDishesByIdsUseCase
import javax.inject.Inject

sealed interface RepeatOrderResult {
    data object Success : RepeatOrderResult
    data object EmptyOrder : RepeatOrderResult
    data object PartialSuccess : RepeatOrderResult
    data object NoAvailableDishes : RepeatOrderResult
}

class RepeatOrderUseCase @Inject constructor(
    private val cartRepository: CartRepository,
    private val getDishesByIdsUseCase: GetDishesByIdsUseCase
) {

    suspend fun execute(order: Order): RepeatOrderResult {

        val orderItems = order.products
        if(orderItems.isEmpty()) {
            return RepeatOrderResult.EmptyOrder
        }
        val dishIds = orderItems.map { it.dish.id }
        val currentDishes = getDishesByIdsUseCase.execute(dishIds)
        val availableDishIds = currentDishes.map { dish ->
            dish.id }.toSet()
        val availableOrder =
            orderItems.filter { cartItem -> cartItem.dish.id in availableDishIds }
        if (availableOrder.isEmpty()) {
            return RepeatOrderResult.NoAvailableDishes
        }
        val hasMissingDishes = availableOrder.size < orderItems.size
        val newCart =
            availableOrder.map { cartItem ->

                CartEntry(
                    dishId = cartItem.dish.id,
                    quantity = cartItem.quantity
                )
            }

        cartRepository.replaceCart(
            newCart
        )
        return if (hasMissingDishes) {
            RepeatOrderResult.PartialSuccess
        } else {
            RepeatOrderResult.Success
        }
    }
}