package com.auditory.trackoccupancy.data.repository

import com.auditory.trackoccupancy.data.api.TrackOccupancyApi
import com.auditory.trackoccupancy.data.model.*
import com.google.gson.Gson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import retrofit2.Response
import java.io.IOException

@ExperimentalCoroutinesApi
class OccupancyRepositoryImplTest {

    @Mock
    private lateinit var mockApi: TrackOccupancyApi

    private lateinit var repository: OccupancyRepositoryImpl
    private val gson = Gson()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        repository = OccupancyRepositoryImpl(mockApi)
    }

    @Test
    fun `getCities returns success result when API call succeeds`() = runTest {
        // Given
        val cities = listOf(
            City(id = 1L, name = LocalizedString(ru = "Москва", en = "Moscow")),
            City(id = 2L, name = LocalizedString(ru = "СПб", en = "Saint Petersburg"))
        )
        whenever(mockApi.getCities()).thenReturn(Response.success(cities))

        // When
        val result = repository.getCities()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(cities, result.getOrNull())
    }

    @Test
    fun `getCities returns failure result when API call fails with network error`() = runTest {
        // Given
        whenever(mockApi.getCities()).thenAnswer { throw IOException("Network error") }

        // When
        val result = repository.getCities()

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Network error") == true)
    }

    @Test
    fun `getCities returns failure result when API returns error response`() = runTest {
        // Given
        val errorResponse = Response.error<List<City>>(404, "Not found".toResponseBody())
        whenever(mockApi.getCities()).thenReturn(errorResponse)

        // When
        val result = repository.getCities()

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("API call failed") == true)
    }

    @Test
    fun `getBuildingsByCity returns success result when API call succeeds`() = runTest {
        // Given
        val buildings = listOf(
            Building(
                id = 1L,
                cityId = 1L,
                address = LocalizedString(ru = "ул. Ленина 1", en = "Lenina St. 1"),
                floorsCount = 5
            )
        )
        whenever(mockApi.getBuildingsByCity(1L)).thenReturn(Response.success(buildings))

        // When
        val result = repository.getBuildingsByCity(1L)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(buildings, result.getOrNull())
    }

    @Test
    fun `getAuditoriumsByBuilding returns success result when API call succeeds`() = runTest {
        // Given
        val auditoriums = listOf(
            Auditorium(
                id = 1L,
                buildingId = 1L,
                auditoriumNumber = "101",
                capacity = 50,
                floorNumber = 1,
                type = LocalizedString(ru = "Лекционная", en = "Lecture Hall"),
                imageUrl = null
            )
        )
        whenever(mockApi.getAuditoriumsByBuilding(1L, 1L)).thenReturn(Response.success(auditoriums))

        // When
        val result = repository.getAuditoriumsByBuilding(1L, 1L)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(auditoriums, result.getOrNull())
    }

    @Test
    fun `getOccupancyByBuilding returns success result when API call succeeds`() = runTest {
        // Given
        val occupancyList = listOf(
            AuditoriumOccupancyResponse(
                auditoriumId = 1L,
                personCount = 25,
                actualTimestamp = java.util.Date(),
                isFresh = true,
                timeDiffMinutes = 2.5,
                warning = null
            )
        )
        whenever(mockApi.getOccupancyByBuilding(1L, 1L, null)).thenReturn(Response.success(occupancyList))

        // When
        val result = repository.getOccupancyByBuilding(1L, 1L)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(occupancyList, result.getOrNull())
    }

    @Test
    fun `getOccupancyByAuditorium returns success result when API call succeeds`() = runTest {
        // Given
        val occupancyResult = OccupancyResult(
            personCount = 30,
            actualTimestamp = java.util.Date(),
            isFresh = true,
            timeDiffMinutes = 1.0,
            warning = null
        )
        whenever(mockApi.getOccupancyByAuditorium(1L, 1L, 1L, null)).thenReturn(Response.success(occupancyResult))

        // When
        val result = repository.getOccupancyByAuditorium(1L, 1L, 1L)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(occupancyResult, result.getOrNull())
    }

    @Test
    fun `getCamerasByAuditorium returns success result when API call succeeds`() = runTest {
        // Given
        val cameras = listOf(
            Camera(
                id = 1L,
                auditoriumId = 1L,
                mac = "AA:BB:CC:DD:EE:FF"
            )
        )
        whenever(mockApi.getCamerasByAuditorium(1L, 1L, 1L)).thenReturn(Response.success(cameras))

        // When
        val result = repository.getCamerasByAuditorium(1L, 1L, 1L)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(cameras, result.getOrNull())
    }

    @Test
    fun `getCameraSnapshot returns success result when API call succeeds`() = runTest {
        // Given
        val imageBytes = "fake_image_data".toByteArray()
        val responseBody = imageBytes.toResponseBody()
        whenever(mockApi.getCameraSnapshot("AA:BB:CC:DD:EE:FF")).thenReturn(Response.success(responseBody))

        // When
        val result = repository.getCameraSnapshot("AA:BB:CC:DD:EE:FF")

        // Then
        assertTrue(result.isSuccess)
        assertArrayEquals(imageBytes, result.getOrNull())
    }

    @Test
    fun `getAuditoriumStatistics handles wrapper response format successfully`() = runTest {
        // Given - API returns wrapper object format
        val responseBody = """{"stats":[{"hour":9,"avg_person_count":10.0}],"warning":"Test warning"}"""
        val mockResponseBody = responseBody.toResponseBody()
        whenever(mockApi.getAuditoriumStatistics(1L, 1L, 1L, "2025-12-25")).thenReturn(Response.success(mockResponseBody))

        // When
        val result = repository.getAuditoriumStatistics(1L, 1L, 1L, "2025-12-25")

        // Then
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertEquals(1, response?.stats?.size)
        assertEquals(9, response?.stats?.get(0)?.hour)
        assertEquals(10.0, response?.stats?.get(0)?.avgPersonCount ?: 0.0, 0.001)
        assertEquals("Test warning", response?.warning)
    }

    @Test
    fun `getAuditoriumStatistics handles plain array response format successfully`() = runTest {
        // Given - API returns plain array format
        val responseBody = """[{"hour":14,"avg_person_count":25.5},{"hour":15,"avg_person_count":20.0}]"""
        val mockResponseBody = responseBody.toResponseBody()
        whenever(mockApi.getAuditoriumStatistics(1L, 1L, 1L, "2025-12-23")).thenReturn(Response.success(mockResponseBody))

        // When
        val result = repository.getAuditoriumStatistics(1L, 1L, 1L, "2025-12-23")

        // Then
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertEquals(2, response?.stats?.size)
        assertEquals(14, response?.stats?.get(0)?.hour)
        assertEquals(25.5, response?.stats?.get(0)?.avgPersonCount ?: 0.0, 0.001)
        assertEquals(15, response?.stats?.get(1)?.hour)
        assertEquals(20.0, response?.stats?.get(1)?.avgPersonCount ?: 0.0, 0.001)
        assertNull(response?.warning)
    }

    @Test
    fun `getAuditoriumStatistics returns empty data for 404 response`() = runTest {
        // Given
        val errorResponse = Response.error<okhttp3.ResponseBody>(404, "Not found".toResponseBody())
        whenever(mockApi.getAuditoriumStatistics(1L, 1L, 1L, "2025-12-25")).thenReturn(errorResponse)

        // When
        val result = repository.getAuditoriumStatistics(1L, 1L, 1L, "2025-12-25")

        // Then
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertTrue(response?.stats?.isEmpty() ?: false)
        assertEquals("No data available", response?.warning)
    }

    @Test
    fun `getAuditoriumStatistics handles invalid JSON response gracefully`() = runTest {
        // Given - Invalid JSON that cannot be parsed as wrapper or array
        // Note: {"invalid": "json"} actually parses to AuditoriumStatisticsResponse with null stats
        // So we need truly unparseable JSON or check that stats is null/empty
        val invalidJson = """not valid json at all"""
        val mockResponseBody = invalidJson.toResponseBody()
        whenever(mockApi.getAuditoriumStatistics(1L, 1L, 1L, "2025-12-25")).thenReturn(Response.success(mockResponseBody))

        // When
        val result = repository.getAuditoriumStatistics(1L, 1L, 1L, "2025-12-25")

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Failed to parse response") == true)
    }

    @Test
    fun `getAuditoriumStatistics returns failure for empty response body`() = runTest {
        // Given
        val emptyBody = "".toResponseBody()
        whenever(mockApi.getAuditoriumStatistics(1L, 1L, 1L, "2025-12-25")).thenReturn(Response.success(emptyBody))

        // When
        val result = repository.getAuditoriumStatistics(1L, 1L, 1L, "2025-12-25")

        // Then
        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getAuditoriumStatistics returns failure for network error`() = runTest {
        // Given
        whenever(mockApi.getAuditoriumStatistics(1L, 1L, 1L, "2025-12-25")).thenAnswer { throw IOException("Network timeout") }

        // When
        val result = repository.getAuditoriumStatistics(1L, 1L, 1L, "2025-12-25")

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Network error") == true)
        assertTrue(result.exceptionOrNull()?.message?.contains("Network timeout") == true)
    }
}
