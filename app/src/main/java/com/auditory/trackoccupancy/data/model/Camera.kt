package com.auditory.trackoccupancy.data.model

import com.google.gson.annotations.SerializedName

data class Camera(
    @SerializedName("id") val id: Long,
    @SerializedName("mac") val mac: String,
    @SerializedName("auditorium_id") val auditoriumId: Long? = null
)
