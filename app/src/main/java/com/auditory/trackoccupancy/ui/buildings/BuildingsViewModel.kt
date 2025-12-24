package com.auditory.trackoccupancy.ui.buildings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auditory.trackoccupancy.data.model.Building
import com.auditory.trackoccupancy.data.repository.OccupancyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BuildingsViewModel @Inject constructor(
    private val repository: OccupancyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BuildingsUiState>(BuildingsUiState.Loading)
    val uiState: StateFlow<BuildingsUiState> = _uiState

    fun loadBuildings(cityId: Long) {
        _uiState.value = BuildingsUiState.Loading

        viewModelScope.launch {
            val result = repository.getBuildingsByCity(cityId)
            result.fold(
                onSuccess = { buildings ->
                    _uiState.value = BuildingsUiState.Success(buildings)
                },
                onFailure = { exception ->
                    _uiState.value = BuildingsUiState.Error(
                        exception.message ?: "Failed to load buildings"
                    )
                }
            )
        }
    }
}

sealed class BuildingsUiState {
    object Loading : BuildingsUiState()
    data class Success(val buildings: List<Building>) : BuildingsUiState()
    data class Error(val message: String) : BuildingsUiState()
}
