package com.example.brainbyte

import com.example.brainbyte.R

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.brainbyte.data.BrainByteDatabase
import com.example.brainbyte.data.entity.Flashcard
import com.example.brainbyte.data.repository.FlashcardRepository
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class StudyFragment : Fragment() {

    private lateinit var btnBack: ImageButton
    private lateinit var deckTitle: TextView
    private lateinit var progressText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var flashcardContainer: CardView
    private lateinit var cardLabel: TextView
    private lateinit var cardContent: TextView
    private lateinit var tapHint: TextView
    private lateinit var btnPrevious: MaterialButton
    private lateinit var btnNext: MaterialButton
    private lateinit var btnShuffle: ImageButton
    private lateinit var completionView: View
    private lateinit var completionStats: TextView
    private lateinit var btnStudyAgain: MaterialButton
    private lateinit var btnFinishDeck: MaterialButton

    private lateinit var repository: FlashcardRepository
    private var flashcards: List<Flashcard> = emptyList()
    private var currentIndex = 0
    private var isShowingTerm = true
    private var deckId: Long = -1
    private var deckName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_study, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initDatabase()
        initViews(view)
        loadArguments()
        setupClickListeners()
        loadFlashcards()
    }

    private fun initDatabase() {
        val database = BrainByteDatabase.getDatabase(requireContext())
        repository = FlashcardRepository(database)
    }

    private fun initViews(view: View) {
        btnBack = view.findViewById(R.id.btn_back)
        deckTitle = view.findViewById(R.id.deck_title)
        progressText = view.findViewById(R.id.progress_text)
        progressBar = view.findViewById(R.id.progress_bar)
        flashcardContainer = view.findViewById(R.id.flashcard_container)
        cardLabel = view.findViewById(R.id.card_label)
        cardContent = view.findViewById(R.id.card_content)
        tapHint = view.findViewById(R.id.tap_hint)
        btnPrevious = view.findViewById(R.id.btn_previous)
        btnNext = view.findViewById(R.id.btn_next)
        btnShuffle = view.findViewById(R.id.btn_shuffle)
        completionView = view.findViewById(R.id.completion_view)
        completionStats = view.findViewById(R.id.completion_stats)
        btnStudyAgain = view.findViewById(R.id.btn_study_again)
        btnFinishDeck = view.findViewById(R.id.btn_finish_deck)
    }

    private fun loadArguments() {
        arguments?.let {
            deckId = it.getLong(ARG_DECK_ID, -1)
            deckName = it.getString(ARG_DECK_NAME, "") ?: ""
        }
        deckTitle.text = deckName
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        flashcardContainer.setOnClickListener {
            flipCard()
        }

        btnPrevious.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                isShowingTerm = true
                displayCurrentCard()
            }
        }

        btnNext.setOnClickListener {
            if (currentIndex < flashcards.size - 1) {
                currentIndex++
                isShowingTerm = true
                displayCurrentCard()
            } else {
                // Finished studying
                showCompletionMessage()
            }
        }

        btnShuffle.setOnClickListener {
            flashcards = flashcards.shuffled()
            currentIndex = 0
            isShowingTerm = true
            displayCurrentCard()
        }

        btnStudyAgain.setOnClickListener {
            currentIndex = 0
            isShowingTerm = true
            
            completionView.visibility = View.GONE
            flashcardContainer.visibility = View.VISIBLE
            btnPrevious.visibility = View.VISIBLE
            btnNext.visibility = View.VISIBLE
            progressText.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
            
            displayCurrentCard()
        }

        btnFinishDeck.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadFlashcards() {
        if (deckId == -1L) {
            Toast.makeText(context, "Invalid deck", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            flashcards = repository.getFlashcardsByDeckIdList(deckId)
            if (flashcards.isEmpty()) {
                Toast.makeText(context, "No flashcards in this deck", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } else {
                displayCurrentCard()
            }
        }
    }

    private fun displayCurrentCard() {
        if (flashcards.isEmpty()) return

        val card = flashcards[currentIndex]

        // Update content
        if (isShowingTerm) {
            cardLabel.text = getString(R.string.term_label)
            cardContent.text = card.term
        } else {
            cardLabel.text = getString(R.string.definition_label)
            cardContent.text = card.definition
        }

        // Update progress
        val progress = ((currentIndex + 1).toFloat() / flashcards.size * 100).toInt()
        progressBar.progress = progress
        progressText.text = "${currentIndex + 1} / ${flashcards.size}"

        // Update button states
        btnPrevious.isEnabled = currentIndex > 0
        btnPrevious.alpha = if (currentIndex > 0) 1f else 0.5f

        // Update next button text for last card
        if (currentIndex == flashcards.size - 1) {
            btnNext.text = getString(R.string.finish_button)
        } else {
            btnNext.text = getString(R.string.next_button)
        }
    }

    private fun flipCard() {
        // Simple flip animation
        flashcardContainer.animate()
            .scaleX(0f)
            .setDuration(150)
            .withEndAction {
                isShowingTerm = !isShowingTerm
                displayCurrentCard()
                flashcardContainer.animate()
                    .scaleX(1f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }

    private fun showCompletionMessage() {
        flashcardContainer.visibility = View.GONE
        btnPrevious.visibility = View.GONE
        btnNext.visibility = View.GONE
        progressText.visibility = View.GONE
        progressBar.visibility = View.GONE

        completionStats.text = "You studied ${flashcards.size} cards."
        completionView.visibility = View.VISIBLE
    }

    companion object {
        private const val ARG_DECK_ID = "deck_id"
        private const val ARG_DECK_NAME = "deck_name"

        fun newInstance(deckId: Long, deckName: String) = StudyFragment().apply {
            arguments = Bundle().apply {
                putLong(ARG_DECK_ID, deckId)
                putString(ARG_DECK_NAME, deckName)
            }
        }
    }
}


