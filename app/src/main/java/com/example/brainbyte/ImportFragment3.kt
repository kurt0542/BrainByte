package com.example.brainbyte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class ImportFragment3 : Fragment() {

    private lateinit var deckNameInput: EditText
    private lateinit var frontCardInput: EditText
    private lateinit var backCardInput: EditText
    private lateinit var previewFrontText: TextView
    private lateinit var previewBackText: TextView
    private lateinit var previewCardFront: MaterialCardView
    private lateinit var previewCardBack: MaterialCardView
    private lateinit var previewCardContainer: View
    private lateinit var cardCounter: TextView
    private lateinit var selectedDeckName: TextView
    private lateinit var changeDeckBtn: TextView
    private lateinit var btnAddAnother: MaterialButton
    private lateinit var btnSaveCard: MaterialButton
    private lateinit var btnFinish: MaterialButton
    private lateinit var btnBulkImport: MaterialButton

    private var currentCardNumber = 1
    private var isShowingFront = true
    private var isAnimating = false
    private val savedCards = mutableListOf<Pair<String, String>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_import3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupCardFlip()
        setupTextWatchers()
        setupClickListeners()
        loadArguments()
        updateCardCounter()
    }

    private fun initViews(view: View) {
        deckNameInput = view.findViewById(R.id.deck_name_input)
        frontCardInput = view.findViewById(R.id.front_card_input)
        backCardInput = view.findViewById(R.id.back_card_input)
        previewFrontText = view.findViewById(R.id.preview_front_text)
        previewBackText = view.findViewById(R.id.preview_back_text)
        previewCardFront = view.findViewById(R.id.preview_card_front)
        previewCardBack = view.findViewById(R.id.preview_card_back)
        previewCardContainer = view.findViewById(R.id.preview_card_container)
        cardCounter = view.findViewById(R.id.card_counter)
        selectedDeckName = view.findViewById(R.id.selected_deck_name)
        changeDeckBtn = view.findViewById(R.id.change_deck_btn)
        btnAddAnother = view.findViewById(R.id.btn_add_another)
        btnSaveCard = view.findViewById(R.id.btn_save_card)
        btnFinish = view.findViewById(R.id.btn_finish)
        btnBulkImport = view.findViewById(R.id.btn_bulk_import)
    }

    private fun loadArguments() {
        arguments?.getString("deckName")?.let { name ->
            selectedDeckName.text = name
            deckNameInput.setText(name)
        }
        arguments?.getString("recognizedText")?.let { text ->
            if (text.isNotEmpty()) frontCardInput.setText(text)
        }
    }

    private fun setupCardFlip() {
        val cameraDist = 8000 * resources.displayMetrics.density
        previewCardFront.cameraDistance = cameraDist
        previewCardBack.cameraDistance = cameraDist
    }

    private fun setupTextWatchers() {
        val previewWatcher = SimpleTextWatcher { updatePreview() }
        frontCardInput.addTextChangedListener(previewWatcher)
        backCardInput.addTextChangedListener(previewWatcher)

        deckNameInput.addTextChangedListener(SimpleTextWatcher { name ->
            selectedDeckName.text = name.ifEmpty { getString(R.string.selected_deck_placeholder) }
        })
    }

    private fun setupClickListeners() {
        previewCardContainer.setOnClickListener { if (!isAnimating) flipCard() }
        changeDeckBtn.setOnClickListener { parentFragmentManager.popBackStack() }
        btnBulkImport.setOnClickListener { showBulkImportDialog() }
        btnAddAnother.setOnClickListener { addAnotherCard() }
        btnSaveCard.setOnClickListener { saveCard() }
        btnFinish.setOnClickListener { finishImport() }
    }

    private fun addAnotherCard() {
        if (validateCardInputs()) {
            saveCurrentCard()
            clearCardInputs()
            currentCardNumber++
            updateCardCounter()
            showToast("Card saved! Add another one.")
        }
    }

    private fun saveCard() {
        if (validateCardInputs()) {
            saveCurrentCard()
            showToast("Card saved!")
        }
    }

    private fun finishImport() {
        if (!validateDeckName()) return

        if (frontCardInput.text.isNotEmpty() && backCardInput.text.isNotEmpty()) {
            saveCurrentCard()
        }

        if (savedCards.isNotEmpty()) {
            val deckName = deckNameInput.text.toString().trim()
            showToast("Deck \"$deckName\" with ${savedCards.size} card(s) created!")
            parentFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        } else {
            showToast("Please add at least one card")
        }
    }

    private fun showBulkImportDialog() {
        val dialog = BulkImportDialog(requireContext()) { importedCards ->
            savedCards.addAll(importedCards)
            currentCardNumber += importedCards.size
            updateCardCounter()
            showToast(getString(R.string.bulk_import_success, importedCards.size))
        }

        arguments?.getString("recognizedText")?.let { text ->
            if (text.isNotEmpty() && frontCardInput.text.isEmpty()) {
                dialog.setInitialText(text)
            }
        }

        dialog.show()
    }

    private fun updatePreview() {
        previewFrontText.text = frontCardInput.text.toString().ifEmpty {
            getString(R.string.preview_front_placeholder)
        }
        previewBackText.text = backCardInput.text.toString().ifEmpty {
            getString(R.string.preview_back_placeholder)
        }
    }

    private fun flipCard() {
        isAnimating = true
        val duration = 150L

        val (hideCard, showCard) = if (isShowingFront) {
            previewCardFront to previewCardBack
        } else {
            previewCardBack to previewCardFront
        }

        hideCard.animate()
            .rotationY(90f)
            .setDuration(duration)
            .withEndAction {
                hideCard.visibility = View.GONE
                showCard.visibility = View.VISIBLE
                showCard.rotationY = -90f
                showCard.animate()
                    .rotationY(0f)
                    .setDuration(duration)
                    .withEndAction { isAnimating = false }
                    .start()
            }
            .start()

        isShowingFront = !isShowingFront
    }

    private fun updateCardCounter() {
        cardCounter.text = getString(R.string.card_counter_format, currentCardNumber)
    }

    private fun validateDeckName(): Boolean {
        val name = deckNameInput.text.toString().trim()
        if (name.isEmpty()) {
            deckNameInput.error = "Please enter a deck name"
            return false
        }
        return true
    }

    private fun validateCardInputs(): Boolean {
        val front = frontCardInput.text.toString().trim()
        val back = backCardInput.text.toString().trim()

        return when {
            front.isEmpty() -> {
                frontCardInput.error = "Please enter the front of the card"
                false
            }
            back.isEmpty() -> {
                backCardInput.error = "Please enter the back of the card"
                false
            }
            else -> true
        }
    }

    private fun saveCurrentCard() {
        val front = frontCardInput.text.toString().trim()
        val back = backCardInput.text.toString().trim()
        if (front.isNotEmpty() && back.isNotEmpty()) {
            savedCards.add(Pair(front, back))
        }
    }

    private fun clearCardInputs() {
        frontCardInput.text.clear()
        backCardInput.text.clear()
        frontCardInput.clearFocus()
        backCardInput.clearFocus()

        if (!isShowingFront) {
            previewCardBack.visibility = View.GONE
            previewCardBack.rotationY = 0f
            previewCardFront.rotationY = 0f
            previewCardFront.visibility = View.VISIBLE
            isShowingFront = true
        }
        updatePreview()
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newInstance(deckName: String? = null, recognizedText: String? = null) =
            ImportFragment3().apply {
                arguments = Bundle().apply {
                    deckName?.let { putString("deckName", it) }
                    recognizedText?.let { putString("recognizedText", it) }
                }
            }
    }
}
