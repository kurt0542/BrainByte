package com.example.brainbyte.ui.settings

import com.example.brainbyte.R
import com.example.brainbyte.ui.profile.EditProfileFragment
import com.example.brainbyte.ui.profile.ChangePasswordFragment
import com.example.brainbyte.ui.reminder.ReminderFragment
import androidx.navigation.fragment.findNavController

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
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

        val notificationSwitch = view.findViewById<SwitchCompat>(R.id.pushNotificationSwitch)
        val preference = requireContext().getSharedPreferences("brainbytes_preference",Context.MODE_PRIVATE)

        notificationSwitch.isChecked = preference.getBoolean("notifications_on", false)


        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            preference.edit().putBoolean("notifications_on",isChecked).apply()
            if (isChecked){
                Toast.makeText(context, "Push Notifications Enabled!", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun initializeLabels(view: View) {
        val editRow = view.findViewById<View>(R.id.row_edit)
        editRow.findViewById<TextView>(R.id.rowText).text = "Edit profile"

        val passwordRow = view.findViewById<View>(R.id.row_password)
        passwordRow.findViewById<TextView>(R.id.rowText).text = "Change Password"

        editRow.setOnClickListener {
            EditProfileFragment().show(childFragmentManager, "EditProfileModal")
        }

        passwordRow.setOnClickListener {
            ChangePasswordFragment().show(childFragmentManager, "ChangePasswordModal")
        }

        val reminderRow = view.findViewById<View>(R.id.row_reminders)

        reminderRow.findViewById<TextView>(R.id.rowText).text = "Set reminders"
        reminderRow.findViewById<ImageView>(R.id.rowIcon).setImageResource(R.drawable.ic_add)

        reminderRow.setOnClickListener {
            findNavController().navigate(com.example.brainbyte.R.id.reminderFragment)
        }
    }
}
