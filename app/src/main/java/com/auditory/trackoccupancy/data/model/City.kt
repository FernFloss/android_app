package com.auditory.trackoccupancy.data.model

import com.google.gson.annotations.SerializedName

data class City(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: LocalizedString
)
