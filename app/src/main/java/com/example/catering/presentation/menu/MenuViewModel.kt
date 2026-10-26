package com.example.catering.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catering.domain.model.Dish
import com.example.catering.domain.model.matchesSearch
import com.example.catering.domain.model.MenuDataSourceType
import com.example.catering.domain.model.PriceRange
import com.example.catering.domain.usecase.dish.GetMenuUseCase
import com.example.catering.domain.usecase.menu.ObserveLastMenuUpdatedAtUseCase
import com.example.catering.domain.usecase.menu.ObserveMenuUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

sealed interface MenuUiState {
    data object Loading : MenuUiState
    data object Error : MenuUiState
    data class Success(
        val dishes: List<Dish>,
        val source: MenuDataSourceType,
        val lastUpdatedAtMillis: Long?,
        val isRefreshing: Boolean,
        val refreshErrorMessage: String?
    ) : MenuUiState
}

enum class SortingOfDishes {
    DEFAULT, PRICE_ASC, PRICE_DESC
}

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val getMenuUseCase: GetMenuUseCase,
    private val observeMenuUseCase: ObserveMenuUseCase,
    private val observeLastMenuUpdatedAtUseCase: ObserveLastMenuUpdatedAtUseCase
) : ViewModel() {

    private val _menuDishes = MutableStateFlow<MenuUiState>(value = MenuUiState.Loading)

    private var refreshInProgress = false

    val menuDishes: StateFlow<MenuUiState> = _menuDishes.asStateFlow()

    private val _searchQuery = MutableStateFlow<String>(value = "")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String>(value = "Все")

    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedSort = MutableStateFlow<SortingOfDishes>(value = SortingOfDishes.DEFAULT)

    val selectedSort: StateFlow<SortingOfDishes> = _selectedSort.asStateFlow()

    private val _selectedPriceRange = MutableStateFlow<PriceRange?>(value = null)

    val selectedPriceRange: StateFlow<PriceRange?> = _selectedPriceRange.asStateFlow()
    val availableCategories: StateFlow<List<String>> =
        menuDishes.map { menuUiState ->
            when (menuUiState) {
                MenuUiState.Loading -> {
                    listOf("Все")
                }

                MenuUiState.Error -> {
                    listOf("Все")
                }

                is MenuUiState.Success -> {
                    listOf("Все") + menuUiState.dishes
                        .map { it.category }
                        .filter { it.isNotBlank() }
                        .distinct()
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            listOf("Все")
        )
    val displayMenuUiState: StateFlow<MenuUiState> =
        combine(
            menuDishes,
            searchQuery,
            selectedCategory,
            selectedSort,
            selectedPriceRange
        ) { menuState, query, category, sort, priceRange ->
            when (menuState) {
                MenuUiState.Loading -> {
                    return@combine MenuUiState.Loading
                }

                MenuUiState.Error -> {
                    return@combine MenuUiState.Error
                }

                is MenuUiState.Success -> {
                    val categoryFilteredDishes =
                        if (category == "Все") {
                            menuState.dishes
                        } else {
                            menuState.dishes.filter { it ->
                                it.category == category
                            }
                        }
                    val searchQuery = query.trim()
                    val resultDishes =
                        if (searchQuery.isBlank()) {
                            categoryFilteredDishes
                        } else {
                            categoryFilteredDishes.filter { dish ->
                                dish.matchesSearch(searchQuery)
                            }

                        }
                    val priceFilter =
                        if (priceRange == null) {
                            resultDishes
                        } else {
                            resultDishes.filter { it ->
                                it.price >= priceRange.minPrice && it.price <= priceRange.maxPrice
                            }
                        }
                    val sortedDishes =
                        when (sort) {
                            SortingOfDishes.DEFAULT -> {
                                priceFilter
                            }

                            SortingOfDishes.PRICE_ASC -> {
                                priceFilter.sortedBy { it.price }
                            }

                            SortingOfDishes.PRICE_DESC -> {
                                priceFilter.sortedByDescending { it.price }
                            }
                        }
                    return@combine MenuUiState.Success(
                        dishes = sortedDishes,
                        source = menuState.source,
                        lastUpdatedAtMillis = menuState.lastUpdatedAtMillis,
                        isRefreshing = menuState.isRefreshing,
                        refreshErrorMessage = menuState.refreshErrorMessage
                    )
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            MenuUiState.Loading
        )


    init { // Автозапуск (Открытие ресторана)
        observeCachedMenu()
        refreshMenu()
    }

    fun refreshMenu() {

        // Не запускаем второй refresh,
        // пока предыдущий запрос ещё выполняется.
        //
        // Флаг ставим ДО launch, чтобы два быстрых
        // нажатия не успели создать две coroutine.
        if (refreshInProgress) {
            return
        }

        refreshInProgress = true

        val currentState =
            _menuDishes.value

        if (currentState is MenuUiState.Success) {

            _menuDishes.value =
                currentState.copy(
                    isRefreshing = true,
                    refreshErrorMessage = null
                )

        } else {

            _menuDishes.value =
                MenuUiState.Loading
        }

        viewModelScope.launch {

            try {

                val menuLoadResult =
                    getMenuUseCase.execute()

                val refreshErrorMessage =
                    when (menuLoadResult.source) {

                        MenuDataSourceType.REMOTE ->
                            null

                        MenuDataSourceType.CACHE ->
                            "Не удалось обновить меню. Показана сохранённая версия."
                    }

                _menuDishes.value =
                    MenuUiState.Success(
                        dishes =
                            menuLoadResult.dishes,
                        source =
                            menuLoadResult.source,
                        lastUpdatedAtMillis =
                            menuLoadResult.lastUpdatedAtMillis,
                        isRefreshing = false,
                        refreshErrorMessage =
                            refreshErrorMessage
                    )

            } catch (e: CancellationException) {

                // CancellationException означает отмену работы,
                // а не ошибку сети/меню.
                throw e

            } catch (e: Exception) {

                val latestState =
                    _menuDishes.value

                if (latestState is MenuUiState.Success) {

                    _menuDishes.value =
                        latestState.copy(
                            isRefreshing = false,
                            refreshErrorMessage =
                                "Не удалось обновить меню. Показана сохранённая версия."
                        )

                } else {

                    _menuDishes.value =
                        MenuUiState.Error
                }

            } finally {

                refreshInProgress = false
            }
        }
    }

    private val _openFiltersRequested = MutableStateFlow(false)
    val openFiltersRequested = _openFiltersRequested.asStateFlow()

    fun consumeFiltersRequest() { _openFiltersRequested.value = false }

    // A Home shortcut starts a fresh selection, without invisible previous filters.
    fun openFromHome(query: String = "", category: String = "Все", openFilters: Boolean = false) {
        _searchQuery.value = query
        _selectedCategory.value = category
        _selectedPriceRange.value = null
        _selectedSort.value = SortingOfDishes.DEFAULT
        _openFiltersRequested.value = openFilters
    }

    fun updateSearchQuery(newText: String) {
        _searchQuery.value = newText
    }

    fun updateSelectedCategory(newCategory: String) {
        _selectedCategory.value = newCategory
    }

    fun updateSelectedSort(newSorting: SortingOfDishes) {
        _selectedSort.value = newSorting
    }

    fun updateSelectedPriceRange(newRange: PriceRange?) {
        _selectedPriceRange.value = newRange
    }

    fun clearSelectedPriceRange() {
        _selectedPriceRange.value = null

    }


    fun observeCachedMenu() {
        viewModelScope.launch {
            combine(
                observeMenuUseCase.execute(),
                observeLastMenuUpdatedAtUseCase.execute()
            ) { dishes, updatedAtMillis ->
                dishes to updatedAtMillis
            }.collect { (dishes, updatedAtMillis) ->

                // Пустой кэш не должен перезаписывать
                // Error или результат удалённой загрузки.
                if (dishes.isEmpty()) {
                    return@collect
                }

                // Читаем актуальное состояние в момент записи,
                // а не заранее внутри combine.
                _menuDishes.update { currentState ->
                    when (currentState) {

                        is MenuUiState.Success -> {
                            // Обновляем кэшированные данные,
                            // сохраняя статус refresh и ошибки.
                            currentState.copy(
                                dishes = dishes,
                                lastUpdatedAtMillis = updatedAtMillis
                            )
                        }

                        MenuUiState.Error -> {
                            // Ошибка уже произошла, но кэш
                            // стал доступен позднее.
                            MenuUiState.Success(
                                dishes = dishes,
                                source = MenuDataSourceType.CACHE,
                                lastUpdatedAtMillis = updatedAtMillis,
                                isRefreshing = false,
                                refreshErrorMessage =
                                    "Не удалось обновить меню. Показана сохранённая версия."
                            )
                        }

                        MenuUiState.Loading -> {
                            // Показываем кэш, пока продолжается
                            // попытка обновления с сервера.
                            MenuUiState.Success(
                                dishes = dishes,
                                source = MenuDataSourceType.CACHE,
                                lastUpdatedAtMillis = updatedAtMillis,
                                isRefreshing = refreshInProgress,
                                refreshErrorMessage = null
                            )
                        }
                    }
                }
            }
        }
    }

}

