package com.auditory.trackoccupancy.ui.occupancy

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.auditory.trackoccupancy.data.model.*
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
import java.text.SimpleDateFormat
import java.util.*

@ExperimentalCoroutinesApi
class OccupancyGraphViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockRepository: OccupancyRepository

    private lateinit var viewModel: OccupancyGraphViewModel
    private val testDispatcher = UnconfinedTestDispatcher()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = OccupancyGraphViewModel(mockRepository)
    }

    @Test
    fun `initial uiState is Loading`() {
        // When - ViewModel is created

        // Then
        assertTrue(viewModel.uiState.value is OccupancyGraphUiState.Loading)
    }

    @Test
    fun `loadOccupancyHistory updates uiState to Success when data is loaded successfully`() = runTest(testDispatcher) {
        // Given
        val cityId = 1L
        val buildingId = 1L
        val auditoriumId = 1L
        val date = "2025-12-25"
        val capacity = 100

        val auditoriums = listOf(
            Auditorium(id = auditoriumId, buildingId = buildingId, auditoriumNumber = "101", capacity = capacity, floor = 1)
        )

        val statisticsResponse = AuditoriumStatisticsResponse(
            stats = listOf(
                AuditoriumStatistics(hour = 9, avgPersonCount = 25.0),
                AuditoriumStatistics(hour = 10, avgPersonCount = 50.0)
            ),
            warning = null
        )

        `when`(mockRepository.getAuditoriumsByBuilding(cityId, buildingId)).thenReturn(Result.success(auditoriums))
        `when`(mockRepository.getAuditoriumStatistics(cityId, buildingId, auditoriumId, date)).thenReturn(Result.success(statisticsResponse))

        // When
        viewModel.loadOccupancyHistory(cityId, buildingId, auditoriumId, date)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is OccupancyGraphUiState.Success)
        val successState = finalState as OccupancyGraphUiState.Success
        assertEquals(2, successState.data.size)

        // Check first data point
        val firstPoint = successState.data[0]
        assertEquals(25.0, firstPoint.avgPersonCount, 0.001)
        assertEquals(capacity, firstPoint.capacity)

        // Check second data point
        val secondPoint = successState.data[1]
        assertEquals(50.0, secondPoint.avgPersonCount, 0.001)
        assertEquals(capacity, secondPoint.capacity)

        verify(mockRepository).getAuditoriumsByBuilding(cityId, buildingId)
        verify(mockRepository).getAuditoriumStatistics(cityId, buildingId, auditoriumId, date)
    }

    @Test
    fun `loadOccupancyHistory updates uiState to Empty when statistics response is empty`() = runTest(testDispatcher) {
        // Given
        val cityId = 1L
        val buildingId = 1L
        val auditoriumId = 1L
        val date = "2025-12-25"

        val auditoriums = listOf(
            Auditorium(id = auditoriumId, buildingId = buildingId, auditoriumNumber = "101", capacity = 50, floor = 1)
        )

        val emptyStatisticsResponse = AuditoriumStatisticsResponse(stats = emptyList(), warning = "No data")

        `when`(mockRepository.getAuditoriumsByBuilding(cityId, buildingId)).thenReturn(Result.success(auditoriums))
        `when`(mockRepository.getAuditoriumStatistics(cityId, buildingId, auditoriumId, date)).thenReturn(Result.success(emptyStatisticsResponse))

        // When
        viewModel.loadOccupancyHistory(cityId, buildingId, auditoriumId, date)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is OccupancyGraphUiState.Empty)

        verify(mockRepository).getAuditoriumsByBuilding(cityId, buildingId)
        verify(mockRepository).getAuditoriumStatistics(cityId, buildingId, auditoriumId, date)
    }

    @Test
    fun `loadOccupancyHistory updates uiState to Error when auditorium is not found`() = runTest(testDispatcher) {
        // Given
        val cityId = 1L
        val buildingId = 1L
        val auditoriumId = 999L // Non-existent auditorium ID
        val date = "2025-12-25"

        val auditoriums = listOf(
            Auditorium(id = 1L, buildingId = buildingId, auditoriumNumber = "101", capacity = 50, floor = 1)
        )

        `when`(mockRepository.getAuditoriumsByBuilding(cityId, buildingId)).thenReturn(Result.success(auditoriums))

        // When
        viewModel.loadOccupancyHistory(cityId, buildingId, auditoriumId, date)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is OccupancyGraphUiState.Error)
        assertEquals("Failed to load auditorium information", (finalState as OccupancyGraphUiState.Error).message)

        verify(mockRepository).getAuditoriumsByBuilding(cityId, buildingId)
        verify(mockRepository, never()).getAuditoriumStatistics(any(), any(), any(), any())
    }

    @Test
    fun `loadOccupancyHistory updates uiState to Error when getAuditoriumsByBuilding fails`() = runTest(testDispatcher) {
        // Given
        val cityId = 1L
        val buildingId = 1L
        val auditoriumId = 1L
        val date = "2025-12-25"

        val exception = RuntimeException("Network error")
        `when`(mockRepository.getAuditoriumsByBuilding(cityId, buildingId)).thenReturn(Result.failure(exception))

        // When
        viewModel.loadOccupancyHistory(cityId, buildingId, auditoriumId, date)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is OccupancyGraphUiState.Error)
        assertEquals("Failed to load occupancy data: Network error", (finalState as OccupancyGraphUiState.Error).message)

        verify(mockRepository).getAuditoriumsByBuilding(cityId, buildingId)
        verify(mockRepository, never()).getAuditoriumStatistics(any(), any(), any(), any())
    }

    @Test
    fun `loadOccupancyHistory updates uiState to Error when getAuditoriumStatistics fails`() = runTest(testDispatcher) {
        // Given
        val cityId = 1L
        val buildingId = 1L
        val auditoriumId = 1L
        val date = "2025-12-25"

        val auditoriums = listOf(
            Auditorium(id = auditoriumId, buildingId = buildingId, auditoriumNumber = "101", capacity = 50, floor = 1)
        )

        val exception = RuntimeException("Statistics API error")
        `when`(mockRepository.getAuditoriumsByBuilding(cityId, buildingId)).thenReturn(Result.success(auditoriums))
        `when`(mockRepository.getAuditoriumStatistics(cityId, buildingId, auditoriumId, date)).thenReturn(Result.failure(exception))

        // When
        viewModel.loadOccupancyHistory(cityId, buildingId, auditoriumId, date)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is OccupancyGraphUiState.Error)
        assertEquals("Failed to load occupancy data: Statistics API error", (finalState as OccupancyGraphUiState.Error).message)

        verify(mockRepository).getAuditoriumsByBuilding(cityId, buildingId)
        verify(mockRepository).getAuditoriumStatistics(cityId, buildingId, auditoriumId, date)
    }

    @Test
    fun `loadOccupancyHistory sets uiState to Loading immediately when called`() = runTest(testDispatcher) {
        // Given
        val cityId = 1L
        val buildingId = 1L
        val auditoriumId = 1L
        val date = "2025-12-25"

        val auditoriums = listOf(
            Auditorium(id = auditoriumId, buildingId = buildingId, auditoriumNumber = "101", capacity = 50, floor = 1)
        )

        val statisticsResponse = AuditoriumStatisticsResponse(
            stats = listOf(AuditoriumStatistics(hour = 9, avgPersonCount = 25.0)),
            warning = null
        )

        `when`(mockRepository.getAuditoriumsByBuilding(cityId, buildingId)).thenReturn(Result.success(auditoriums))
        `when`(mockRepository.getAuditoriumStatistics(cityId, buildingId, auditoriumId, date)).thenReturn(Result.success(statisticsResponse))

        // When
        viewModel.loadOccupancyHistory(cityId, buildingId, auditoriumId, date)

        // Then - Should be Loading immediately
        assertTrue(viewModel.uiState.value is OccupancyGraphUiState.Loading)

        // And then Success after coroutine completes
        advanceUntilIdle()
        val finalState = viewModel.uiState.value
        assertTrue(finalState is OccupancyGraphUiState.Success)
    }

    @Test
    fun `loadOccupancyHistory uses current date as default when no date provided`() = runTest(testDispatcher) {
        // Given
        val cityId = 1L
        val buildingId = 1L
        val auditoriumId = 1L
        val currentDate = dateFormat.format(Date())

        val auditoriums = listOf(
            Auditorium(id = auditoriumId, buildingId = buildingId, auditoriumNumber = "101", capacity = 50, floor = 1)
        )

        val statisticsResponse = AuditoriumStatisticsResponse(
            stats = listOf(AuditoriumStatistics(hour = 9, avgPersonCount = 25.0)),
            warning = null
        )

        `when`(mockRepository.getAuditoriumsByBuilding(cityId, buildingId)).thenReturn(Result.success(auditoriums))
        `when`(mockRepository.getAuditoriumStatistics(cityId, buildingId, auditoriumId, currentDate)).thenReturn(Result.success(statisticsResponse))

        // When
        viewModel.loadOccupancyHistory(cityId, buildingId, auditoriumId)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is OccupancyGraphUiState.Success)

        verify(mockRepository).getAuditoriumStatistics(cityId, buildingId, auditoriumId, currentDate)
    }

    @Test
    fun `loadOccupancyHistory correctly converts statistics to data points with timestamps`() = runTest(testDispatcher) {
        // Given
        val cityId = 1L
        val buildingId = 1L
        val auditoriumId = 1L
        val date = "2025-12-25"
        val capacity = 100

        val auditoriums = listOf(
            Auditorium(id = auditoriumId, buildingId = buildingId, auditoriumNumber = "101", capacity = capacity, floor = 1)
        )

        val statisticsResponse = AuditoriumStatisticsResponse(
            stats = listOf(
                AuditoriumStatistics(hour = 9, avgPersonCount = 25.0),
                AuditoriumStatistics(hour = 14, avgPersonCount = 75.0)
            ),
            warning = null
        )

        `when`(mockRepository.getAuditoriumsByBuilding(cityId, buildingId)).thenReturn(Result.success(auditoriums))
        `when`(mockRepository.getAuditoriumStatistics(cityId, buildingId, auditoriumId, date)).thenReturn(Result.success(statisticsResponse))

        // When
        viewModel.loadOccupancyHistory(cityId, buildingId, auditoriumId, date)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is OccupancyGraphUiState.Success)
        val successState = finalState as OccupancyGraphUiState.Success
        assertEquals(2, successState.data.size)

        // Verify timestamps are created correctly for the specified date and hours
        val calendar = Calendar.getInstance()
        val selectedDateObj = dateFormat.parse(date)
        calendar.time = selectedDateObj ?: Date()

        // First data point should be at 9:00 on the selected date
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val expectedTimestamp9 = calendar.timeInMillis

        // Second data point should be at 14:00 on the selected date
        calendar.set(Calendar.HOUR_OF_DAY, 14)
        val expectedTimestamp14 = calendar.timeInMillis

        assertEquals(expectedTimestamp9, successState.data[0].timestamp)
        assertEquals(expectedTimestamp14, successState.data[1].timestamp)
    }

    @Test
    fun `OccupancyDataPoint equality works correctly`() {
        // Given
        val point1 = OccupancyDataPoint(timestamp = 1000L, avgPersonCount = 25.0, capacity = 100)
        val point2 = OccupancyDataPoint(timestamp = 1000L, avgPersonCount = 25.0, capacity = 100)
        val point3 = OccupancyDataPoint(timestamp = 2000L, avgPersonCount = 25.0, capacity = 100)

        // Then
        assertEquals(point1, point2)
        assertNotEquals(point1, point3)
    }

    @Test
    fun `OccupancyDataPoint copy works correctly`() {
        // Given
        val original = OccupancyDataPoint(timestamp = 1000L, avgPersonCount = 25.0, capacity = 100)

        // When
        val copied = original.copy(avgPersonCount = 50.0)

        // Then
        assertEquals(1000L, copied.timestamp)
        assertEquals(50.0, copied.avgPersonCount, 0.001)
        assertEquals(100, copied.capacity)
    }
}
