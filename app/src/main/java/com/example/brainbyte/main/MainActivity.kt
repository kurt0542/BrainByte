package com.example.brainbyte.main

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.brainbyte.CardsFragment
import com.example.brainbyte.HomeFragment
import com.example.brainbyte.ImportFragment
import com.example.brainbyte.ProfileFragment
import com.example.brainbyte.R
import com.example.brainbyte.SearchFragment

class MainActivity : AppCompatActivity() {

    private val selectedColor = Color.parseColor("#5B50E8")
    private val unselectedColor = Color.parseColor("#C4C4C4")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHome = findViewById<LinearLayout>(R.id.nav_home)
        val navSearch = findViewById<LinearLayout>(R.id.nav_cards)
        val navCart = findViewById<LinearLayout>(R.id.nav_analytics)
        val navProfile = findViewById<LinearLayout>(R.id.nav_settings)

        val centerButton = findViewById<android.view.View>(R.id.nav_center_button)

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
            updateNavColors(navHome)
        }

        navHome.setOnClickListener {
            loadFragment(HomeFragment())
            updateNavColors(navHome)
        }

        navSearch.setOnClickListener {
            loadFragment(SearchFragment())
            updateNavColors(navSearch)
        }

        navCart.setOnClickListener {
            loadFragment(CardsFragment())
            updateNavColors(navCart)
        }

        navProfile.setOnClickListener {
            loadFragment(ProfileFragment())
            updateNavColors(navProfile)
        }

        centerButton.setOnClickListener {
            loadFragment(ImportFragment())

            updateNavColors(null)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
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