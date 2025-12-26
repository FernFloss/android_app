package com.auditory.trackoccupancy.ui.buildings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.auditory.trackoccupancy.data.model.Building
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
class BuildingsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockRepository: OccupancyRepository

    private lateinit var viewModel: BuildingsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = BuildingsViewModel(mockRepository)
    }

    @Test
    fun `initial uiState is Loading`() {
        // When - ViewModel is created

        // Then
        assertTrue(viewModel.uiState.value is BuildingsUiState.Loading)
    }

    @Test
    fun `loadBuildings updates uiState to Success when repository returns buildings successfully`() = runTest(testDispatcher) {
        // Given
        val cityId = 1L
        val buildings = listOf(
            Building(
                id = 1L,
                cityId = cityId,
                address = LocalizedString(ru = "ул. Ленина 1", en = "Lenina St. 1"),
                name = LocalizedString(ru = "Главный корпус", en = "Main Building")
            ),
            Building(
                id = 2L,
                cityId = cityId,
                address = LocalizedString(ru = "ул. Пушкина 10", en = "Pushkina St. 10"),
                name = LocalizedString(ru = "Лекционный корпус", en = "Lecture Building")
            )
        )
        `when`(mockRepository.getBuildingsByCity(cityId)).thenReturn(Result.success(buildings))

        // When
        viewModel.loadBuildings(cityId)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is BuildingsUiState.Success)
        assertEquals(buildings, (finalState as BuildingsUiState.Success).buildings)
        verify(mockRepository).getBuildingsByCity(cityId)
    }

    @Test
    fun `loadBuildings updates uiState to Error when repository returns failure`() = runTest(testDispatcher) {
        // Given
        val cityId = 1L
        val exception = RuntimeException("Network error")
        `when`(mockRepository.getBuildingsByCity(cityId)).thenReturn(Result.failure(exception))

        // When
        viewModel.loadBuildings(cityId)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is BuildingsUiState.Error)
        assertEquals("Network error", (finalState as BuildingsUiState.Error).message)
        verify(mockRepository).getBuildingsByCity(cityId)
    }

    @Test
    fun `loadBuildings updates uiState to Error with default message when exception message is null`() = runTest(testDispatcher) {
        // Given
        val cityId = 1L
        val exception = RuntimeException()
        `when`(mockRepository.getBuildingsByCity(cityId)).thenReturn(Result.failure(exception))

        // When
        viewModel.loadBuildings(cityId)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is BuildingsUiState.Error)
        assertEquals("Failed to load buildings", (finalState as BuildingsUiState.Error).message)
        verify(mockRepository).getBuildingsByCity(cityId)
    }

    @Test
    fun `loadBuildings sets uiState to Loading immediately when called`() = runTest(testDispatcher) {
        // Given
        val cityId = 1L
        val buildings = listOf(Building(
            id = 1L,
            cityId = cityId,
            address = LocalizedString(ru = "Test", en = "Test"),
            name = LocalizedString(ru = "Test", en = "Test")
        ))
        `when`(mockRepository.getBuildingsByCity(cityId)).thenReturn(Result.success(buildings))

        // When
        viewModel.loadBuildings(cityId)

        // Then - Should be Loading immediately
        assertTrue(viewModel.uiState.value is BuildingsUiState.Loading)

        // And then Success after coroutine completes
        advanceUntilIdle()
        val finalState = viewModel.uiState.value
        assertTrue(finalState is BuildingsUiState.Success)
    }

    @Test
    fun `loadBuildings handles empty buildings list correctly`() = runTest(testDispatcher) {
        // Given
        val cityId = 1L
        val emptyBuildings = emptyList<Building>()
        `when`(mockRepository.getBuildingsByCity(cityId)).thenReturn(Result.success(emptyBuildings))

        // When
        viewModel.loadBuildings(cityId)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is BuildingsUiState.Success)
        assertTrue((finalState as BuildingsUiState.Success).buildings.isEmpty())
        verify(mockRepository).getBuildingsByCity(cityId)
    }

    @Test
    fun `loadBuildings can be called multiple times with different cityIds`() = runTest(testDispatcher) {
        // Given
        val cityId1 = 1L
        val cityId2 = 2L
        val buildings1 = listOf(Building(
            id = 1L,
            cityId = cityId1,
            address = LocalizedString(ru = "Address1", en = "Address1"),
            name = LocalizedString(ru = "Building1", en = "Building1")
        ))
        val buildings2 = listOf(Building(
            id = 2L,
            cityId = cityId2,
            address = LocalizedString(ru = "Address2", en = "Address2"),
            name = LocalizedString(ru = "Building2", en = "Building2")
        ))

        `when`(mockRepository.getBuildingsByCity(cityId1)).thenReturn(Result.success(buildings1))
        `when`(mockRepository.getBuildingsByCity(cityId2)).thenReturn(Result.success(buildings2))

        // When - First call
        viewModel.loadBuildings(cityId1)
        advanceUntilIdle()

        // Then - First result
        var finalState = viewModel.uiState.value
        assertTrue(finalState is BuildingsUiState.Success)
        assertEquals(buildings1, (finalState as BuildingsUiState.Success).buildings)

        // When - Second call with different city
        viewModel.loadBuildings(cityId2)
        advanceUntilIdle()

        // Then - Second result
        finalState = viewModel.uiState.value
        assertTrue(finalState is BuildingsUiState.Success)
        assertEquals(buildings2, (finalState as BuildingsUiState.Success).buildings)

        verify(mockRepository).getBuildingsByCity(cityId1)
        verify(mockRepository).getBuildingsByCity(cityId2)
    }
}
