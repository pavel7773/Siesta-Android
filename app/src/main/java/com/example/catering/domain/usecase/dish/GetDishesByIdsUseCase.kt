package com.example.catering.domain.usecase.dish

import com.example.catering.domain.model.Dish
import com.example.catering.domain.repository.MenuRepository
import javax.inject.Inject

class GetDishesByIdsUseCase @Inject constructor(private val menuRepository: MenuRepository) {

    suspend fun execute(dishIds: List<Int>) : List<Dish> = menuRepository.getDishesByIds(dishIds)

}