package com.example.catering.presentation.recently

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catering.domain.usecase.recently.AddRecentlyViewedUseCase
import com.example.catering.domain.usecase.recently.ClearRecentlyViewedUseCase
import com.example.catering.domain.usecase.recently.GetRecentlyViewedIdsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecentlyViewedViewModel @Inject constructor(
    private val addRecentlyViewedUseCase: AddRecentlyViewedUseCase,
    getRecentlyViewedIdsUseCase: GetRecentlyViewedIdsUseCase,
    private val clearRecentlyViewedUseCase: ClearRecentlyViewedUseCase
) : ViewModel() {

    val recentlyViewedDishIdsState =
        getRecentlyViewedIdsUseCase.execute()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addRecentlyViewed(dishId: Int) {
        viewModelScope.launch {
            addRecentlyViewedUseCase.execute(dishId)
        }
    }

    fun clearRecentlyViewed() {
        viewModelScope.launch {
            clearRecentlyViewedUseCase.execute()
        }
    }
}