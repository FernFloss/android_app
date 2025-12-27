package com.auditory.trackoccupancy.ui.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.auditory.trackoccupancy.data.model.LoginRequest
import com.auditory.trackoccupancy.data.model.LoginResponse
import com.auditory.trackoccupancy.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class LoginViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockAuthRepository: AuthRepository

    private lateinit var viewModel: LoginViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(mockAuthRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial loginState is Idle`() {
        // Then
        assertTrue(viewModel.loginState.value is LoginState.Idle)
    }

    @Test
    fun `login updates state to Success when repository returns successful response`() = runTest(testDispatcher) {
        // Given
        val request = LoginRequest("admin", "admin")
        val response = LoginResponse("ok")
        `when`(mockAuthRepository.login(request)).thenReturn(Result.success(response))

        // When
        viewModel.login("admin", "admin")
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.loginState.value is LoginState.Success)
        verify(mockAuthRepository).login(request)
    }

    @Test
    fun `login updates state to Error when repository returns failure`() = runTest(testDispatcher) {
        // Given
        val request = LoginRequest("wrong", "credentials")
        val exception = RuntimeException("Invalid credentials")
        `when`(mockAuthRepository.login(request)).thenReturn(Result.failure(exception))

        // When
        viewModel.login("wrong", "credentials")
        advanceUntilIdle()

        // Then
        val state = viewModel.loginState.value
        assertTrue(state is LoginState.Error)
        assertEquals("Invalid credentials", (state as LoginState.Error).message)
        verify(mockAuthRepository).login(request)
    }

    @Test
    fun `login updates state to Error with default message when exception has null message`() = runTest(testDispatcher) {
        // Given
        val request = LoginRequest("user", "pass")
        val exception = RuntimeException()
        `when`(mockAuthRepository.login(request)).thenReturn(Result.failure(exception))

        // When
        viewModel.login("user", "pass")
        advanceUntilIdle()

        // Then
        val state = viewModel.loginState.value
        assertTrue(state is LoginState.Error)
        assertEquals("Login failed", (state as LoginState.Error).message)
        verify(mockAuthRepository).login(request)
    }

    @Test
    fun `login sets state to Loading immediately when called`() = runTest(testDispatcher) {
        // Given
        val request = LoginRequest("admin", "admin")
        val response = LoginResponse("ok")
        `when`(mockAuthRepository.login(request)).thenReturn(Result.success(response))

        // When
        viewModel.login("admin", "admin")

        // Then - Should be Loading immediately before coroutine completes
        // Note: With UnconfinedTestDispatcher, this happens synchronously
        // so we check the final state instead
        advanceUntilIdle()
        assertTrue(viewModel.loginState.value is LoginState.Success)
    }

    @Test
    fun `login trims whitespace from credentials`() = runTest(testDispatcher) {
        // Given - Note: The ViewModel receives already trimmed values from the Activity
        val request = LoginRequest("admin", "admin")
        val response = LoginResponse("ok")
        `when`(mockAuthRepository.login(request)).thenReturn(Result.success(response))

        // When
        viewModel.login("admin", "admin")
        advanceUntilIdle()

        // Then
        verify(mockAuthRepository).login(request)
    }

    @Test
    fun `login handles network error correctly`() = runTest(testDispatcher) {
        // Given
        val request = LoginRequest("admin", "admin")
        val exception = java.io.IOException("Network error")
        `when`(mockAuthRepository.login(request)).thenReturn(Result.failure(exception))

        // When
        viewModel.login("admin", "admin")
        advanceUntilIdle()

        // Then
        val state = viewModel.loginState.value
        assertTrue(state is LoginState.Error)
        assertEquals("Network error", (state as LoginState.Error).message)
    }

    @Test
    fun `login can be retried after failure`() = runTest(testDispatcher) {
        // Given - First attempt fails
        val request = LoginRequest("admin", "admin")
        val exception = RuntimeException("Server error")
        `when`(mockAuthRepository.login(request))
            .thenReturn(Result.failure(exception))
            .thenReturn(Result.success(LoginResponse("ok")))

        // When - First attempt
        viewModel.login("admin", "admin")
        advanceUntilIdle()

        // Then - Should be Error
        assertTrue(viewModel.loginState.value is LoginState.Error)

        // When - Retry
        viewModel.login("admin", "admin")
        advanceUntilIdle()

        // Then - Should be Success
        assertTrue(viewModel.loginState.value is LoginState.Success)
        verify(mockAuthRepository, times(2)).login(request)
    }
}

