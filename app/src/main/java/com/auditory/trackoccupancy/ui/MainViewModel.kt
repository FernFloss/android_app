package com.auditory.trackoccupancy.ui

import androidx.lifecycle.ViewModel
import com.auditory.trackoccupancy.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val isLoggedIn = authRepository.isLoggedIn()
}
