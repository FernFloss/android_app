package com.auditory.trackoccupancy.utils

import androidx.appcompat.app.AppCompatDelegate
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ThemeUtils.
 * 
 * Note: Tests for isDarkTheme() require Android Context which is not available
 * in pure unit tests. Those tests should be in androidTest with Robolectric or
 * on-device instrumented tests.
 * 
 * The tests here verify that the correct modes are set via AppCompatDelegate.
 */
class ThemeUtilsTest {

    @Test
    fun `setThemeMode sets MODE_NIGHT_YES correctly`() {
        // When
        ThemeUtils.setThemeMode(AppCompatDelegate.MODE_NIGHT_YES)

        // Then
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, AppCompatDelegate.getDefaultNightMode())
    }

    @Test
    fun `setThemeMode sets MODE_NIGHT_NO correctly`() {
        // When
        ThemeUtils.setThemeMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Then
        assertEquals(AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.getDefaultNightMode())
    }

    @Test
    fun `setThemeMode sets MODE_NIGHT_FOLLOW_SYSTEM correctly`() {
        // When
        ThemeUtils.setThemeMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        // Then
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, AppCompatDelegate.getDefaultNightMode())
    }

    @Test
    fun `forceDarkTheme sets MODE_NIGHT_YES`() {
        // When
        ThemeUtils.forceDarkTheme()

        // Then
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, ThemeUtils.getCurrentThemeMode())
    }

    @Test
    fun `forceLightTheme sets MODE_NIGHT_NO`() {
        // When
        ThemeUtils.forceLightTheme()

        // Then
        assertEquals(AppCompatDelegate.MODE_NIGHT_NO, ThemeUtils.getCurrentThemeMode())
    }

    @Test
    fun `followSystemTheme sets MODE_NIGHT_FOLLOW_SYSTEM`() {
        // When
        ThemeUtils.followSystemTheme()

        // Then
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, ThemeUtils.getCurrentThemeMode())
    }

    @Test
    fun `getCurrentThemeMode returns current mode`() {
        // Given
        ThemeUtils.setThemeMode(AppCompatDelegate.MODE_NIGHT_YES)

        // When
        val currentMode = ThemeUtils.getCurrentThemeMode()

        // Then
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, currentMode)
    }
}
