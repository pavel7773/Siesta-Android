package com.example.catering.presentation.dishdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catering.domain.model.Dish
import com.example.catering.domain.usecase.dish.GetDishByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DishDetailUiState {

    data object Loading : DishDetailUiState
    data class Success(val dish: Dish) : DishDetailUiState
    data object Error : DishDetailUiState
}
@HiltViewModel
class DishDetailViewModel @Inject constructor(
    private val getDishByIdUseCase: GetDishByIdUseCase) : ViewModel() {
    private val _dishDetailUiState =
        MutableStateFlow<DishDetailUiState>(value = DishDetailUiState.Loading)

    val dishDetailUiState: StateFlow<DishDetailUiState> = _dishDetailUiState.asStateFlow()


    fun loadDish(dishId: Int) {

        _dishDetailUiState.value = DishDetailUiState.Loading
        viewModelScope.launch {
            try {
                val dish = getDishByIdUseCase.execute(dishId)
                if (dish != null) {
                    _dishDetailUiState.value = DishDetailUiState.Success(dish)
                } else {
                    _dishDetailUiState.value = DishDetailUiState.Error
                }
            }
            catch (e: Exception) {
                _dishDetailUiState.value = DishDetailUiState.Error
            }
        }
    }
}