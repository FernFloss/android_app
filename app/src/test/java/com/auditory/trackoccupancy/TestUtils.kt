package com.auditory.trackoccupancy

import com.auditory.trackoccupancy.data.model.*
import java.util.*

/**
 * Utility class for creating test data
 */
object TestUtils {

    fun createTestCity(
        id: Long = 1L,
        nameRu: String = "Москва",
        nameEn: String = "Moscow"
    ) = City(
        id = id,
        name = LocalizedString(ru = nameRu, en = nameEn)
    )

    fun createTestBuilding(
        id: Long = 1L,
        cityId: Long = 1L,
        addressRu: String = "ул. Ленина 1",
        addressEn: String = "Lenina St. 1",
        floorsCount: Int = 5
    ) = Building(
        id = id,
        cityId = cityId,
        address = LocalizedString(ru = addressRu, en = addressEn),
        floorsCount = floorsCount
    )

    fun createTestAuditorium(
        id: Long = 1L,
        buildingId: Long = 1L,
        auditoriumNumber: String = "101",
        capacity: Int = 50,
        floorNumber: Int = 1,
        typeRu: String = "Лекционная",
        typeEn: String = "Lecture",
        imageUrl: String? = null
    ) = Auditorium(
        id = id,
        buildingId = buildingId,
        auditoriumNumber = auditoriumNumber,
        capacity = capacity,
        floorNumber = floorNumber,
        type = LocalizedString(ru = typeRu, en = typeEn),
        imageUrl = imageUrl
    )

    fun createTestCamera(
        id: Long = 1L,
        auditoriumId: Long = 1L,
        mac: String = "AA:BB:CC:DD:EE:FF",
        nameRu: String = "Камера 1",
        nameEn: String = "Camera 1"
    ) = Camera(
        id = id,
        auditoriumId = auditoriumId,
        mac = mac,
        name = LocalizedString(ru = nameRu, en = nameEn)
    )

    fun createTestAuditoriumStatistics(
        hour: Int = 9,
        avgPersonCount: Double = 25.0
    ) = AuditoriumStatistics(
        hour = hour,
        avgPersonCount = avgPersonCount
    )

    fun createTestAuditoriumStatisticsResponse(
        stats: List<AuditoriumStatistics> = listOf(createTestAuditoriumStatistics()),
        warning: String? = null
    ) = AuditoriumStatisticsResponse(
        stats = stats,
        warning = warning
    )

    fun createTestOccupancyResult(
        personCount: Int = 30,
        actualTimestamp: Date = Date(),
        isFresh: Boolean = true,
        timeDiffMinutes: Double = 5.0,
        warning: String? = null
    ) = OccupancyResult(
        personCount = personCount,
        actualTimestamp = actualTimestamp,
        isFresh = isFresh,
        timeDiffMinutes = timeDiffMinutes,
        warning = warning
    )

    fun createTestAuditoriumOccupancyResponse(
        auditoriumId: Long = 1L,
        personCount: Int = 25,
        actualTimestamp: Date = Date(),
        isFresh: Boolean = true,
        timeDiffMinutes: Double = 2.5,
        warning: String? = null
    ) = AuditoriumOccupancyResponse(
        auditoriumId = auditoriumId,
        personCount = personCount,
        actualTimestamp = actualTimestamp,
        isFresh = isFresh,
        timeDiffMinutes = timeDiffMinutes,
        warning = warning
    )

    fun createTestOccupancyDataPoint(
        timestamp: Long = System.currentTimeMillis(),
        avgPersonCount: Double = 25.0,
        capacity: Int = 100
    ) = com.auditory.trackoccupancy.ui.occupancy.OccupancyDataPoint(
        timestamp = timestamp,
        avgPersonCount = avgPersonCount,
        capacity = capacity
    )

    /**
     * Creates a list of test cities
     */
    fun createTestCities(count: Int = 3) = (1..count).map { index ->
        createTestCity(
            id = index.toLong(),
            nameRu = "Город $index",
            nameEn = "City $index"
        )
    }

    /**
     * Creates a list of test buildings for a city
     */
    fun createTestBuildings(cityId: Long = 1L, count: Int = 2) = (1..count).map { index ->
        createTestBuilding(
            id = index.toLong(),
            cityId = cityId,
            addressRu = "Адрес $index",
            addressEn = "Address $index",
            floorsCount = 3 + index
        )
    }

    /**
     * Creates a list of test auditoriums for a building
     */
    fun createTestAuditoriums(buildingId: Long = 1L, count: Int = 3) = (1..count).map { index ->
        createTestAuditorium(
            id = index.toLong(),
            buildingId = buildingId,
            auditoriumNumber = "${100 + index}",
            capacity = 30 + index * 10,
            floorNumber = index,
            typeRu = "Лекционная",
            typeEn = "Lecture",
            imageUrl = "https://example.com/image$index.jpg"
        )
    }

    /**
     * Creates a list of test cameras for an auditorium
     */
    fun createTestCameras(auditoriumId: Long = 1L, count: Int = 2) = (1..count).map { index ->
        createTestCamera(
            id = index.toLong(),
            auditoriumId = auditoriumId,
            mac = "AA:BB:CC:DD:EE:${index.toString().padStart(2, '0')}",
            nameRu = "Камера $index",
            nameEn = "Camera $index"
        )
    }

    /**
     * Creates test statistics data for multiple hours
     */
    fun createTestStatisticsData(hours: List<Int> = listOf(9, 10, 11, 12, 13, 14)) =
        hours.map { hour ->
            createTestAuditoriumStatistics(
                hour = hour,
                avgPersonCount = (Math.random() * 50).toInt().toDouble()
            )
        }
}
