package com.auditory.trackoccupancy.data.repository

import com.auditory.trackoccupancy.data.model.LoginRequest
import com.auditory.trackoccupancy.data.model.LoginResponse
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<LoginResponse>
    fun getAuthToken(): Flow<String?>
    suspend fun saveAuthToken(token: String?)
    suspend fun clearAuthToken()
    fun isLoggedIn(): Flow<Boolean>
}
