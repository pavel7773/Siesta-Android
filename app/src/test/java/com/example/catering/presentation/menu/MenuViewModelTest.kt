package com.example.catering.presentation.menu

import com.example.catering.domain.model.Dish
import com.example.catering.domain.model.MenuDataSourceType
import com.example.catering.domain.model.MenuLoadResult
import com.example.catering.domain.model.PriceRange
import com.example.catering.domain.usecase.dish.GetMenuUseCase
import com.example.catering.domain.usecase.menu.ObserveLastMenuUpdatedAtUseCase
import com.example.catering.domain.usecase.menu.ObserveMenuUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
class MenuViewModelTest {

    private val testDispatcher =
        StandardTestDispatcher()

    private lateinit var getMenuUseCase: GetMenuUseCase
    private lateinit var observeMenuUseCase: ObserveMenuUseCase
    private lateinit var observeLastMenuUpdatedAtUseCase:
            ObserveLastMenuUpdatedAtUseCase

    private lateinit var cachedMenuFlow:
            MutableStateFlow<List<Dish>>

    private lateinit var lastUpdatedFlow:
            MutableStateFlow<Long?>

    @Before
    fun setup() {

        Dispatchers.setMain(testDispatcher)

        getMenuUseCase = mockk()
        observeMenuUseCase = mockk()
        observeLastMenuUpdatedAtUseCase = mockk()

        cachedMenuFlow =
            MutableStateFlow(emptyList())

        lastUpdatedFlow =
            MutableStateFlow(null)

        every {
            observeMenuUseCase.execute()
        } returns cachedMenuFlow

        every {
            observeLastMenuUpdatedAtUseCase.execute()
        } returns lastUpdatedFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `remote menu success exposes remote dishes without refresh error`() =
        runTest {

            val dishes = listOf(
                createDish(
                    id = 1,
                    name = "Паста",
                    price = 500,
                    category = "Горячее"
                ),
                createDish(
                    id = 2,
                    name = "Салат",
                    price = 300,
                    category = "Салаты"
                )
            )

            coEvery {
                getMenuUseCase.execute()
            } returns createMenuResult(
                dishes = dishes,
                source = MenuDataSourceType.REMOTE,
                lastUpdatedAtMillis = 1000L
            )

            val viewModel =
                createViewModel()

            advanceUntilIdle()

            val state =
                viewModel.menuDishes.value

            assertTrue(
                state is MenuUiState.Success
            )

            state as MenuUiState.Success

            assertEquals(
                dishes,
                state.dishes
            )

            assertEquals(
                MenuDataSourceType.REMOTE,
                state.source
            )

            assertEquals(
                1000L,
                state.lastUpdatedAtMillis
            )

            assertEquals(
                false,
                state.isRefreshing
            )

            assertEquals(
                null,
                state.refreshErrorMessage
            )
        }

    @Test
    fun `cache menu result exposes saved version warning`() =
        runTest {

            val dishes = listOf(
                createDish(
                    id = 1,
                    name = "Паста",
                    price = 500,
                    category = "Горячее"
                )
            )

            coEvery {
                getMenuUseCase.execute()
            } returns createMenuResult(
                dishes = dishes,
                source = MenuDataSourceType.CACHE,
                lastUpdatedAtMillis = 500L
            )

            val viewModel =
                createViewModel()

            advanceUntilIdle()

            val state =
                viewModel.menuDishes.value as MenuUiState.Success

            assertEquals(
                MenuDataSourceType.CACHE,
                state.source
            )

            assertEquals(
                "Не удалось обновить меню. Показана сохранённая версия.",
                state.refreshErrorMessage
            )
        }

    @Test
    fun `refresh error without cached menu exposes error state`() =
        runTest {

            coEvery {
                getMenuUseCase.execute()
            } throws IOException(
                "No internet"
            )

            val viewModel =
                createViewModel()

            advanceUntilIdle()

            assertEquals(
                MenuUiState.Error,
                viewModel.menuDishes.value
            )
        }

    @Test
    fun `refresh error keeps cached dishes and exposes refresh warning`() =
        runTest {

            val cachedDish =
                createDish(
                    id = 1,
                    name = "Сохранённая паста",
                    price = 500,
                    category = "Горячее"
                )

            cachedMenuFlow.value =
                listOf(cachedDish)

            lastUpdatedFlow.value =
                1234L

            coEvery {
                getMenuUseCase.execute()
            } throws IOException(
                "No internet"
            )

            val viewModel =
                createViewModel()

            advanceUntilIdle()

            val state =
                viewModel.menuDishes.value as MenuUiState.Success

            assertEquals(
                listOf(cachedDish),
                state.dishes
            )

            assertEquals(
                1234L,
                state.lastUpdatedAtMillis
            )

            assertEquals(
                "Не удалось обновить меню. Показана сохранённая версия.",
                state.refreshErrorMessage
            )

            assertEquals(
                false,
                state.isRefreshing
            )
        }

    @Test
    fun `available categories contain all distinct non blank categories`() =
        runTest {

            val dishes =
                listOf(
                    createDish(
                        id = 1,
                        category = "Горячее"
                    ),
                    createDish(
                        id = 2,
                        category = "Салаты"
                    ),
                    createDish(
                        id = 3,
                        category = "Горячее"
                    ),
                    createDish(
                        id = 4,
                        category = ""
                    )
                )

            coEvery {
                getMenuUseCase.execute()
            } returns createMenuResult(
                dishes = dishes,
                source = MenuDataSourceType.REMOTE,
                lastUpdatedAtMillis = 1000L
            )

            val viewModel =
                createViewModel()

            // availableCategories использует WhileSubscribed.
            // Поэтому в тесте нужен реальный подписчик.
            backgroundScope.launch(
                UnconfinedTestDispatcher(testScheduler)
            ) {
                viewModel.availableCategories.collect { }
            }

            advanceUntilIdle()

            assertEquals(
                listOf(
                    "Все",
                    "Горячее",
                    "Салаты"
                ),
                viewModel.availableCategories.value
            )
        }

    @Test
    fun `category price filter and descending sort are combined correctly`() =
        runTest {

            val dishes =
                listOf(
                    createDish(
                        id = 1,
                        name = "Паста",
                        price = 500,
                        category = "Горячее"
                    ),
                    createDish(
                        id = 2,
                        name = "Стейк",
                        price = 800,
                        category = "Горячее"
                    ),
                    createDish(
                        id = 3,
                        name = "Дорогая пицца",
                        price = 1200,
                        category = "Горячее"
                    ),
                    createDish(
                        id = 4,
                        name = "Салат",
                        price = 450,
                        category = "Салаты"
                    )
                )

            coEvery {
                getMenuUseCase.execute()
            } returns createMenuResult(
                dishes = dishes,
                source = MenuDataSourceType.REMOTE,
                lastUpdatedAtMillis = 1000L
            )

            val viewModel =
                createViewModel()

            backgroundScope.launch(
                UnconfinedTestDispatcher(testScheduler)
            ) {
                viewModel.displayMenuUiState.collect { }
            }

            advanceUntilIdle()

            viewModel.updateSelectedCategory(
                "Горячее"
            )

            viewModel.updateSelectedPriceRange(
                createPriceRange(
                    minPrice = 400,
                    maxPrice = 900
                )
            )

            viewModel.updateSelectedSort(
                SortingOfDishes.PRICE_DESC
            )

            runCurrent()

            val state =
                viewModel.displayMenuUiState.value
                        as MenuUiState.Success

            assertEquals(
                listOf(2, 1),
                state.dishes.map { it.id }
            )
        }

    @Test
    fun `search trims text and matches dish name or info ignoring case`() =
        runTest {

            val dishes =
                listOf(
                    createDish(
                        id = 1,
                        name = "Паста Карбонара",
                        info = "Сливочный соус"
                    ),
                    createDish(
                        id = 2,
                        name = "Салат",
                        info = "Свежие овощи"
                    ),
                    createDish(
                        id = 3,
                        name = "Суп",
                        info = "ПАСТА и овощи"
                    )
                )

            coEvery {
                getMenuUseCase.execute()
            } returns createMenuResult(
                dishes = dishes,
                source = MenuDataSourceType.REMOTE,
                lastUpdatedAtMillis = 1000L
            )

            val viewModel =
                createViewModel()

            backgroundScope.launch(
                UnconfinedTestDispatcher(testScheduler)
            ) {
                viewModel.displayMenuUiState.collect { }
            }

            advanceUntilIdle()

            viewModel.updateSearchQuery(
                "   паста   "
            )

            runCurrent()

            val state =
                viewModel.displayMenuUiState.value
                        as MenuUiState.Success

            assertEquals(
                listOf(1, 3),
                state.dishes.map { it.id }
            )
        }

    @Test
    fun `second refresh while first refresh is running does not start another request`() =
        runTest {

            val deferred =
                CompletableDeferred<MenuLoadResult>()

            coEvery {
                getMenuUseCase.execute()
            } coAnswers {
                deferred.await()
            }

            val viewModel =
                createViewModel()

            // Запускаем первоначальный refresh из init.
            runCurrent()

            // Пользователь быстро нажал refresh ещё раз.
            viewModel.refreshMenu()

            runCurrent()

            coVerify(exactly = 1) {
                getMenuUseCase.execute()
            }

            deferred.complete(
                createMenuResult(
                    dishes = listOf(
                        createDish(id = 1)
                    ),
                    source = MenuDataSourceType.REMOTE,
                    lastUpdatedAtMillis = 1000L
                )
            )

            advanceUntilIdle()
        }

    @Test
    fun `refresh cancellation is not converted into menu error`() =
        runTest {

            coEvery {
                getMenuUseCase.execute()
            } throws CancellationException(
                "Coroutine cancelled"
            )

            val viewModel =
                createViewModel()

            advanceUntilIdle()

            // Отмена coroutine — не ошибка загрузки меню.
            //
            // Поэтому CancellationException нельзя превращать
            // в MenuUiState.Error.
            assertEquals(
                MenuUiState.Loading,
                viewModel.menuDishes.value
            )
        }

    @Test
    fun `home category shortcut clears stale search price and sorting`() = runTest {
        coEvery { getMenuUseCase.execute() } returns createMenuResult(
            listOf(createDish(1, category = "Паста"), createDish(2, category = "Пицца")),
            MenuDataSourceType.REMOTE, 1000L
        )
        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.displayMenuUiState.collect { }
        }
        viewModel.updateSearchQuery("старый поиск")
        viewModel.updateSelectedPriceRange(createPriceRange(0, 1))
        viewModel.updateSelectedSort(SortingOfDishes.PRICE_DESC)
        viewModel.openFromHome(category = "Паста")
        advanceUntilIdle()
        assertEquals("", viewModel.searchQuery.value)
        assertEquals(null, viewModel.selectedPriceRange.value)
        assertEquals(SortingOfDishes.DEFAULT, viewModel.selectedSort.value)
        assertEquals(listOf(1), (viewModel.displayMenuUiState.value as MenuUiState.Success).dishes.map { it.id })
    }

