package com.example.catering.presentation.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catering.domain.usecase.favorite.AddFavoriteDishUseCase
import com.example.catering.domain.usecase.favorite.GetFavoriteDishIdsUseCase
import com.example.catering.domain.usecase.favorite.RemoveFavoriteDishUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    getFavoriteDishIdsUseCase: GetFavoriteDishIdsUseCase,
    private val addFavoriteDishUseCase: AddFavoriteDishUseCase,
    private val removeFavoriteDishUseCase: RemoveFavoriteDishUseCase,
) : ViewModel() {
    val favoriteDishIdsState = getFavoriteDishIdsUseCase.execute()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val favoriteBadgeState: StateFlow<FavoriteBadgeState> =
        favoriteDishIdsState
            .map { ids ->
                val count = ids.size
                FavoriteBadgeState(
                    count = count,
                    text = if (count <= 99) count.toString() else "99+"
                )
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                FavoriteBadgeState(
                    count = 0,
                    text = "0"
                )
            )

    fun toggleFavorite(dishId: Int, isCurrentlyFavorite: Boolean) {
        viewModelScope.launch {
            if (isCurrentlyFavorite) {
                removeFavoriteDishUseCase.execute(dishId)
            } else {
                addFavoriteDishUseCase.execute(dishId)
            }
        }
    }

    fun deleteFavorite(dishId: Int) {
        viewModelScope.launch {
            removeFavoriteDishUseCase.execute(dishId)
        }
    }
}


