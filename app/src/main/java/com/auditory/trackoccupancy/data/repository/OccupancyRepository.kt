package com.auditory.trackoccupancy.data.repository

import com.auditory.trackoccupancy.data.model.*

interface OccupancyRepository {
    suspend fun getCities(): Result<List<City>>
    suspend fun getBuildingsByCity(cityId: Long): Result<List<Building>>
    suspend fun getAuditoriumsByBuilding(cityId: Long, buildingId: Long): Result<List<Auditorium>>
    suspend fun getOccupancyByBuilding(cityId: Long, buildingId: Long, timestamp: String? = null): Result<List<AuditoriumOccupancyResponse>>
    suspend fun getOccupancyByAuditorium(cityId: Long, buildingId: Long, auditoriumId: Long, timestamp: String? = null): Result<OccupancyResult>
    suspend fun getCamerasByAuditorium(cityId: Long, buildingId: Long, auditoriumId: Long): Result<List<Camera>>
    suspend fun getCameraSnapshot(mac: String): Result<ByteArray>
    suspend fun getAuditoriumStatistics(cityId: Long, buildingId: Long, auditoriumId: Long, day: String): Result<AuditoriumStatisticsResponse>
}
