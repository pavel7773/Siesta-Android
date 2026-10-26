package com.example.catering.domain.usecase.recently

import com.example.catering.domain.repository.RecentlyViewedRepository
import javax.inject.Inject

class GetRecentlyViewedIdsUseCase @Inject constructor(private val recentlyViewedRepository: RecentlyViewedRepository) {
     fun execute() = recentlyViewedRepository.getRecentlyViewedDishIdsFlow()
}