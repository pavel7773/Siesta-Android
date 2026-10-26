package com.example.catering.domain.cart

import com.example.catering.domain.model.CartEntry
import com.example.catering.domain.model.CartItem
import com.example.catering.domain.model.Dish

fun buildCartItems(
    cartEntries: List<CartEntry>,
    menuDishes: List<Dish>
): List<CartItem> {

    val dishesById = menuDishes.associateBy { dish ->
        dish.id
    }

    return cartEntries.map { entry ->

        val dish = dishesById[entry.dishId]
            ?: throw IllegalStateException(
                "Dish ${entry.dishId} is missing from menu"
            )

        CartItem(
            dish = dish,
            quantity = entry.quantity
        )
    }
}
fun calculateTotalPrice(cartItems: List<CartItem>): Long {
    return cartItems.sumOf { cartItem ->
        cartItem.quantity * cartItem.dish.price.toLong()
    }
}