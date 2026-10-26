package com.example.catering.domain.usecase.recently

import com.example.catering.domain.repository.RecentlyViewedRepository
import javax.inject.Inject

class ClearRecentlyViewedUseCase @Inject constructor(private val recentlyViewedRepository: RecentlyViewedRepository) {

    suspend fun execute() = recentlyViewedRepository.clearRecentlyViewedDishes()
}