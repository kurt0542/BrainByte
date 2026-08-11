package com.example.brainbyte.ui.auth

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
import com.example.brainbyte.ui.main.MainActivity
import com.google.android.material.textfield.TextInputEditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.brainbyte.constants.APPWRITE_PROJECT_ID
import com.example.brainbyte.constants.APPWRITE_PUBLIC_ENDPOINT
import io.appwrite.Client
import io.appwrite.services.Account
import kotlinx.coroutines.launch
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val signupText = view.findViewById<TextView>(R.id.signup_text)
        val root = view.findViewById<ScrollView>(R.id.login_root)
        val loginButton = view.findViewById<AppCompatButton>(R.id.login_btn)
        val email = view.findViewById<TextInputEditText>(R.id.et_email)
        val password = view.findViewById<TextInputEditText>(R.id.et_password)

        val authViewModel: AuthViewModel by activityViewModels()

        loginButton.setOnClickListener {
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString().trim()
            
            if (emailText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.login(emailText, passwordText)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.uiState.collectLatest { state ->
                when (state) {
                    is AuthUiState.Success -> {
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    }
                    is AuthUiState.Error -> {
                        Toast.makeText(requireContext(), "Login Failed: ${state.message}", Toast.LENGTH_LONG).show()
                        authViewModel.resetState()
                    }
                    else -> {}
                }
            }
        }

        val loginGoogleBtn = view.findViewById<AppCompatButton>(R.id.login_google_btn)
        loginGoogleBtn.setOnClickListener {
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
                    Toast.makeText(requireContext(), "Google Login Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
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