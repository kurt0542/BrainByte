package com.example.brainbyte

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

class BulkImportDialog(
    context: Context,
    private val onCardsImported: (List<Pair<String, String>>) -> Unit
) : Dialog(context, android.R.style.Theme_Material_Light_Dialog_NoActionBar) {

    private lateinit var separatorRadioGroup: RadioGroup
    private lateinit var customSeparatorLayout: TextInputLayout
    private lateinit var customSeparatorInput: EditText
    private lateinit var bulkTextInput: EditText
    private lateinit var previewCount: TextView
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnImport: MaterialButton

    private var currentSeparator = " - "

    init {
        setupDialog()
    }

    private fun setupDialog() {
        setContentView(R.layout.dialog_bulk_import)
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawableResource(android.R.color.white)

        initViews()
        setupListeners()
        updatePreviewCount()
    }

    private fun initViews() {
        separatorRadioGroup = findViewById(R.id.separator_radio_group)
        customSeparatorLayout = findViewById(R.id.custom_separator_layout)
        customSeparatorInput = findViewById(R.id.custom_separator_input)
        bulkTextInput = findViewById(R.id.bulk_text_input)
        previewCount = findViewById(R.id.preview_count)
        btnCancel = findViewById(R.id.btn_cancel)
        btnImport = findViewById(R.id.btn_import)
    }

    private fun setupListeners() {
        separatorRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            customSeparatorLayout.visibility =
                if (checkedId == R.id.radio_custom) View.VISIBLE else View.GONE

            currentSeparator = when (checkedId) {
                R.id.radio_dash -> " - "
                R.id.radio_colon -> ": "
                R.id.radio_equals -> " = "
                R.id.radio_custom -> customSeparatorInput.text.toString().ifEmpty { " - " }
                else -> " - "
            }
            updatePreviewCount()
        }

        customSeparatorInput.addTextChangedListener(SimpleTextWatcher {
            if (separatorRadioGroup.checkedRadioButtonId == R.id.radio_custom) {
                currentSeparator = it.ifEmpty { " - " }
                updatePreviewCount()
            }
        })

        bulkTextInput.addTextChangedListener(SimpleTextWatcher { updatePreviewCount() })

        btnCancel.setOnClickListener { dismiss() }

        btnImport.setOnClickListener {
            val parsedCards = parseCards()
            if (parsedCards.isNotEmpty()) {
                onCardsImported(parsedCards)
                dismiss()
            }
        }
    }

    fun setInitialText(text: String) {
        bulkTextInput.setText(text)
    }

    private fun updatePreviewCount() {
        val count = parseCards().size
        previewCount.text = if (count == 0) {
            context.getString(R.string.cards_detected_zero)
        } else {
            context.getString(R.string.cards_detected_format, count)
        }
    }

    private fun parseCards(): List<Pair<String, String>> {
        val text = bulkTextInput.text.toString()
        return text.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { line ->
                val parts = line.split(currentSeparator, limit = 2)
                if (parts.size == 2) {
                    val front = parts[0].trim()
                    val back = parts[1].trim()
                    if (front.isNotEmpty() && back.isNotEmpty()) {
                        Pair(front, back)
                    } else null
                } else null
            }
    }
}
