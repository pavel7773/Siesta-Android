package com.example.catering.domain.usecase.menu

import com.example.catering.domain.model.Dish
import com.example.catering.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMenuUseCase @Inject constructor(private val menuRepository: MenuRepository) {

    fun execute(): Flow<List<Dish>> = menuRepository.observeMenu()
}