package com.example.brainbyte.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.brainbyte.R
import com.example.brainbyte.constants.APPWRITE_PROJECT_ID
import com.example.brainbyte.constants.APPWRITE_PUBLIC_ENDPOINT
import com.example.brainbyte.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import io.appwrite.Client
import io.appwrite.services.Account
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {
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
    }

    override fun onResume() {
        super.onResume()
        val client = Client(this)
            .setEndpoint(APPWRITE_PUBLIC_ENDPOINT)
            .setProject(APPWRITE_PROJECT_ID)
        val account = Account(client)
        
        lifecycleScope.launch {
            try {
                account.get()
                // If this succeeds, the user is logged in
                startActivity(Intent(this@AuthActivity, MainActivity::class.java))
                finish()
            } catch (e: Exception) {
                // Not logged in, stay on this screen
            }
        }
    }
}