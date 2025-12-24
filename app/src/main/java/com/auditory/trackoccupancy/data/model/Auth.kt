package com.auditory.trackoccupancy.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("login") val login: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("status") val status: String
)
