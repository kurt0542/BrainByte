package com.example.brainbyte.ui.main

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import com.example.brainbyte.R
import com.example.brainbyte.constants.APPWRITE_PROJECT_ID
import com.example.brainbyte.constants.APPWRITE_PUBLIC_ENDPOINT
import dagger.hilt.android.AndroidEntryPoint
import io.appwrite.Client


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val selectedColor = Color.parseColor("#5B50E8")
    private val unselectedColor = Color.parseColor("#C4C4C4")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val client = Client(applicationContext)
            .setEndpoint(APPWRITE_PUBLIC_ENDPOINT)
            .setProject(APPWRITE_PROJECT_ID)

        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHome = findViewById<LinearLayout>(R.id.nav_home)
        val navSearch = findViewById<LinearLayout>(R.id.nav_cards)
        val navCart = findViewById<LinearLayout>(R.id.nav_analytics)
        val navProfile = findViewById<LinearLayout>(R.id.nav_settings)

        val centerButton = findViewById<android.view.View>(R.id.nav_center_button)

        if (savedInstanceState == null) {
            updateNavColors(navHome)
        }

        navHome.setOnClickListener {
            findNavController(R.id.fragment_container).navigate(R.id.homeFragment)
            updateNavColors(navHome)
        }

        navSearch.setOnClickListener {
            findNavController(R.id.fragment_container).navigate(R.id.cardsFragment)
            updateNavColors(navSearch)
        }

        navCart.setOnClickListener {
            findNavController(R.id.fragment_container).navigate(R.id.analyticsFragment)
            updateNavColors(navCart)
        }

        navProfile.setOnClickListener {
            findNavController(R.id.fragment_container).navigate(R.id.settingsFragment)
            updateNavColors(navProfile)
        }

        centerButton.setOnClickListener {
            findNavController(R.id.fragment_container).navigate(R.id.documentScannerFragment)
            updateNavColors(null)
        }
    }


    private fun updateNavColors(selectedLayout: LinearLayout?) {
        val navItems = listOf(
            findViewById<LinearLayout>(R.id.nav_home),
            findViewById<LinearLayout>(R.id.nav_cards),
            findViewById<LinearLayout>(R.id.nav_analytics),
            findViewById<LinearLayout>(R.id.nav_settings)
        )

        navItems.forEach { layout ->
            val icon = layout.getChildAt(0) as ImageView
            val text = layout.getChildAt(1) as TextView

            if (layout == selectedLayout) {
                icon.setColorFilter(selectedColor)
                text.setTextColor(selectedColor)
            } else {
                icon.setColorFilter(unselectedColor)
                text.setTextColor(unselectedColor)
            }
        }
    }
}