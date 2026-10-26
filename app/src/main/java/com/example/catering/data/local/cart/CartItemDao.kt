package com.example.catering.data.local.cart

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CartItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCartItem(
        cartItemEntity: CartItemEntity
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCartItems(
        cartItemEntities: List<CartItemEntity>
    )

    @Query("DELETE FROM cart_items WHERE dishId = :dishId")
    suspend fun deleteCartItemByDishId(
        dishId: Int
    )

    @Query("SELECT * FROM cart_items")
    fun getCartItemsFlow(): Flow<List<CartItemEntity>>

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    @Query(
        """
    SELECT * FROM cart_items
    WHERE dishId = :dishId
    LIMIT 1
    """
    )
    suspend fun getCartItemByDishId(
        dishId: Int
    ): CartItemEntity?
}