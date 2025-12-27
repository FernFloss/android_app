package com.auditory.trackoccupancy.ui.cities

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.auditory.trackoccupancy.data.model.City
import com.auditory.trackoccupancy.data.model.LocalizedString
import com.auditory.trackoccupancy.data.repository.OccupancyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class CitiesViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockRepository: OccupancyRepository

    private lateinit var viewModel: CitiesViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = CitiesViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState is Loading`() {
        // When - ViewModel is created

        // Then
        assertTrue(viewModel.uiState.value is CitiesUiState.Loading)
    }

    @Test
    fun `loadCities updates uiState to Success when repository returns cities successfully`() = runTest(testDispatcher) {
        // Given
        val cities = listOf(
            City(id = 1L, name = LocalizedString(ru = "Москва", en = "Moscow")),
            City(id = 2L, name = LocalizedString(ru = "СПб", en = "Saint Petersburg"))
        )
        whenever(mockRepository.getCities()).thenReturn(Result.success(cities))

        // When
        viewModel.loadCities()
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is CitiesUiState.Success)
        assertEquals(cities, (finalState as CitiesUiState.Success).cities)
    }

    @Test
    fun `loadCities updates uiState to Error when repository returns failure with exception message`() = runTest(testDispatcher) {
        // Given
        val exception = RuntimeException("Network error")
        whenever(mockRepository.getCities()).thenReturn(Result.failure(exception))

        // When
        viewModel.loadCities()
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is CitiesUiState.Error)
        assertEquals("Network error", (finalState as CitiesUiState.Error).message)
    }

    @Test
    fun `loadCities updates uiState to Error when repository returns failure with null message`() = runTest(testDispatcher) {
        // Given
        val exception = RuntimeException()
        whenever(mockRepository.getCities()).thenReturn(Result.failure(exception))

        // When
        viewModel.loadCities()
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is CitiesUiState.Error)
        assertEquals("Failed to load cities", (finalState as CitiesUiState.Error).message)
    }

    @Test
    fun `loadCities transitions through Loading state before Success`() = runTest {
        // Given - Use StandardTestDispatcher to control coroutine execution
        val standardDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(standardDispatcher)
        val vm = CitiesViewModel(mockRepository)
        
        val cities = listOf(City(id = 1L, name = LocalizedString(ru = "Test", en = "Test")))
        whenever(mockRepository.getCities()).thenReturn(Result.success(cities))

        // Initial state should be Loading
        assertTrue(vm.uiState.value is CitiesUiState.Loading)

        // When - Start loading (coroutine is suspended)
        vm.loadCities()
        
        // State should still be Loading since coroutine hasn't completed
        assertTrue(vm.uiState.value is CitiesUiState.Loading)

        // Advance until coroutine completes
        advanceUntilIdle()
        
        // Then - Final state should be Success
        val finalState = vm.uiState.value
        assertTrue(finalState is CitiesUiState.Success)
    }

    @Test
    fun `loadCities handles empty cities list correctly`() = runTest(testDispatcher) {
        // Given
        val emptyCities = emptyList<City>()
        whenever(mockRepository.getCities()).thenReturn(Result.success(emptyCities))

        // When
        viewModel.loadCities()
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is CitiesUiState.Success)
        assertTrue((finalState as CitiesUiState.Success).cities.isEmpty())
    }

    @Test
    fun `loadCities can be called multiple times with different results`() = runTest(testDispatcher) {
        // Given
        val cities1 = listOf(City(id = 1L, name = LocalizedString(ru = "City1", en = "City1")))
        val cities2 = listOf(City(id = 2L, name = LocalizedString(ru = "City2", en = "City2")))
        whenever(mockRepository.getCities())
            .thenReturn(Result.success(cities1))
            .thenReturn(Result.success(cities2))

        // When - First call
        viewModel.loadCities()
        advanceUntilIdle()

        // Then - First result
        var finalState = viewModel.uiState.value
        assertTrue(finalState is CitiesUiState.Success)
        assertEquals(cities1, (finalState as CitiesUiState.Success).cities)

        // When - Second call
        viewModel.loadCities()
        advanceUntilIdle()

        // Then - Second result
        finalState = viewModel.uiState.value
        assertTrue(finalState is CitiesUiState.Success)
        assertEquals(cities2, (finalState as CitiesUiState.Success).cities)
    }
}
