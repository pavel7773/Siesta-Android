package com.example.catering.data.repository.cart

import com.example.catering.data.local.cart.CartItemEntity
import com.example.catering.data.local.cart.CartLocalDataSource
import com.example.catering.domain.model.CartEntry
import com.example.catering.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CartRoomRepository @Inject constructor(
    private val cartLocalDataSource: CartLocalDataSource
) : CartRepository {


    override fun getAllCartFlow(): Flow<List<CartEntry>> {
        return cartLocalDataSource.getCartItemsFlow().map { entities ->
            entities.map { entity ->
                CartEntry(
                    entity.dishId,
                    entity.quantity
                )
            }
        }
    }

    override suspend fun addCart(
        dishId: Int,
        quantity: Int
    ) {
        cartLocalDataSource.saveCartItem(dishId, quantity)
    }

    override suspend fun removeCart(dishId: Int) {
        cartLocalDataSource.deleteCartItemByDishId(dishId)
    }

    override suspend fun clearCart() {
        cartLocalDataSource.clearCart()
    }

    override suspend fun replaceCart(
        items: List<CartEntry>
    ) {

        val entities =
            items.map { item ->

                CartItemEntity(
                    dishId = item.dishId,
                    quantity = item.quantity
                )
            }

        cartLocalDataSource.replaceCartItems(
            entities
        )
    }
    override suspend fun changeQuantity(
        dishId: Int,
        delta: Int
    ) {
        cartLocalDataSource.changeQuantity(
            dishId = dishId,
            delta = delta
        )
    }
}