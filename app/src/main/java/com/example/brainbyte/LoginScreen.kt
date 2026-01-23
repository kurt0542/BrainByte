package com.example.brainbyte

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.textfield.TextInputEditText

class LoginScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_screen)

        val root = findViewById<ScrollView>(R.id.login_root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.updatePadding(
                left = sysBars.left,
                top = sysBars.top,
                right = sysBars.right,
                bottom = maxOf(sysBars.bottom, ime.bottom)
            )
            insets
        }

        val email = findViewById<TextInputEditText>(R.id.et_email)
        val password = findViewById<TextInputEditText>(R.id.et_password)

        email.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                password.requestFocus()
                true
            } else false
        }

        password.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                root.post {
                    val rect = Rect()
                    view.getDrawingRect(rect)
                    root.offsetDescendantRectToMyCoords(view, rect)
                    root.smoothScrollTo(0, rect.bottom)
                }
            }
        }

        password.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                view.clearFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
                true
            } else false
        }
    }
}