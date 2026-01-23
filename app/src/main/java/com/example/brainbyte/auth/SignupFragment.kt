package com.example.brainbyte.auth

import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.brainbyte.R
import com.google.android.material.textfield.TextInputEditText

class SignupFragment : Fragment(R.layout.fragment_signup) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val signinText = view.findViewById<TextView>(R.id.login_text)
        val root = view.findViewById<ScrollView>(R.id.signup_root)

        signinText.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, LoginFragment())
                .commit()
        }

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.updatePadding(
                left = sysBars.left,
                top = sysBars.top,
                right = sysBars.right,
                bottom = maxOf(sysBars.bottom, ime.bottom)
            )
            insets
        }

        val username = view.findViewById<TextInputEditText>(R.id.et_username)
        val email = view.findViewById<TextInputEditText>(R.id.et_email)
        val password = view.findViewById<TextInputEditText>(R.id.et_password)
        val confirmPassword = view.findViewById<TextInputEditText>(R.id.et_confirm_password)

        username.isFocusableInTouchMode = true
        email.isFocusableInTouchMode = true
        password.isFocusableInTouchMode = true
        confirmPassword.isFocusableInTouchMode = true

        username.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                email.requestFocus()
                val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
                imm?.showSoftInput(email, InputMethodManager.SHOW_IMPLICIT)
                true
            } else false
        }

        email.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                root.postDelayed({
                    val rect = Rect()
                    v.getDrawingRect(rect)
                    root.offsetDescendantRectToMyCoords(v, rect)
                    val extra = 24
                    root.smoothScrollTo(0, rect.bottom + extra)
                }, 100)
                val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
                imm?.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        email.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                password.requestFocus()
                val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
                imm?.showSoftInput(password, InputMethodManager.SHOW_IMPLICIT)
                true
            } else false
        }

        password.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                root.post {
                    val rect = Rect()
                    v.getDrawingRect(rect)
                    root.offsetDescendantRectToMyCoords(v, rect)
                    val extra = 24
                    root.smoothScrollTo(0, rect.bottom + extra)
                }
                val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
                imm?.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        password.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                confirmPassword.requestFocus()
                val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
                imm?.showSoftInput(confirmPassword, InputMethodManager.SHOW_IMPLICIT)
                true
            } else false
        }

        confirmPassword.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                root.post {
                    val rect = Rect()
                    v.getDrawingRect(rect)
                    root.offsetDescendantRectToMyCoords(v, rect)
                    val extra = 24
                    root.smoothScrollTo(0, rect.bottom + extra)
                }
                val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
                imm?.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        confirmPassword.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                v.clearFocus()
                val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
                imm?.hideSoftInputFromWindow(v.windowToken, 0)
                true
            } else false
        }
    }
}