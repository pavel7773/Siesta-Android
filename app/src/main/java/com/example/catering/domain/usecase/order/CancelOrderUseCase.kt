package com.example.catering.domain.usecase.order

import com.example.catering.domain.repository.OrderRepository
import javax.inject.Inject

class CancelOrderUseCase @Inject constructor(private val orderRepository: OrderRepository) {
        suspend fun execute(orderId: Int) {
            orderRepository.cancelOrder(orderId)
        }
    }
