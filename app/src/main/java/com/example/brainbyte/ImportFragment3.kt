package com.example.brainbyte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.brainbyte.data.BrainByteDatabase
import com.example.brainbyte.data.repository.FlashcardRepository
import com.example.brainbyte.ocr.FlashcardPair
import com.example.brainbyte.ocr.ScannedFlashcardAdapter
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class ImportFragment3 : Fragment() {

    private lateinit var deckNameInput: EditText
    private lateinit var cardCounter: TextView
    private lateinit var selectedDeckName: TextView
    private lateinit var changeDeckBtn: TextView
    private lateinit var btnFinish: MaterialButton
    private lateinit var flashcardsRecyclerView: RecyclerView
    private lateinit var emptyStateContainer: View

    private val flashcardsList = mutableListOf<FlashcardPair>()
    private lateinit var flashcardAdapter: ScannedFlashcardAdapter
    private lateinit var repository: FlashcardRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_import3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initDatabase()
        initViews(view)
        setupRecyclerView()
        setupTextWatchers()
        setupClickListeners()
        loadArguments()
        updateCardCounter()
    }

    private fun initDatabase() {
        val database = BrainByteDatabase.getDatabase(requireContext())
        repository = FlashcardRepository(database.deckDao(), database.flashcardDao())
    }

    private fun initViews(view: View) {
        deckNameInput = view.findViewById(R.id.deck_name_input)
        cardCounter = view.findViewById(R.id.card_counter)
        selectedDeckName = view.findViewById(R.id.selected_deck_name)
        changeDeckBtn = view.findViewById(R.id.change_deck_btn)
        btnFinish = view.findViewById(R.id.btn_finish)
        flashcardsRecyclerView = view.findViewById(R.id.flashcards_recycler_view)
        emptyStateContainer = view.findViewById(R.id.empty_state_container)
    }

    private fun setupRecyclerView() {
        flashcardAdapter = ScannedFlashcardAdapter(
            flashcardsList,
            onDeleteClick = { position -> deleteFlashcard(position) },
            onFlashcardChanged = { updateCardCounter() }
        )
        flashcardsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = flashcardAdapter
        }
    }

    private fun loadArguments() {
        arguments?.getString("deckName")?.let { name ->
            selectedDeckName.text = name
            deckNameInput.setText(name)
        }
        arguments?.getString("recognizedText")?.let { text ->
            if (text.isNotEmpty()) {
                parseFlashcardsFromText(text)
            }
        }
    }

    private fun parseFlashcardsFromText(text: String) {
        val entries = text.split("\n\n")
        entries.forEach { entry ->
            val parts = entry.split(": ", limit = 2)
            if (parts.size == 2) {
                val term = parts[0].trim()
                val definition = parts[1].trim()
                if (term.isNotEmpty() && definition.isNotEmpty()) {
                    flashcardsList.add(FlashcardPair(term, definition))
                }
            }
        }
        flashcardAdapter.notifyDataSetChanged()
        updateCardCounter()
    }

    private fun setupTextWatchers() {
        deckNameInput.addTextChangedListener(SimpleTextWatcher { name ->
            selectedDeckName.text = name.ifEmpty { getString(R.string.selected_deck_placeholder) }
        })
    }

    private fun setupClickListeners() {
        changeDeckBtn.setOnClickListener { parentFragmentManager.popBackStack() }
        btnFinish.setOnClickListener { finishImport() }
    }

    private fun deleteFlashcard(position: Int) {
        flashcardAdapter.removeFlashcard(position)
        updateCardCounter()
        showToast("Flashcard deleted")
    }

    private fun finishImport() {
        if (!validateDeckName()) return

        if (flashcardsList.isNotEmpty()) {
            val deckName = deckNameInput.text.toString().trim()
            val finalFlashcards = flashcardAdapter.getFlashcards()

            btnFinish.isEnabled = false
            btnFinish.text = "Saving..."

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val deckId = repository.createDeckWithFlashcards(deckName, finalFlashcards)

                    showToast("Deck \"$deckName\" with ${finalFlashcards.size} card(s) saved!")

                    parentFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
                } catch (e: Exception) {
                    showToast("Failed to save deck: ${e.message}")
                    btnFinish.isEnabled = true
                    btnFinish.text = "Finish"
                }
            }
        } else {
            showToast("Please add at least one card")
        }
    }

    private fun updateCardCounter() {
        val count = flashcardsList.size
        cardCounter.text = getString(R.string.cards_count_format, count)

        if (count == 0) {
            emptyStateContainer.visibility = View.VISIBLE
            flashcardsRecyclerView.visibility = View.GONE
        } else {
            emptyStateContainer.visibility = View.GONE
            flashcardsRecyclerView.visibility = View.VISIBLE
        }

        btnFinish.isEnabled = count > 0
    }

    private fun validateDeckName(): Boolean {
        val name = deckNameInput.text.toString().trim()
        if (name.isEmpty()) {
            deckNameInput.error = "Please enter a deck name"
            return false
        }
        return true
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
