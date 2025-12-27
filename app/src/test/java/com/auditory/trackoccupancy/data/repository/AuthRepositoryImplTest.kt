package com.auditory.trackoccupancy.data.repository

import android.content.SharedPreferences
import com.auditory.trackoccupancy.data.api.TrackOccupancyApi
import com.auditory.trackoccupancy.data.model.LoginRequest
import com.auditory.trackoccupancy.data.model.LoginResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import retrofit2.Response
import java.io.IOException

@ExperimentalCoroutinesApi
class AuthRepositoryImplTest {

    @Mock
    private lateinit var mockApi: TrackOccupancyApi

    @Mock
    private lateinit var mockPreferences: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var repository: AuthRepositoryImpl
    private lateinit var closeable: AutoCloseable

    @Before
    fun setUp() {
        closeable = MockitoAnnotations.openMocks(this)
        whenever(mockPreferences.getString(any(), isNull())).thenReturn(null)
        whenever(mockPreferences.edit()).thenReturn(mockEditor)
        whenever(mockEditor.putString(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.remove(any())).thenReturn(mockEditor)
        repository = AuthRepositoryImpl(mockApi, mockPreferences)
    }

    @After
    fun tearDown() {
        closeable.close()
    }

    @Test
    fun `login returns success when API returns ok status`() = runTest {
        // Given
        val request = LoginRequest("admin", "admin")
        val response = LoginResponse("ok")
        whenever(mockApi.login(request)).thenReturn(Response.success(response))

        // When
        val result = repository.login(request)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("ok", result.getOrNull()?.status)
        verify(mockApi).login(request)
    }

    @Test
    fun `login returns success when API returns OK status (uppercase)`() = runTest {
        // Given
        val request = LoginRequest("admin", "admin")
        val response = LoginResponse("OK")
        whenever(mockApi.login(request)).thenReturn(Response.success(response))

        // When
        val result = repository.login(request)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("OK", result.getOrNull()?.status)
        verify(mockApi).login(request)
    }

    @Test
    fun `login returns failure when API returns error status`() = runTest {
        // Given
        val request = LoginRequest("wrong", "credentials")
        val response = LoginResponse("error")
        whenever(mockApi.login(request)).thenReturn(Response.success(response))

        // When
        val result = repository.login(request)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Login failed") ?: false)
        verify(mockApi).login(request)
    }

    @Test
    fun `login returns failure when API returns HTTP error`() = runTest {
        // Given
        val request = LoginRequest("admin", "admin")
        val errorResponse = Response.error<LoginResponse>(401, "Unauthorized".toResponseBody())
        whenever(mockApi.login(request)).thenReturn(errorResponse)

        // When
        val result = repository.login(request)

        // Then
        assertTrue(result.isFailure)
        verify(mockApi).login(request)
    }

    @Test
    fun `login returns failure when API throws network error`() = runTest {
        // Given
        val request = LoginRequest("admin", "admin")
        whenever(mockApi.login(request)).thenAnswer { throw IOException("Network error") }

        // When
        val result = repository.login(request)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
        verify(mockApi).login(request)
    }

    @Test
    fun `login returns failure when API returns null body`() = runTest {
        // Given
        val request = LoginRequest("admin", "admin")
        whenever(mockApi.login(request)).thenReturn(Response.success(null))

        // When
        val result = repository.login(request)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
        verify(mockApi).login(request)
    }

    @Test
    fun `isLoggedIn returns false when no token stored`() = runTest {
        // Given - no token stored (default setup)

        // When
        val isLoggedIn = repository.isLoggedIn().first()

        // Then
        assertFalse(isLoggedIn)
    }

    @Test
    fun `isLoggedIn returns true when token is stored`() = runTest {
        // Given
        whenever(mockPreferences.getString("auth_token", null)).thenReturn("some_token")
        val repositoryWithToken = AuthRepositoryImpl(mockApi, mockPreferences)

        // When
        val isLoggedIn = repositoryWithToken.isLoggedIn().first()

        // Then
        assertTrue(isLoggedIn)
    }

    @Test
    fun `saveAuthToken saves token to preferences`() = runTest {
        // When
        repository.saveAuthToken("test_token")

        // Then
        verify(mockEditor).putString("auth_token", "test_token")
        verify(mockEditor).apply()
    }

    @Test
    fun `saveAuthToken does not save null token`() = runTest {
        // When
        repository.saveAuthToken(null)

        // Then
        verify(mockEditor, never()).putString(eq("auth_token"), any())
    }

    @Test
    fun `saveAuthToken does not save empty token`() = runTest {
        // When
        repository.saveAuthToken("")

        // Then
        verify(mockEditor, never()).putString(eq("auth_token"), any())
    }

    @Test
    fun `clearAuthToken removes token from preferences`() = runTest {
        // When
        repository.clearAuthToken()

        // Then
        verify(mockEditor).remove("auth_token")
        verify(mockEditor).apply()
    }

    @Test
    fun `getAuthToken returns flow with current token`() = runTest {
        // Given
        whenever(mockPreferences.getString("auth_token", null)).thenReturn("stored_token")
        val repositoryWithToken = AuthRepositoryImpl(mockApi, mockPreferences)

        // When
        val token = repositoryWithToken.getAuthToken().first()

        // Then
        assertEquals("stored_token", token)
    }
}
