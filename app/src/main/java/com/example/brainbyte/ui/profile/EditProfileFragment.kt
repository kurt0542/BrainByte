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

class EditProfileFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Removed back button since it's a modal now
        
        view.findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            // Mock save action
            Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }
}
