package com.auditory.trackoccupancy.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.auditory.trackoccupancy.R
import com.auditory.trackoccupancy.TrackOccupancyApplication
import com.auditory.trackoccupancy.databinding.ActivityMainBinding
import com.auditory.trackoccupancy.ui.auth.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply locale before inflating views
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val languageCode = prefs.getString("language", "en") ?: "en"
        Log.d("MainActivity", "onCreate - loaded language from prefs: $languageCode")
        Log.d("MainActivity", "onCreate - current system locale: ${Locale.getDefault()}")

        if (languageCode != "en") {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            val config = resources.configuration
            config.setLocale(locale)
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)
            Log.d("MainActivity", "Applied locale: $languageCode")
        } else {
            // Ensure English is set as default
            Locale.setDefault(Locale.ENGLISH)
            val config = resources.configuration
            config.setLocale(Locale.ENGLISH)
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)
            Log.d("MainActivity", "Ensured English locale is set")
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAuthentication()
        setupLanguageSwitcher()
    }

    override fun onResume() {
        super.onResume()
        // Refresh resources when activity resumes (after locale change)
        resources.updateConfiguration(resources.configuration, resources.displayMetrics)
    }

    private fun checkAuthentication() {
        Log.d("MainActivity", "Starting authentication check")
        lifecycleScope.launch {
            viewModel.isLoggedIn.collect { isLoggedIn ->
                Log.d("MainActivity", "Authentication check result: isLoggedIn = $isLoggedIn")
                if (!isLoggedIn) {
                    Log.w("MainActivity", "User not logged in, redirecting to LoginActivity")
                    navigateToLogin()
                } else {
                    Log.d("MainActivity", "User is logged in, proceeding with main app flow")
                    // User is logged in, proceed with main app flow
                    setupNavigation()
                }
            }
        }
    }

    private fun setupNavigation() {
        Log.d("MainActivity", "Setting up navigation")
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Setup ActionBar with NavController - only if ActionBar is available
        if (supportActionBar != null) {
            appBarConfiguration = AppBarConfiguration(navController.graph)
            setupActionBarWithNavController(navController, appBarConfiguration)
        } else {
            Log.w("MainActivity", "ActionBar not available, skipping ActionBar setup")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setupLanguageSwitcher() {
        binding.languageFab.setOnClickListener {
            showLanguageDialog()
        }
    }

    private fun showLanguageDialog() {
        // Force refresh resources to ensure correct locale
        resources.updateConfiguration(resources.configuration, resources.displayMetrics)

        val languages = arrayOf(getString(R.string.english), getString(R.string.russian))
        val currentLanguage = getCurrentLanguage()

        Log.d("MainActivity", "showLanguageDialog - current language: $currentLanguage")
        Log.d("MainActivity", "showLanguageDialog - dialog title: ${getString(R.string.language)}")
        Log.d("MainActivity", "showLanguageDialog - languages: ${languages.joinToString()}")
        Log.d("MainActivity", "showLanguageDialog - locale: ${resources.configuration.locales[0]}")

        val checkedItem = if (currentLanguage == "ru") 1 else 0

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.language))
            .setSingleChoiceItems(languages, checkedItem) { dialog, which ->
                val selectedLanguage = if (which == 0) "en" else "ru"
                Log.d("MainActivity", "Language selected from dialog: $selectedLanguage (index: $which)")
                Log.d("MainActivity", "Languages array: ${languages.joinToString()}")
                changeLocale(selectedLanguage)
                (dialog as AlertDialog).dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun getCurrentLanguage(): String {
        val locale = resources.configuration.locales[0]
        val language = locale.language
        Log.d("MainActivity", "getCurrentLanguage - detected language: $language, locale: $locale")
        return language
    }


    private fun changeLocale(languageCode: String) {
        Log.d("MainActivity", "changeLocale called with: $languageCode")

        // Apply locale through Application class
        (application as TrackOccupancyApplication).applyLocale(languageCode)

        // For English, also reset the system default locale
        if (languageCode == "en") {
            Locale.setDefault(Locale.ENGLISH)
            Log.d("MainActivity", "Reset to English locale")
        }

        // Show restart dialog
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.language))
            .setMessage(getString(R.string.language_changed))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                Log.d("MainActivity", "Restarting app after locale change")
                // Restart the entire app
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finishAffinity()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun navigateToLogin() {
        Log.d("MainActivity", "Navigating back to LoginActivity")
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