    @Test
    fun `home filters preserve entered query and request is consumed once`() = runTest {
        coEvery { getMenuUseCase.execute() } returns createMenuResult(emptyList(), MenuDataSourceType.REMOTE, null)
        val viewModel = createViewModel()
        viewModel.updateSelectedCategory("Паста")
        viewModel.openFromHome(query = "pizza", openFilters = true)
        assertEquals("pizza", viewModel.searchQuery.value)
        assertEquals("Все", viewModel.selectedCategory.value)
        assertTrue(viewModel.openFiltersRequested.value)
        viewModel.consumeFiltersRequest()
        assertEquals(false, viewModel.openFiltersRequested.value)
        advanceUntilIdle()
    }

    @Test
    fun `home latin search uses menu search and respects subsequent price filter`() = runTest {
        coEvery { getMenuUseCase.execute() } returns createMenuResult(
            listOf(createDish(1, "Пицца", 500), createDish(2, "Пицца", 900), createDish(3, "Паста", 500)),
            MenuDataSourceType.REMOTE, 1000L
        )
        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.displayMenuUiState.collect { }
        }
        viewModel.openFromHome(query = " PIZZA ")
        viewModel.updateSelectedPriceRange(createPriceRange(400, 600))
        advanceUntilIdle()
        assertEquals(listOf(1), (viewModel.displayMenuUiState.value as MenuUiState.Success).dishes.map { it.id })
    }

    private fun createViewModel(): MenuViewModel {

        return MenuViewModel(
            getMenuUseCase = getMenuUseCase,
            observeMenuUseCase = observeMenuUseCase,
            observeLastMenuUpdatedAtUseCase =
                observeLastMenuUpdatedAtUseCase
        )
    }

    private fun createMenuResult(
        dishes: List<Dish>,
        source: MenuDataSourceType,
        lastUpdatedAtMillis: Long?
    ): MenuLoadResult {

        val result =
            mockk<MenuLoadResult>()

        every {
            result.dishes
        } returns dishes

        every {
            result.source
        } returns source

        every {
            result.lastUpdatedAtMillis
        } returns lastUpdatedAtMillis

        return result
    }

    private fun createPriceRange(
        minPrice: Int,
        maxPrice: Int
    ): PriceRange {

        val range =
            mockk<PriceRange>()

        every {
            range.minPrice
        } returns minPrice

        every {
            range.maxPrice
        } returns maxPrice

        return range
    }

    private fun createDish(
        id: Int,
        name: String = "Блюдо $id",
        price: Int = 500,
        info: String = "Описание",
        category: String = "Горячее"
    ): Dish {

        return Dish(
            id = id,
            name = name,
            price = price,
            info = info,
            imageUrl = "",
            weight = 300,
            calories = 400,
            proteins = 20.0,
            fats = 15.0,
            carbs = 40.0,
            category = category
        )
    }
}
