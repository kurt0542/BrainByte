package com.example.brainbyte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment

class NavigationBarFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_navigation_bar, container, false)

        view.findViewById<View>(R.id.nav_home).setOnClickListener {
            Toast.makeText(requireContext(), "Home clicked", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.nav_cards).setOnClickListener {
            Toast.makeText(requireContext(), "Cards clicked", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.nav_center_button).setOnClickListener {
            Toast.makeText(requireContext(), "Center button clicked", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.nav_analytics).setOnClickListener {
            Toast.makeText(requireContext(), "Analytics clicked", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.nav_settings).setOnClickListener {
            Toast.makeText(requireContext(), "Settings clicked", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}
