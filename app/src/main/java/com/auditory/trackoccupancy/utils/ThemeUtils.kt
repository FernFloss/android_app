package com.auditory.trackoccupancy.utils

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

/**
 * Utility class for theme-related operations
 */
object ThemeUtils {

    /**
     * Check if the current theme is dark mode
     */
    fun isDarkTheme(context: Context): Boolean {
        val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

    /**
     * Get the current theme mode
     */
    fun getCurrentThemeMode(): Int {
        return AppCompatDelegate.getDefaultNightMode()
    }

    /**
     * Set the theme mode
     */
    fun setThemeMode(mode: Int) {
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    /**
     * Follow system theme (default behavior)
     */
    fun followSystemTheme() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    /**
     * Force light theme
     */
    fun forceLightTheme() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    /**
     * Force dark theme
     */
    fun forceDarkTheme() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
}
