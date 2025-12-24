package com.auditory.trackoccupancy.data.repository

import com.auditory.trackoccupancy.data.api.TrackOccupancyApi
import com.auditory.trackoccupancy.data.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OccupancyRepositoryImpl @Inject constructor(
    private val api: TrackOccupancyApi
) : OccupancyRepository {

    override suspend fun getCities(): Result<List<City>> {
        return safeApiCall { api.getCities() }
    }

    override suspend fun getBuildingsByCity(cityId: Long): Result<List<Building>> {
        return safeApiCall { api.getBuildingsByCity(cityId) }
    }

    override suspend fun getAuditoriumsByBuilding(cityId: Long, buildingId: Long): Result<List<Auditorium>> {
        return safeApiCall { api.getAuditoriumsByBuilding(cityId, buildingId) }
    }

    override suspend fun getOccupancyByBuilding(
        cityId: Long,
        buildingId: Long,
        timestamp: String?
    ): Result<List<AuditoriumOccupancyResponse>> {
        return safeApiCall { api.getOccupancyByBuilding(cityId, buildingId, timestamp) }
    }

    override suspend fun getOccupancyByAuditorium(
        cityId: Long,
        buildingId: Long,
        auditoriumId: Long,
        timestamp: String?
    ): Result<OccupancyResult> {
        return safeApiCall { api.getOccupancyByAuditorium(cityId, buildingId, auditoriumId, timestamp) }
    }

    override suspend fun getCamerasByAuditorium(
        cityId: Long,
        buildingId: Long,
        auditoriumId: Long
    ): Result<List<Camera>> {
        return safeApiCall { api.getCamerasByAuditorium(cityId, buildingId, auditoriumId) }
    }

    override suspend fun getCameraSnapshot(mac: String): Result<ByteArray> {
        return try {
            val response = api.getCameraSnapshot(mac)
            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    Result.success(responseBody.bytes())
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("Failed to get snapshot: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private inline fun <T> safeApiCall(apiCall: () -> Response<T>): Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("API call failed: ${response.message()}"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
