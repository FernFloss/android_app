package com.auditory.trackoccupancy.ui.occupancy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auditory.trackoccupancy.data.repository.OccupancyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OccupancyGraphViewModel @Inject constructor(
    private val occupancyRepository: OccupancyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OccupancyGraphUiState>(OccupancyGraphUiState.Loading)
    val uiState: StateFlow<OccupancyGraphUiState> = _uiState

    fun loadOccupancyHistory(cityId: Long, buildingId: Long, auditoriumId: Long, date: String = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())) {
        _uiState.value = OccupancyGraphUiState.Loading

        viewModelScope.launch {
            try {
                // First, get the auditorium capacity
                val auditoriumResult = occupancyRepository.getAuditoriumsByBuilding(cityId, buildingId)
                val auditorium = auditoriumResult.getOrNull()?.find { it.id == auditoriumId }

                if (auditorium == null) {
                    _uiState.value = OccupancyGraphUiState.Error("Failed to load auditorium information")
                    return@launch
                }

                val capacity = auditorium.capacity

                // Then get the statistics
                val result = occupancyRepository.getAuditoriumStatistics(cityId, buildingId, auditoriumId, date)

                result.onSuccess { statistics ->
                    // Convert statistics to OccupancyDataPoint format
                    val dataPoints = statistics.map { stat ->
                        // Create timestamp for the selected date at the given hour
                        val calendar = java.util.Calendar.getInstance()
                        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        val selectedDateObj = dateFormat.parse(date)
                        calendar.time = selectedDateObj ?: java.util.Date()
                        calendar.set(java.util.Calendar.HOUR_OF_DAY, stat.hour)
                        calendar.set(java.util.Calendar.MINUTE, 0)
                        calendar.set(java.util.Calendar.SECOND, 0)
                        calendar.set(java.util.Calendar.MILLISECOND, 0)


                        OccupancyDataPoint(
                            timestamp = calendar.timeInMillis,
                            avgPersonCount = stat.avgPersonCount,
                            capacity = capacity
                        )
                    }
                    _uiState.value = OccupancyGraphUiState.Success(dataPoints, System.currentTimeMillis())
                }.onFailure { exception ->
                    _uiState.value = OccupancyGraphUiState.Error("Failed to load occupancy data: ${exception.message}")
                }
            } catch (e: Exception) {
                _uiState.value = OccupancyGraphUiState.Error("Failed to load occupancy data: ${e.message}")
            }
        }
    }

}

sealed class OccupancyGraphUiState {
    object Loading : OccupancyGraphUiState()
    data class Success(val data: List<OccupancyDataPoint>, val lastUpdated: Long?) : OccupancyGraphUiState()
    data class Error(val message: String) : OccupancyGraphUiState()
}

data class OccupancyDataPoint(
    val timestamp: Long,
    val avgPersonCount: Double,
    val capacity: Int
)
