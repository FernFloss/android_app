package com.auditory.trackoccupancy.data.repository

import android.content.SharedPreferences
import android.util.Log
import com.auditory.trackoccupancy.data.api.TrackOccupancyApi
import com.auditory.trackoccupancy.data.model.LoginRequest
import com.auditory.trackoccupancy.data.model.LoginResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: TrackOccupancyApi,
    private val preferences: SharedPreferences
) : AuthRepository {

    private val _authToken = MutableStateFlow(getStoredToken())

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
    }

    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            Log.d("AuthRepository", "Making login API call for user: ${request.login}")
            val response = api.login(request)
            Log.d("AuthRepository", "Login API response: ${response.code()} ${response.message()}")
            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    Log.d("AuthRepository", "Login response body: status=${loginResponse.status}")
                    if (loginResponse.status == "OK") {
                        Log.d("AuthRepository", "Login successful, status: ${loginResponse.status}")
                        // Since backend doesn't provide token, save a dummy token to indicate login success
                        saveAuthToken("logged_in_${System.currentTimeMillis()}")
                        Result.success(loginResponse)
                    } else {
                        Log.e("AuthRepository", "Login failed with status: ${loginResponse.status}")
                        Result.failure(Exception("Login failed: ${loginResponse.status}"))
                    }
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Log.e("AuthRepository", "Login failed with code ${response.code()}: ${response.message()}")
                // Try to log error body if available
                response.errorBody()?.string()?.let { errorBody ->
                    Log.e("AuthRepository", "Error response body: $errorBody")
                }
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login API call failed", e)
            Result.failure(e)
        }
    }

    override fun getAuthToken(): Flow<String?> = _authToken

    override suspend fun saveAuthToken(token: String?) {
        if (token.isNullOrEmpty()) {
            Log.e("AuthRepository", "Cannot save null or empty token")
            return
        }
        Log.d("AuthRepository", "Saving auth token: ${token.take(20)}...")
        preferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
        _authToken.value = token
        Log.d("AuthRepository", "Auth token saved, _authToken.value is now: ${_authToken.value?.take(20)}...")
    }

    override suspend fun clearAuthToken() {
        preferences.edit().remove(KEY_AUTH_TOKEN).apply()
        _authToken.value = null
    }

    override fun isLoggedIn(): Flow<Boolean> = _authToken.map { !it.isNullOrEmpty() }

    private fun getStoredToken(): String? {
        return preferences.getString(KEY_AUTH_TOKEN, null)
    }
}
