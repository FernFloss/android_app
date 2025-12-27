package com.auditory.trackoccupancy.data.repository

import com.auditory.trackoccupancy.data.api.TrackOccupancyApi
import com.auditory.trackoccupancy.data.model.AuditoriumStatisticsResponse
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
class OccupancyRepositoryIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var api: TrackOccupancyApi
    private lateinit var repository: OccupancyRepositoryImpl

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(TrackOccupancyApi::class.java)
        repository = OccupancyRepositoryImpl(api)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `getAuditoriumStatistics handles wrapper response format correctly`() = runTest {
        // Given - Mock server returns wrapper object format
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""{"stats":[{"hour":9,"avg_person_count":25.5},{"hour":10,"avg_person_count":30.0}],"warning":"Test warning"}""")
            .addHeader("Content-Type", "application/json")

        mockWebServer.enqueue(mockResponse)

        // When
        val result = repository.getAuditoriumStatistics(1L, 1L, 1L, "2025-12-25")

        // Then
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertEquals(2, response?.stats?.size)
        assertEquals(9, response?.stats?.get(0)?.hour)
        assertEquals(25.5, response?.stats?.get(0)?.avgPersonCount ?: 0.0, 0.001)
        assertEquals(10, response?.stats?.get(1)?.hour)
        assertEquals(30.0, response?.stats?.get(1)?.avgPersonCount ?: 0.0, 0.001)
        assertEquals("Test warning", response?.warning)

        // Verify request was made
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("GET", recordedRequest.method)
        assertTrue(recordedRequest.path?.contains("/v1/cities/1/buildings/1/auditories/1/statistics") ?: false)
        assertTrue(recordedRequest.path?.contains("day=2025-12-25") ?: false)
    }

    @Test
    fun `getAuditoriumStatistics handles plain array response format correctly`() = runTest {
        // Given - Mock server returns plain array format
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""[{"hour":14,"avg_person_count":45.0},{"hour":15,"avg_person_count":50.0}]""")
            .addHeader("Content-Type", "application/json")

        mockWebServer.enqueue(mockResponse)

        // When
        val result = repository.getAuditoriumStatistics(2L, 3L, 5L, "2025-12-23")

        // Then
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertEquals(2, response?.stats?.size)
        assertEquals(14, response?.stats?.get(0)?.hour)
        assertEquals(45.0, response?.stats?.get(0)?.avgPersonCount ?: 0.0, 0.001)
        assertEquals(15, response?.stats?.get(1)?.hour)
        assertEquals(50.0, response?.stats?.get(1)?.avgPersonCount ?: 0.0, 0.001)
        assertNull(response?.warning)

        // Verify request was made
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("GET", recordedRequest.method)
        assertTrue(recordedRequest.path?.contains("/v1/cities/2/buildings/3/auditories/5/statistics") ?: false)
        assertTrue(recordedRequest.path?.contains("day=2025-12-23") ?: false)
    }

    @Test
    fun `getAuditoriumStatistics handles empty stats array correctly`() = runTest {
        // Given - Mock server returns empty stats array
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""{"stats":[],"warning":"No statistics found for this auditorium on the selected day"}""")
            .addHeader("Content-Type", "application/json")

        mockWebServer.enqueue(mockResponse)

        // When
        val result = repository.getAuditoriumStatistics(1L, 1L, 1L, "2025-12-25")

        // Then
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertTrue(response?.stats?.isEmpty() ?: false)
        assertEquals("No statistics found for this auditorium on the selected day", response?.warning)
    }

    @Test
    fun `getAuditoriumStatistics handles 404 response as empty data`() = runTest {
        // Given - Mock server returns 404
        val mockResponse = MockResponse()
            .setResponseCode(404)
            .setBody("Not Found")
            .addHeader("Content-Type", "text/plain")

        mockWebServer.enqueue(mockResponse)

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
    fun `getAuditoriumStatistics handles server error correctly`() = runTest {
        // Given - Mock server returns 500 error
        val mockResponse = MockResponse()
            .setResponseCode(500)
            .setBody("Internal Server Error")
            .addHeader("Content-Type", "text/plain")

        mockWebServer.enqueue(mockResponse)

        // When
        val result = repository.getAuditoriumStatistics(1L, 1L, 1L, "2025-12-25")

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("API call failed") ?: false)
    }

    @Test
    fun `getAuditoriumStatistics handles network timeout correctly`() = runTest {
        // Given - Mock server is slow (longer than our 1 second timeout)
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""{"stats":[{"hour":9,"avg_person_count":10.0}]}""")
            .addHeader("Content-Type", "application/json")
            .setBodyDelay(2, TimeUnit.SECONDS) // Delay longer than timeout

        mockWebServer.enqueue(mockResponse)

        // When
        val result = repository.getAuditoriumStatistics(1L, 1L, 1L, "2025-12-25")

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Network error") ?: false)
    }

    @Test
    fun `getAuditoriumStatistics handles invalid JSON response correctly`() = runTest {
        // Given - Mock server returns truly invalid/unparseable JSON
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""not valid json at all""")
            .addHeader("Content-Type", "application/json")

        mockWebServer.enqueue(mockResponse)

        // When
        val result = repository.getAuditoriumStatistics(1L, 1L, 1L, "2025-12-25")

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Failed to parse response") ?: false)
    }

    @Test
    fun `getAuditoriumStatistics handles empty response body correctly`() = runTest {
        // Given - Mock server returns empty body
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("")
            .addHeader("Content-Type", "application/json")

        mockWebServer.enqueue(mockResponse)

        // When
        val result = repository.getAuditoriumStatistics(1L, 1L, 1L, "2025-12-25")

        // Then
        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }
}
