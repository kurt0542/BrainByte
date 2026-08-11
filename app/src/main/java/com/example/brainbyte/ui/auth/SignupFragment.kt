package com.example.brainbyte.ui.auth

import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.example.brainbyte.R
import com.example.brainbyte.constants.APPWRITE_PROJECT_ID
import com.example.brainbyte.constants.APPWRITE_PUBLIC_ENDPOINT
import com.google.android.material.textfield.TextInputEditText
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.services.Account
import kotlinx.coroutines.launch
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
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

        // Ensure you have a button in your XML layout with this ID (or change it to match)
        val signupButton = view.findViewById<Button>(R.id.signup_btn)

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

        val authViewModel: AuthViewModel by activityViewModels()

        // --- APPWRITE AUTHENTICATION SETUP ---
        signupButton.setOnClickListener {
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString().trim()
            val confirmPasswordText = confirmPassword.text.toString().trim()
            val nameText = username.text.toString().trim()

            if (passwordText != confirmPasswordText) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.signup(emailText, passwordText, nameText)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.uiState.collectLatest { state ->
                when (state) {
                    is AuthUiState.Success -> {
                        Toast.makeText(requireContext(), "Account created successfully!", Toast.LENGTH_SHORT).show()
                        authViewModel.resetState()
                        // Navigate to Login after successful registration
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainerView, LoginFragment())
                            .commit()
                    }
                    is AuthUiState.Error -> {
                        Toast.makeText(requireContext(), "Error: ${state.message}", Toast.LENGTH_LONG).show()
                        authViewModel.resetState()
                    }
                    else -> {}
                }
            }
        }

        val signupGoogleBtn = view.findViewById<Button>(R.id.signup_google_btn)
        signupGoogleBtn.setOnClickListener {
            val client = Client(requireContext())
                .setEndpoint(APPWRITE_PUBLIC_ENDPOINT)
                .setProject(APPWRITE_PROJECT_ID)
            val account = Account(client)

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    account.createOAuth2Session(
                        activity = requireActivity(),
                        provider = io.appwrite.enums.OAuthProvider.GOOGLE
                    )
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Google Signup Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}