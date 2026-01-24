package com.example.brainbyte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView // Don't forget these imports!
import android.widget.TextView
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeLabels(view)
    }

    private fun initializeLabels(view: View) {
        val editRow = view.findViewById<View>(R.id.row_edit)
        editRow.findViewById<TextView>(R.id.rowText).text = "Edit profile"

        val passwordRow = view.findViewById<View>(R.id.row_password)
        passwordRow.findViewById<TextView>(R.id.rowText).text = "Change Password"

        val reminderRow = view.findViewById<View>(R.id.row_reminders)

        reminderRow.findViewById<TextView>(R.id.rowText).text = "Set reminders"
        reminderRow.findViewById<ImageView>(R.id.rowIcon).setImageResource(R.drawable.ic_add)
    }
}