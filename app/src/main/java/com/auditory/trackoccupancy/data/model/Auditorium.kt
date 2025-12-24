package com.auditory.trackoccupancy.data.model

import com.google.gson.annotations.SerializedName

data class Auditorium(
    @SerializedName("id") val id: Long,
    @SerializedName("building_id") val buildingId: Long,
    @SerializedName("floor_number") val floorNumber: Int,
    @SerializedName("capacity") val capacity: Int,
    @SerializedName("auditorium_number") val auditoriumNumber: String,
    @SerializedName("type") val type: LocalizedString,
    @SerializedName("image_url") val imageUrl: String?
)
