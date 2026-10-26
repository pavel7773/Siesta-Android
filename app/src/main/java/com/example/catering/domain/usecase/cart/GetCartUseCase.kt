package com.example.catering.domain.usecase.cart

import com.example.catering.domain.model.CartEntry
import com.example.catering.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCartUseCase @Inject constructor(private val cartRepository: CartRepository) {

    fun execute(): Flow<List<CartEntry>> = cartRepository.getAllCartFlow()
}