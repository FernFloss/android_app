package com.auditory.trackoccupancy.ui.cities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auditory.trackoccupancy.data.model.City
import com.auditory.trackoccupancy.data.repository.OccupancyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CitiesViewModel @Inject constructor(
    private val occupancyRepository: OccupancyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CitiesUiState>(CitiesUiState.Loading)
    val uiState: StateFlow<CitiesUiState> = _uiState

    fun loadCities() {
        _uiState.value = CitiesUiState.Loading

        viewModelScope.launch {
            val result = occupancyRepository.getCities()
            result.fold(
                onSuccess = { cities ->
                    _uiState.value = CitiesUiState.Success(cities)
                },
                onFailure = { exception ->
                    _uiState.value = CitiesUiState.Error(
                        exception.message ?: "Failed to load cities"
                    )
                }
            )
        }
    }
}

sealed class CitiesUiState {
    object Loading : CitiesUiState()
    data class Success(val cities: List<City>) : CitiesUiState()
    data class Error(val message: String) : CitiesUiState()
}
