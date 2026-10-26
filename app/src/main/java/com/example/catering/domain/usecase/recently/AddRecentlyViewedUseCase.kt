package com.example.catering.domain.usecase.recently

import com.example.catering.domain.repository.RecentlyViewedRepository
import javax.inject.Inject

class AddRecentlyViewedUseCase @Inject constructor(private val recentlyViewedRepository: RecentlyViewedRepository) {

    suspend fun execute(dishId: Int) = recentlyViewedRepository.saveRecentlyViewedDish(dishId)

}