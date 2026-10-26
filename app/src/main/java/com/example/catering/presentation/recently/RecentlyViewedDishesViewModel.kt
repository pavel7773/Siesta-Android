package com.example.catering.presentation.recently

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catering.domain.model.Dish
import com.example.catering.domain.usecase.dish.GetDishesByIdsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RecentlyUiState {
    data object Loading : RecentlyUiState
    data class Success(val dishes: List<Dish>) : RecentlyUiState

    data object Empty : RecentlyUiState
    data object Error : RecentlyUiState
}
@HiltViewModel
class RecentlyViewedDishesViewModel @Inject constructor(private val getDishesByIdsUseCase: GetDishesByIdsUseCase) :
    ViewModel() {
    private val _recentlyViewedUiState =
        MutableStateFlow<RecentlyUiState>(value = RecentlyUiState.Empty)
    val recentlyViewedUiState: StateFlow<RecentlyUiState> = _recentlyViewedUiState.asStateFlow()

    fun loadRecentlyViewed(dishIds: List<Int>) {
        viewModelScope.launch {
            if (dishIds.isEmpty()) {
                _recentlyViewedUiState.value = RecentlyUiState.Empty
                return@launch
            }
            try {
                _recentlyViewedUiState.value = RecentlyUiState.Loading

                val dishes = getDishesByIdsUseCase.execute(dishIds)

                if (dishes.isEmpty()) {
                    _recentlyViewedUiState.value = RecentlyUiState.Empty
                } else {
                    _recentlyViewedUiState.value = RecentlyUiState.Success(dishes = dishes)
                }
            } catch (e: Exception) {
                _recentlyViewedUiState.value = RecentlyUiState.Error
            }

        }
    }
}