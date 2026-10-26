package com.example.catering.domain.usecase.dish

import com.example.catering.domain.model.MenuLoadResult
import com.example.catering.domain.repository.MenuRepository
import javax.inject.Inject

class GetMenuUseCase @Inject constructor(private val menuRepository: MenuRepository) {
    suspend fun execute(): MenuLoadResult = menuRepository.getMenu()
}