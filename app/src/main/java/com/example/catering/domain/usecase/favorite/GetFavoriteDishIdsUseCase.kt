package com.example.catering.domain.usecase.favorite

import com.example.catering.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoriteDishIdsUseCase @Inject constructor(private val favoriteRepository: FavoriteRepository) {

    fun execute(): Flow<List<Int>> = favoriteRepository.getFavoriteDishIdsFlow()

}