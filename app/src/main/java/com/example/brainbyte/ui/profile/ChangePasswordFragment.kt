package com.example.brainbyte.ui.profile

import com.example.brainbyte.R

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ChangePasswordFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_change_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Removed back button since it's a modal now
        
        view.findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            val currentPassword = view.findViewById<TextInputEditText>(R.id.etCurrentPassword).text.toString()
            val newPassword = view.findViewById<TextInputEditText>(R.id.etNewPassword).text.toString()
            val confirmPassword = view.findViewById<TextInputEditText>(R.id.etConfirmPassword).text.toString()

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(context, "New passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mock save action
            Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }
}
