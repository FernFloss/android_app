package com.auditory.trackoccupancy.data.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class OccupancyResult(
    @SerializedName("person_count") val personCount: Int,
    @SerializedName("actual_timestamp") val actualTimestamp: Date,
    @SerializedName("is_fresh") val isFresh: Boolean,
    @SerializedName("time_diff_minutes") val timeDiffMinutes: Double,
    @SerializedName("warning") val warning: String? = null
)

data class AuditoriumOccupancyResponse(
    @SerializedName("auditorium_id") val auditoriumId: Long,
    @SerializedName("person_count") val personCount: Int,
    @SerializedName("actual_timestamp") val actualTimestamp: Date,
    @SerializedName("is_fresh") val isFresh: Boolean,
    @SerializedName("time_diff_minutes") val timeDiffMinutes: Double,
    @SerializedName("warning") val warning: String? = null
)

data class AuditoriumStatisticsResponse(
    @SerializedName("stats") val stats: List<AuditoriumStatistics>,
    @SerializedName("warning") val warning: String? = null
)

data class AuditoriumStatistics(
    @SerializedName("hour") val hour: Int,
    @SerializedName("avg_person_count") val avgPersonCount: Double
)