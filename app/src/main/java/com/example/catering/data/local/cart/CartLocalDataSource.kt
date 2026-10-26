package com.example.catering.data.local.cart

import androidx.room.withTransaction
import com.example.catering.data.local.database.AppDatabase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CartLocalDataSource @Inject constructor(
    private val cartItemDao: CartItemDao,
    private val database: AppDatabase
) {

    fun getCartItemsFlow(): Flow<List<CartItemEntity>> {
        return cartItemDao.getCartItemsFlow()
    }

    suspend fun saveCartItem(
        dishId: Int,
        quantity: Int
    ) {

        val cart = CartItemEntity(
            dishId = dishId,
            quantity = quantity
        )

        cartItemDao.upsertCartItem(cart)
    }

    suspend fun deleteCartItemByDishId(
        dishId: Int
    ) {
        cartItemDao.deleteCartItemByDishId(dishId)
    }

    suspend fun clearCart() {
        cartItemDao.clearCart()
    }

    suspend fun replaceCartItems(
        items: List<CartItemEntity>
    ) {

        // clear + insert выполняются как одна операция.
        //
        // Если вставка хотя бы одного блюда упадёт,
        // Room откатит и clearCart(), и предыдущие insert.
        database.withTransaction {

            cartItemDao.clearCart()

            cartItemDao.upsertCartItems(
                items
            )
        }
    }

    suspend fun changeQuantity(
        dishId: Int,
        delta: Int
    ) {

        database.withTransaction {

            // Читаем АКТУАЛЬНОЕ значение непосредственно из Room,
            // а не старое количество из UI.
            val currentItem =
                cartItemDao.getCartItemByDishId(
                    dishId = dishId
                )

            val currentQuantity =
                currentItem?.quantity ?: 0

            val newQuantity =
                currentQuantity + delta

            if (newQuantity > 0) {

                cartItemDao.upsertCartItem(
                    CartItemEntity(
                        dishId = dishId,
                        quantity = newQuantity
                    )
                )

            } else if (currentItem != null) {

                // Если количество стало 0 или меньше,
                // полностью удаляем блюдо из корзины.
                cartItemDao.deleteCartItemByDishId(
                    dishId = dishId
                )
            }
        }
    }
}