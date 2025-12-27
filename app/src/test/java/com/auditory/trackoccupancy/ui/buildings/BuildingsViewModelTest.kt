package com.auditory.trackoccupancy.ui.buildings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.auditory.trackoccupancy.data.model.Building
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
        Dispatchers.setMain(testDispatcher)
        viewModel = BuildingsViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
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
                floorsCount = 5
            ),
            Building(
                id = 2L,
                cityId = cityId,
                address = LocalizedString(ru = "ул. Пушкина 10", en = "Pushkina St. 10"),
                floorsCount = 3
            )
        )
        whenever(mockRepository.getBuildingsByCity(cityId)).thenReturn(Result.success(buildings))

        // When
        viewModel.loadBuildings(cityId)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is BuildingsUiState.Success)
        assertEquals(buildings, (finalState as BuildingsUiState.Success).buildings)
    }

    @Test
    fun `loadBuildings updates uiState to Error when repository returns failure`() = runTest(testDispatcher) {
        // Given
        val cityId = 1L
        val exception = RuntimeException("Network error")
        whenever(mockRepository.getBuildingsByCity(cityId)).thenReturn(Result.failure(exception))

        // When
        viewModel.loadBuildings(cityId)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is BuildingsUiState.Error)
        assertEquals("Network error", (finalState as BuildingsUiState.Error).message)
    }

    @Test
    fun `loadBuildings updates uiState to Error with default message when exception message is null`() = runTest(testDispatcher) {
        // Given
        val cityId = 1L
        val exception = RuntimeException()
        whenever(mockRepository.getBuildingsByCity(cityId)).thenReturn(Result.failure(exception))

        // When
        viewModel.loadBuildings(cityId)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is BuildingsUiState.Error)
        assertEquals("Failed to load buildings", (finalState as BuildingsUiState.Error).message)
    }

    @Test
    fun `loadBuildings transitions through Loading state before Success`() = runTest {
        // Given - Use StandardTestDispatcher to control coroutine execution
        val standardDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(standardDispatcher)
        val vm = BuildingsViewModel(mockRepository)
        
        val cityId = 1L
        val buildings = listOf(Building(
            id = 1L,
            cityId = cityId,
            address = LocalizedString(ru = "Test", en = "Test"),
            floorsCount = 2
        ))
        whenever(mockRepository.getBuildingsByCity(cityId)).thenReturn(Result.success(buildings))

        // Initial state should be Loading
        assertTrue(vm.uiState.value is BuildingsUiState.Loading)

        // When - Start loading (coroutine is suspended)
        vm.loadBuildings(cityId)
        
        // State should still be Loading since coroutine hasn't completed
        assertTrue(vm.uiState.value is BuildingsUiState.Loading)

        // Advance until coroutine completes
        advanceUntilIdle()
        
        // Then - Final state should be Success
        val finalState = vm.uiState.value
        assertTrue(finalState is BuildingsUiState.Success)
    }

    @Test
    fun `loadBuildings handles empty buildings list correctly`() = runTest(testDispatcher) {
        // Given
        val cityId = 1L
        val emptyBuildings = emptyList<Building>()
        whenever(mockRepository.getBuildingsByCity(cityId)).thenReturn(Result.success(emptyBuildings))

        // When
        viewModel.loadBuildings(cityId)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is BuildingsUiState.Success)
        assertTrue((finalState as BuildingsUiState.Success).buildings.isEmpty())
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
            floorsCount = 4
        ))
        val buildings2 = listOf(Building(
            id = 2L,
            cityId = cityId2,
            address = LocalizedString(ru = "Address2", en = "Address2"),
            floorsCount = 6
        ))

        whenever(mockRepository.getBuildingsByCity(cityId1)).thenReturn(Result.success(buildings1))
        whenever(mockRepository.getBuildingsByCity(cityId2)).thenReturn(Result.success(buildings2))

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
    }
}
