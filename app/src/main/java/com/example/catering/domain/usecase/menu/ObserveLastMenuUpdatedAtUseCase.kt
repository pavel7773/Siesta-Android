package com.example.catering.domain.usecase.menu

import com.example.catering.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveLastMenuUpdatedAtUseCase @Inject constructor(private val menuRepository: MenuRepository) {

    fun execute(): Flow<Long?> = menuRepository.observeLastMenuUpdatedAt()
}