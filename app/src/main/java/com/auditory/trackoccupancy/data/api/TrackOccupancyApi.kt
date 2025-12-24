package com.auditory.trackoccupancy.data.api

import com.auditory.trackoccupancy.data.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface TrackOccupancyApi {

    // Authentication
    @POST("/v1/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // Cities
    @GET("/v1/cities")
    suspend fun getCities(): Response<List<City>>

    // Buildings
    @GET("/v1/cities/{cityId}/buildings")
    suspend fun getBuildingsByCity(@Path("cityId") cityId: Long): Response<List<Building>>

    // Auditoriums
    @GET("/v1/cities/{cityId}/buildings/{buildingId}/auditories")
    suspend fun getAuditoriumsByBuilding(
        @Path("cityId") cityId: Long,
        @Path("buildingId") buildingId: Long
    ): Response<List<Auditorium>>

    // Occupancy
    @GET("/v1/cities/{cityId}/buildings/{buildingId}/auditories/occupancy")
    suspend fun getOccupancyByBuilding(
        @Path("cityId") cityId: Long,
        @Path("buildingId") buildingId: Long,
        @Query("timestamp") timestamp: String? = null
    ): Response<List<AuditoriumOccupancyResponse>>

    @GET("/v1/cities/{cityId}/buildings/{buildingId}/auditories/{auditoriumId}/occupancy")
    suspend fun getOccupancyByAuditorium(
        @Path("cityId") cityId: Long,
        @Path("buildingId") buildingId: Long,
        @Path("auditoriumId") auditoriumId: Long,
        @Query("timestamp") timestamp: String? = null
    ): Response<OccupancyResult>

    // Cameras
    @GET("/v1/cities/{cityId}/buildings/{buildingId}/auditories/{auditoriumId}/cameras")
    suspend fun getCamerasByAuditorium(
        @Path("cityId") cityId: Long,
        @Path("buildingId") buildingId: Long,
        @Path("auditoriumId") auditoriumId: Long
    ): Response<List<Camera>>

    // Camera snapshot
    @GET("/api/snapshot")
    suspend fun getCameraSnapshot(@Query("mac") mac: String): Response<ResponseBody>
}
