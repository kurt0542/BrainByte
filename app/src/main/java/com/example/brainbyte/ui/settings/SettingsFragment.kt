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
import androidx.lifecycle.lifecycleScope
import com.example.brainbyte.constants.APPWRITE_PROJECT_ID
import com.example.brainbyte.constants.APPWRITE_PUBLIC_ENDPOINT
import com.example.brainbyte.ui.auth.AuthActivity
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Avatars
import kotlinx.coroutines.launch
import android.graphics.BitmapFactory
import android.widget.Button
import android.content.Intent
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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

        val usernameText = view.findViewById<TextView>(R.id.username_text)
        val profileImage = view.findViewById<ImageView>(R.id.profile_image)
        
        val prefs = requireContext().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val cachedName = prefs.getString("user_name", null)
        val avatarFile = java.io.File(requireContext().cacheDir, "avatar.png")

        if (cachedName != null && avatarFile.exists()) {
            usernameText.text = cachedName
            val bitmap = BitmapFactory.decodeFile(avatarFile.absolutePath)
            profileImage.setImageBitmap(bitmap)
            profileImage.alpha = 1f
        } else {
            val client = Client(requireContext())
                .setEndpoint(APPWRITE_PUBLIC_ENDPOINT)
                .setProject(APPWRITE_PROJECT_ID)
            val account = Account(client)
            val avatars = Avatars(client)

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val user = account.get()
                    usernameText.text = user.name
                    prefs.edit().putString("user_name", user.name).apply()

                    val initialsBytes = avatars.getInitials(name = user.name)
                    avatarFile.writeBytes(initialsBytes)
                    
                    val bitmap = BitmapFactory.decodeByteArray(initialsBytes, 0, initialsBytes.size)
                    profileImage.setImageBitmap(bitmap)
                    profileImage.animate().alpha(1f).setDuration(300).start()
                } catch (e: Exception) {
                    usernameText.text = "Guest"
                    profileImage.setImageResource(R.drawable.ic_profile)
                    profileImage.animate().alpha(1f).setDuration(300).start()
                }
            }
        }

        val btnSignOut = view.findViewById<Button>(R.id.btn_sign_out)
        btnSignOut.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Sign out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign out") { _, _ ->
                    val client = Client(requireContext())
                        .setEndpoint(APPWRITE_PUBLIC_ENDPOINT)
                        .setProject(APPWRITE_PROJECT_ID)
                    val account = Account(client)
                    
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            account.deleteSession("current")
                        } catch (e: Exception) {
                            // Ignore, maybe no active session
                        }
                        
                        // Clear local cache
                        prefs.edit().remove("user_name").apply()
                        if (avatarFile.exists()) {
                            avatarFile.delete()
                        }

                        Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(requireContext(), AuthActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
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
