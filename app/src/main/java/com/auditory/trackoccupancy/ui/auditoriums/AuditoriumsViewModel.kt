package com.auditory.trackoccupancy.ui.auditoriums

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auditory.trackoccupancy.data.model.Auditorium
import com.auditory.trackoccupancy.data.repository.OccupancyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AuditoriumsViewModel @Inject constructor(
    private val repository: OccupancyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuditoriumsUiState>(AuditoriumsUiState.Loading)
    val uiState: StateFlow<AuditoriumsUiState> = _uiState

    fun loadAuditoriums(cityId: Long, buildingId: Long) {
        _uiState.value = AuditoriumsUiState.Loading

        viewModelScope.launch {
            try {
                // Create current timestamp in ISO 8601 format with timezone
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                sdf.timeZone = TimeZone.getDefault()
                val timestamp = sdf.format(Date())

                Log.d("AuditoriumsViewModel", "Requesting building occupancy with timestamp: $timestamp")

                // Load auditoriums and occupancy data in parallel
                val auditoriumsDeferred = async { repository.getAuditoriumsByBuilding(cityId, buildingId) }
                val occupancyDeferred = async { repository.getOccupancyByBuilding(cityId, buildingId, timestamp) }

                val auditoriumsResult = auditoriumsDeferred.await()
                val occupancyResult = occupancyDeferred.await()

                if (auditoriumsResult.isSuccess && occupancyResult.isSuccess) {
                    val auditoriums = auditoriumsResult.getOrNull() ?: emptyList()
                    val occupancyData = occupancyResult.getOrNull() ?: emptyList()

                    // Combine auditorium data with occupancy data
                    val auditoriumWithOccupancy = auditoriums.map { auditorium ->
                        val occupancy = occupancyData.find { it.auditoriumId == auditorium.id }
                        val occupancyPercentage = if (auditorium.capacity > 0 && occupancy != null) {
                            (occupancy.personCount.toFloat() / auditorium.capacity * 100).toInt()
                        } else {
                            0
                        }

                        AuditoriumWithOccupancy(
                            auditorium = auditorium,
                            currentOccupancy = occupancy?.personCount ?: 0,
                            occupancyPercentage = occupancyPercentage,
                            isFresh = occupancy?.isFresh ?: false
                        )
                    }

                    Log.d("AuditoriumsViewModel", "Loaded ${auditoriumWithOccupancy.size} auditoriums with occupancy data")
                    _uiState.value = AuditoriumsUiState.Success(auditoriumWithOccupancy)
                } else {
                    val errorMessage = when {
                        auditoriumsResult.isFailure -> auditoriumsResult.exceptionOrNull()?.message ?: "Failed to load auditoriums"
                        occupancyResult.isFailure -> occupancyResult.exceptionOrNull()?.message ?: "Failed to load occupancy data"
                        else -> "Unknown error occurred"
                    }
                    Log.e("AuditoriumsViewModel", "Error loading data: $errorMessage")
                    _uiState.value = AuditoriumsUiState.Error(errorMessage)
                }
            } catch (e: Exception) {
                Log.e("AuditoriumsViewModel", "Exception loading auditoriums", e)
                _uiState.value = AuditoriumsUiState.Error(e.message ?: "Failed to load data")
            }
        }
    }
}

data class AuditoriumWithOccupancy(
    val auditorium: Auditorium,
    val currentOccupancy: Int,
    val occupancyPercentage: Int,
    val isFresh: Boolean
)

sealed class AuditoriumsUiState {
    object Loading : AuditoriumsUiState()
    data class Success(val auditoriumsWithOccupancy: List<AuditoriumWithOccupancy>) : AuditoriumsUiState()
    data class Error(val message: String) : AuditoriumsUiState()
}
