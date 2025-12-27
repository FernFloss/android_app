package com.auditory.trackoccupancy.ui.auth

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.auditory.trackoccupancy.R
import com.auditory.trackoccupancy.TrackOccupancyApplication
import com.auditory.trackoccupancy.databinding.ActivityLoginBinding
import com.auditory.trackoccupancy.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply locale before inflating views
        applyLocale()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupLanguageSwitcher()
        observeViewModel()
    }

    private fun applyLocale() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val languageCode = prefs.getString("language", "en") ?: "en"
        Log.d("LoginActivity", "onCreate - loaded language from prefs: $languageCode")

        val locale = if (languageCode == "en") Locale.ENGLISH else Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
        Log.d("LoginActivity", "Applied locale: $languageCode")
    }

    private fun setupUI() {
        binding.loginButton.setOnClickListener {
            val login = binding.loginEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (login.isEmpty()) {
                binding.loginEditText.error = getString(R.string.login_hint)
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.passwordEditText.error = getString(R.string.password_hint)
                return@setOnClickListener
            }

            viewModel.login(login, password)
        }
    }

    private fun setupLanguageSwitcher() {
        binding.languageFab.setOnClickListener {
            showLanguageDialog()
        }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf(getString(R.string.english), getString(R.string.russian))
        val currentLanguage = getCurrentLanguage()

        Log.d("LoginActivity", "showLanguageDialog - current language: $currentLanguage")

        val checkedItem = if (currentLanguage == "ru") 1 else 0

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.language))
            .setSingleChoiceItems(languages, checkedItem) { dialog, which ->
                val selectedLanguage = if (which == 0) "en" else "ru"
                Log.d("LoginActivity", "Language selected from dialog: $selectedLanguage (index: $which)")
                changeLocale(selectedLanguage)
                (dialog as AlertDialog).dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun getCurrentLanguage(): String {
        val locale = resources.configuration.locales[0]
        return locale.language
    }

    private fun changeLocale(languageCode: String) {
        Log.d("LoginActivity", "changeLocale called with: $languageCode")

        // Apply locale through Application class
        (application as TrackOccupancyApplication).applyLocale(languageCode)

        // Show restart dialog
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.language))
            .setMessage(getString(R.string.language_changed))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                Log.d("LoginActivity", "Restarting app after locale change")
                // Restart the entire app
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finishAffinity()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                Log.d("LoginActivity", "Login state changed to: $state")
                when (state) {
                    is LoginState.Loading -> {
                        binding.loginButton.isEnabled = false
                        binding.loginButton.text = getString(R.string.logging_in)
                    }
                    is LoginState.Success -> {
                        Log.d("LoginActivity", "Login successful, navigating to MainActivity")
                        binding.loginButton.isEnabled = true
                        binding.loginButton.text = getString(R.string.login_button)
                        navigateToMain()
                    }
                    is LoginState.Error -> {
                        binding.loginButton.isEnabled = true
                        binding.loginButton.text = getString(R.string.login_button)
                        Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        binding.loginButton.isEnabled = true
                        binding.loginButton.text = getString(R.string.login_button)
                    }
                }
            }
        }
    }

    private fun navigateToMain() {
        Log.d("LoginActivity", "Starting MainActivity")
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
