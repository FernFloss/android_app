package com.auditory.trackoccupancy.data.model

import org.junit.Assert.*
import org.junit.Test
import java.util.*

class OccupancyTest {

    @Test
    fun `AuditoriumStatistics creates correctly with valid data`() {
        // Given
        val hour = 14
        val avgPersonCount = 25.5

        // When
        val statistics = AuditoriumStatistics(hour = hour, avgPersonCount = avgPersonCount)

        // Then
        assertEquals(hour, statistics.hour)
        assertEquals(avgPersonCount, statistics.avgPersonCount, 0.001)
    }

    @Test
    fun `AuditoriumStatisticsResponse creates correctly with stats list`() {
        // Given
        val stats = listOf(
            AuditoriumStatistics(hour = 9, avgPersonCount = 10.0),
            AuditoriumStatistics(hour = 10, avgPersonCount = 15.5)
        )
        val warning = "Test warning"

        // When
        val response = AuditoriumStatisticsResponse(stats = stats, warning = warning)

        // Then
        assertEquals(stats, response.stats)
        assertEquals(warning, response.warning)
        assertEquals(2, response.stats.size)
    }

    @Test
    fun `AuditoriumStatisticsResponse creates correctly without warning`() {
        // Given
        val stats = listOf(AuditoriumStatistics(hour = 12, avgPersonCount = 8.0))

        // When
        val response = AuditoriumStatisticsResponse(stats = stats, warning = null)

        // Then
        assertEquals(stats, response.stats)
        assertNull(response.warning)
    }

    @Test
    fun `AuditoriumStatisticsResponse creates correctly with empty stats list`() {
        // When
        val response = AuditoriumStatisticsResponse(stats = emptyList(), warning = "No data")

        // Then
        assertTrue(response.stats.isEmpty())
        assertEquals("No data", response.warning)
    }

    @Test
    fun `OccupancyResult creates correctly with all fields`() {
        // Given
        val personCount = 42
        val actualTimestamp = Date()
        val isFresh = true
        val timeDiffMinutes = 5.5
        val warning = "Test warning"

        // When
        val result = OccupancyResult(
            personCount = personCount,
            actualTimestamp = actualTimestamp,
            isFresh = isFresh,
            timeDiffMinutes = timeDiffMinutes,
            warning = warning
        )

        // Then
        assertEquals(personCount, result.personCount)
        assertEquals(actualTimestamp, result.actualTimestamp)
        assertEquals(isFresh, result.isFresh)
        assertEquals(timeDiffMinutes, result.timeDiffMinutes, 0.001)
        assertEquals(warning, result.warning)
    }

    @Test
    fun `OccupancyResult creates correctly with null warning`() {
        // Given
        val personCount = 10
        val actualTimestamp = Date()
        val isFresh = false
        val timeDiffMinutes = 15.0

        // When
        val result = OccupancyResult(
            personCount = personCount,
            actualTimestamp = actualTimestamp,
            isFresh = isFresh,
            timeDiffMinutes = timeDiffMinutes,
            warning = null
        )

        // Then
        assertEquals(personCount, result.personCount)
        assertEquals(actualTimestamp, result.actualTimestamp)
        assertEquals(isFresh, result.isFresh)
        assertEquals(timeDiffMinutes, result.timeDiffMinutes, 0.001)
        assertNull(result.warning)
    }

    @Test
    fun `AuditoriumOccupancyResponse creates correctly with all fields`() {
        // Given
        val auditoriumId = 123L
        val personCount = 35
        val actualTimestamp = Date()
        val isFresh = true
        val timeDiffMinutes = 2.5
        val warning = "Auditorium occupancy warning"

        // When
        val response = AuditoriumOccupancyResponse(
            auditoriumId = auditoriumId,
            personCount = personCount,
            actualTimestamp = actualTimestamp,
            isFresh = isFresh,
            timeDiffMinutes = timeDiffMinutes,
            warning = warning
        )

        // Then
        assertEquals(auditoriumId, response.auditoriumId)
        assertEquals(personCount, response.personCount)
        assertEquals(actualTimestamp, response.actualTimestamp)
        assertEquals(isFresh, response.isFresh)
        assertEquals(timeDiffMinutes, response.timeDiffMinutes, 0.001)
        assertEquals(warning, response.warning)
    }

    @Test
    fun `AuditoriumStatistics equality works correctly`() {
        // Given
        val stat1 = AuditoriumStatistics(hour = 14, avgPersonCount = 25.0)
        val stat2 = AuditoriumStatistics(hour = 14, avgPersonCount = 25.0)
        val stat3 = AuditoriumStatistics(hour = 15, avgPersonCount = 25.0)

        // Then
        assertEquals(stat1, stat2)
        assertNotEquals(stat1, stat3)
    }

    @Test
    fun `AuditoriumStatisticsResponse equality works correctly`() {
        // Given
        val stats1 = listOf(AuditoriumStatistics(hour = 9, avgPersonCount = 10.0))
        val stats2 = listOf(AuditoriumStatistics(hour = 9, avgPersonCount = 10.0))
        val response1 = AuditoriumStatisticsResponse(stats = stats1, warning = "Warning")
        val response2 = AuditoriumStatisticsResponse(stats = stats2, warning = "Warning")
        val response3 = AuditoriumStatisticsResponse(stats = stats1, warning = null)

        // Then
        assertEquals(response1, response2)
        assertNotEquals(response1, response3)
    }

    @Test
    fun `AuditoriumStatistics copy works correctly`() {
        // Given
        val original = AuditoriumStatistics(hour = 10, avgPersonCount = 20.0)

        // When
        val copied = original.copy(avgPersonCount = 25.0)

        // Then
        assertEquals(10, copied.hour)
        assertEquals(25.0, copied.avgPersonCount, 0.001)
    }

    @Test
    fun `AuditoriumStatisticsResponse copy works correctly`() {
        // Given
        val original = AuditoriumStatisticsResponse(
            stats = listOf(AuditoriumStatistics(hour = 12, avgPersonCount = 15.0)),
            warning = "Original warning"
        )

        // When
        val copied = original.copy(warning = "Updated warning")

        // Then
        assertEquals(original.stats, copied.stats)
        assertEquals("Updated warning", copied.warning)
    }
}
