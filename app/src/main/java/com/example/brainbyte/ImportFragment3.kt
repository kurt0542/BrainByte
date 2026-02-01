package com.example.brainbyte

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

    private var currentCardNumber = 1
    private var savedCardsCount = 0
    private var isShowingFront = true
    private var isAnimating = false

    private val savedCards = mutableListOf<Pair<String, String>>()
    private var deckName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_import3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupTextWatchers()
        setupClickListeners()
        setupCardFlip()
        updateCardCounter()

        arguments?.getString("deckName")?.let { name ->
            selectedDeckName.text = name
            deckNameInput.setText(name)
        }

        arguments?.getString("recognizedText")?.let { text ->
            if (text.isNotEmpty()) {
                frontCardInput.setText(text)
            }
        }
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
    }

    private fun setupCardFlip() {
        val scale = resources.displayMetrics.density
        val cameraDist = 8000 * scale
        previewCardFront.cameraDistance = cameraDist
        previewCardBack.cameraDistance = cameraDist
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updatePreview()
            }
        }

        frontCardInput.addTextChangedListener(textWatcher)
        backCardInput.addTextChangedListener(textWatcher)

        deckNameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val name = s.toString().trim()
                if (name.isNotEmpty()) {
                    selectedDeckName.text = name
                } else {
                    selectedDeckName.text = getString(R.string.selected_deck_placeholder)
                }
            }
        })
    }

    private fun setupClickListeners() {
        previewCardContainer.setOnClickListener {
            if (!isAnimating) {
                flipCard()
            }
        }

        changeDeckBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnAddAnother.setOnClickListener {
            if (validateCardInputs()) {
                saveCurrentCard()
                clearCardInputs()
                currentCardNumber++
                updateCardCounter()
                Toast.makeText(context, "Card saved! Add another one.", Toast.LENGTH_SHORT).show()
            }
        }

        btnSaveCard.setOnClickListener {
            if (validateCardInputs()) {
                saveCurrentCard()
                Toast.makeText(context, "Card saved!", Toast.LENGTH_SHORT).show()
            }
        }

        // Finish import button
        btnFinish.setOnClickListener {
            if (!validateDeckName()) return@setOnClickListener

            if (frontCardInput.text.isNotEmpty() && backCardInput.text.isNotEmpty()) {
                saveCurrentCard()
            }

            if (savedCards.isNotEmpty() || savedCardsCount > 0) {
                deckName = deckNameInput.text.toString().trim()
                Toast.makeText(
                    context,
                    "Deck \"$deckName\" with ${savedCardsCount + savedCards.size} card(s) created!",
                    Toast.LENGTH_LONG
                ).show()
                parentFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
            } else {
                Toast.makeText(context, "Please add at least one card", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePreview() {
        val frontText = frontCardInput.text.toString()
        val backText = backCardInput.text.toString()

        previewFrontText.text = frontText.ifEmpty {
            getString(R.string.preview_front_placeholder)
        }

        previewBackText.text = backText.ifEmpty {
            getString(R.string.preview_back_placeholder)
        }
    }

    private fun flipCard() {
        isAnimating = true

        val flipOutDuration = 150L
        val flipInDuration = 150L

        if (isShowingFront) {
            previewCardFront.animate()
                .rotationY(90f)
                .setDuration(flipOutDuration)
                .withEndAction {
                    previewCardFront.visibility = View.GONE
                    previewCardBack.visibility = View.VISIBLE
                    previewCardBack.rotationY = -90f

                    previewCardBack.animate()
                        .rotationY(0f)
                        .setDuration(flipInDuration)
                        .withEndAction {
                            isAnimating = false
                        }
                        .start()
                }
                .start()
        } else {
            previewCardBack.animate()
                .rotationY(90f)
                .setDuration(flipOutDuration)
                .withEndAction {
                    previewCardBack.visibility = View.GONE
                    previewCardFront.visibility = View.VISIBLE
                    previewCardFront.rotationY = -90f

                    previewCardFront.animate()
                        .rotationY(0f)
                        .setDuration(flipInDuration)
                        .withEndAction {
                            isAnimating = false
                        }
                        .start()
                }
                .start()
        }

        isShowingFront = !isShowingFront
    }

    private fun updateCardCounter() {
        cardCounter.text = getString(R.string.card_counter_format, currentCardNumber)
    }

    private fun validateDeckName(): Boolean {
        val name = deckNameInput.text.toString().trim()
        return if (name.isEmpty()) {
            deckNameInput.error = "Please enter a deck name"
            false
        } else {
            true
        }
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
            savedCardsCount++
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

    companion object {
        fun newInstance(deckName: String? = null, recognizedText: String? = null): ImportFragment3 {
            return ImportFragment3().apply {
                arguments = Bundle().apply {
                    deckName?.let { putString("deckName", it) }
                    recognizedText?.let { putString("recognizedText", it) }
                }
            }
        }
    }
}
