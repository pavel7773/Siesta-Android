package com.example.catering.domain.usecase.order

import com.example.catering.domain.model.Order
import com.example.catering.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllOrdersUseCase @Inject constructor(private val orderRepository: OrderRepository) {

    fun execute(): Flow<List<Order>> = orderRepository.getAllOrdersFlow()
}