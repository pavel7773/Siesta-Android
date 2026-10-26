package com.example.catering.domain.usecase.order

import com.example.catering.domain.repository.OrderRepository
import javax.inject.Inject

class RefreshOrdersUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend fun execute() {
        orderRepository.refreshOrders()
    }
}