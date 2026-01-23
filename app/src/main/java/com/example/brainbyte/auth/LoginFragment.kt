package com.example.brainbyte.auth

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.example.brainbyte.R
import com.example.brainbyte.main.MainActivity
import com.google.android.material.textfield.TextInputEditText

class LoginFragment : Fragment(R.layout.fragment_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val signupText = view.findViewById<TextView>(R.id.signup_text)
        val root = view.findViewById<ScrollView>(R.id.login_root)
        val loginButton = view.findViewById<AppCompatButton>(R.id.login_btn)

        loginButton.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)

            startActivity(intent)
            requireActivity().finish()
        }
        signupText.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, SignupFragment())
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

        val email = view.findViewById<TextInputEditText>(R.id.et_email)
        val password = view.findViewById<TextInputEditText>(R.id.et_password)

        email.isFocusableInTouchMode = true
        password.isFocusableInTouchMode = true

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

        password.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                v.clearFocus()
                val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
                imm?.hideSoftInputFromWindow(v.windowToken, 0)
                true
            } else false
        }
    }
}