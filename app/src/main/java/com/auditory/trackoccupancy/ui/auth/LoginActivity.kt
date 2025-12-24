package com.auditory.trackoccupancy.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.auditory.trackoccupancy.databinding.ActivityLoginBinding
import com.auditory.trackoccupancy.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.loginButton.setOnClickListener {
            val login = binding.loginEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (login.isEmpty()) {
                binding.loginEditText.error = "Login is required"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.passwordEditText.error = "Password is required"
                return@setOnClickListener
            }

            viewModel.login(login, password)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                Log.d("LoginActivity", "Login state changed to: $state")
                when (state) {
                    is LoginState.Loading -> {
                        binding.loginButton.isEnabled = false
                        binding.loginButton.text = "Logging in..."
                    }
                    is LoginState.Success -> {
                        Log.d("LoginActivity", "Login successful, navigating to MainActivity")
                        binding.loginButton.isEnabled = true
                        binding.loginButton.text = "Login"
                        navigateToMain()
                    }
                    is LoginState.Error -> {
                        binding.loginButton.isEnabled = true
                        binding.loginButton.text = "Login"
                        Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        binding.loginButton.isEnabled = true
                        binding.loginButton.text = "Login"
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
