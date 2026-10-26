package com.example.catering.domain.usecase.dish

import com.example.catering.domain.model.Dish
import com.example.catering.domain.repository.MenuRepository
import javax.inject.Inject

class GetDishByIdUseCase @Inject constructor(private val menuRepository: MenuRepository) {

    suspend fun execute(dishId: Int) : Dish? = menuRepository.getDishById(dishId)
}