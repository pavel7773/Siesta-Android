package com.example.catering.presentation.favorite

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

sealed interface FavoriteUiState {
    data object Loading : FavoriteUiState
    data class Success(val dishes: List<Dish>) : FavoriteUiState
    data object Empty : FavoriteUiState
    data object Error : FavoriteUiState
}
@HiltViewModel
class FavoriteDishesViewModel @Inject constructor(private val getFavoriteDishesUseCase: GetDishesByIdsUseCase) :
    ViewModel() {
    private val _favoriteUiState = MutableStateFlow<FavoriteUiState>(value = FavoriteUiState.Empty)
    val favoriteUiState: StateFlow<FavoriteUiState> = _favoriteUiState.asStateFlow()

    fun loadFavoriteDishes(favoriteIds: List<Int>) {
        viewModelScope.launch {
            if (favoriteIds.isEmpty()) {
                _favoriteUiState.value = FavoriteUiState.Empty
                return@launch
            }
            _favoriteUiState.value = FavoriteUiState.Loading

            try {
                val favoriteDishes = getFavoriteDishesUseCase.execute(favoriteIds)
                _favoriteUiState.value =
                    if (favoriteDishes.isEmpty()) {
                        FavoriteUiState.Empty
                    } else {
                        FavoriteUiState.Success(favoriteDishes)
                    }

            } catch (e: Exception) {
                _favoriteUiState.value = FavoriteUiState.Error
            }
        }
    }
}