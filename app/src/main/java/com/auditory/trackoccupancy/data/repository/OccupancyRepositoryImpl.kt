package com.auditory.trackoccupancy.data.repository

import com.auditory.trackoccupancy.data.api.TrackOccupancyApi
import com.auditory.trackoccupancy.data.model.*
import com.google.gson.Gson
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

    override suspend fun getAuditoriumStatistics(
        cityId: Long,
        buildingId: Long,
        auditoriumId: Long,
        day: String
    ): Result<AuditoriumStatisticsResponse> {
        return try {
            val response = api.getAuditoriumStatistics(cityId, buildingId, auditoriumId, day)
            if (response.isSuccessful) {
                val responseBody = response.body()?.string()
                if (responseBody.isNullOrEmpty()) {
                    return Result.failure(Exception("Empty response body"))
                }

                val gson = Gson()

                // Try to parse as wrapper object first
                try {
                    val wrapperResponse = gson.fromJson(responseBody, AuditoriumStatisticsResponse::class.java)
                    Result.success(wrapperResponse)
                } catch (e: Exception) {
                    // If wrapper parsing fails, try parsing as plain array
                    try {
                        val statsArray = gson.fromJson(responseBody, Array<AuditoriumStatistics>::class.java)
                        Result.success(AuditoriumStatisticsResponse(statsArray.toList(), null))
                    } catch (e2: Exception) {
                        Result.failure(Exception("Failed to parse response: ${e2.message}"))
                    }
                }
            } else if (response.code() == 404) {
                // Treat 404 as no data available (empty response)
                Result.success(AuditoriumStatisticsResponse(emptyList(), "No data available"))
            } else {
                Result.failure(Exception("API call failed: ${response.message()}"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.message}"))
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
