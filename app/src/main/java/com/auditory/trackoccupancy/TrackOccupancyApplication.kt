package com.auditory.trackoccupancy

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import dagger.hilt.android.HiltAndroidApp
import java.util.*

@HiltAndroidApp
class TrackOccupancyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Locale is now applied in MainActivity to ensure proper resource loading
    }

    fun applyLocale(languageCode: String) {
        android.util.Log.d("TrackOccupancyApplication", "applyLocale called with: $languageCode")

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        // Apply the locale to the application context
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)

        // Save the preference
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("language", languageCode).apply()

        android.util.Log.d("TrackOccupancyApplication", "Locale applied successfully")
    }
}
