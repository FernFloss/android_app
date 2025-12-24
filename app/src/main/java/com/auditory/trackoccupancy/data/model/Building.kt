package com.auditory.trackoccupancy.data.model

import com.google.gson.annotations.SerializedName

data class Building(
    @SerializedName("id") val id: Long,
    @SerializedName("city_id") val cityId: Long,
    @SerializedName("address") val address: LocalizedString,
    @SerializedName("floors_count") val floorsCount: Int
)
