package com.example.catering.domain.usecase.favorite

import com.example.catering.domain.repository.FavoriteRepository
import javax.inject.Inject

class AddFavoriteDishUseCase @Inject constructor(private val favoriteRepository: FavoriteRepository) {

    suspend fun execute(dishId: Int) = favoriteRepository.addFavoriteDish(dishId)

}