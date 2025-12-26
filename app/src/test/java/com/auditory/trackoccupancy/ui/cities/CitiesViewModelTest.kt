package com.auditory.trackoccupancy.ui.cities

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.auditory.trackoccupancy.data.model.City
import com.auditory.trackoccupancy.data.model.LocalizedString
import com.auditory.trackoccupancy.data.repository.OccupancyRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

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
        viewModel = CitiesViewModel(mockRepository)
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
        `when`(mockRepository.getCities()).thenReturn(Result.success(cities))

        // When
        viewModel.loadCities()
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is CitiesUiState.Success)
        assertEquals(cities, (finalState as CitiesUiState.Success).cities)
        verify(mockRepository).getCities()
    }

    @Test
    fun `loadCities updates uiState to Error when repository returns failure with exception message`() = runTest(testDispatcher) {
        // Given
        val exception = RuntimeException("Network error")
        `when`(mockRepository.getCities()).thenReturn(Result.failure(exception))

        // When
        viewModel.loadCities()
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is CitiesUiState.Error)
        assertEquals("Network error", (finalState as CitiesUiState.Error).message)
        verify(mockRepository).getCities()
    }

    @Test
    fun `loadCities updates uiState to Error when repository returns failure with null message`() = runTest(testDispatcher) {
        // Given
        val exception = RuntimeException()
        `when`(mockRepository.getCities()).thenReturn(Result.failure(exception))

        // When
        viewModel.loadCities()
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is CitiesUiState.Error)
        assertEquals("Failed to load cities", (finalState as CitiesUiState.Error).message)
        verify(mockRepository).getCities()
    }

    @Test
    fun `loadCities updates uiState to Loading immediately when called`() = runTest(testDispatcher) {
        // Given
        val cities = listOf(City(id = 1L, name = LocalizedString(ru = "Test", en = "Test")))
        `when`(mockRepository.getCities()).thenReturn(Result.success(cities))

        // When
        viewModel.loadCities()

        // Then - Should be Loading immediately
        assertTrue(viewModel.uiState.value is CitiesUiState.Loading)

        // And then Success after coroutine completes
        advanceUntilIdle()
        val finalState = viewModel.uiState.value
        assertTrue(finalState is CitiesUiState.Success)
    }

    @Test
    fun `loadCities handles empty cities list correctly`() = runTest(testDispatcher) {
        // Given
        val emptyCities = emptyList<City>()
        `when`(mockRepository.getCities()).thenReturn(Result.success(emptyCities))

        // When
        viewModel.loadCities()
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is CitiesUiState.Success)
        assertTrue((finalState as CitiesUiState.Success).cities.isEmpty())
        verify(mockRepository).getCities()
    }

    @Test
    fun `loadCities can be called multiple times and each call updates uiState to Loading first`() = runTest(testDispatcher) {
        // Given
        val cities1 = listOf(City(id = 1L, name = LocalizedString(ru = "City1", en = "City1")))
        val cities2 = listOf(City(id = 2L, name = LocalizedString(ru = "City2", en = "City2")))
        `when`(mockRepository.getCities()).thenReturn(Result.success(cities1), Result.success(cities2))

        // When - First call
        viewModel.loadCities()
        advanceUntilIdle()

        // Then - First result
        var finalState = viewModel.uiState.value
        assertTrue(finalState is CitiesUiState.Success)
        assertEquals(cities1, (finalState as CitiesUiState.Success).cities)

        // When - Second call
        viewModel.loadCities()

        // Then - Should be Loading again
        assertTrue(viewModel.uiState.value is CitiesUiState.Loading)

        // And then second result
        advanceUntilIdle()
        finalState = viewModel.uiState.value
        assertTrue(finalState is CitiesUiState.Success)
        assertEquals(cities2, (finalState as CitiesUiState.Success).cities)

        verify(mockRepository, times(2)).getCities()
    }
}
