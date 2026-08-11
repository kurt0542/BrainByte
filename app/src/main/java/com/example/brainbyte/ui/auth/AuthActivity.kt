package com.example.brainbyte.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.brainbyte.R
import com.example.brainbyte.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_auth)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fragmentContainerView, LoginFragment())
                .commit()
        }

        lifecycleScope.launch {
            authViewModel.uiState.collectLatest { state ->
                when (state) {
                    is AuthUiState.Success -> {
                        startActivity(Intent(this@AuthActivity, MainActivity::class.java))
                        finish()
                    }
                    else -> {
                        // Stay on this screen
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        authViewModel.checkSession()
    }
}