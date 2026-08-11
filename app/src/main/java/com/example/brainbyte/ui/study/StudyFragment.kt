package com.example.brainbyte.ui.study

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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.brainbyte.data.entity.Flashcard
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
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

    private val viewModel: StudyViewModel by viewModels()
    private var isShowingTerm = true
    private var deckId: String = ""
    private var deckName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_study, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        loadArguments()
        setupClickListeners()
        observeViewModel()
        loadFlashcards()
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
            deckId = it.getString(ARG_DECK_ID, "") ?: ""
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
            // Logic handled by viewmodel conceptually, but UI handles flip state
            // Let's keep UI flip state here, but we shouldn't directly modify currentIndex
        }

        btnNext.setOnClickListener {
            viewModel.answerCard(true) // Assuming next means correct for now or just advancing
            isShowingTerm = true
        }

        btnShuffle.setOnClickListener {
            // Ideally handled by viewModel, skipped for simplicity now or can trigger a reload
        }

        btnStudyAgain.setOnClickListener {
            isShowingTerm = true
            
            completionView.visibility = View.GONE
            flashcardContainer.visibility = View.VISIBLE
            btnPrevious.visibility = View.VISIBLE
            btnNext.visibility = View.VISIBLE
            progressText.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
            
            viewModel.startStudySession(deckId)
        }

        btnFinishDeck.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadFlashcards() {
        if (deckId.isEmpty()) {
            Toast.makeText(context, "Invalid deck", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }
        viewModel.startStudySession(deckId)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is StudyUiState.Loading -> {
                        // Show loading
                    }
                    is StudyUiState.Study -> {
                        displayCurrentCard(state)
                    }
                    is StudyUiState.Completed -> {
                        showCompletionMessage(state.totalCards)
                    }
                    is StudyUiState.Error -> {
                        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }
                }
            }
        }
    }

    private fun displayCurrentCard(state: StudyUiState.Study) {
        val card = state.currentCard

        // Update content
        if (isShowingTerm) {
            cardLabel.text = getString(R.string.term_label)
            cardContent.text = card.term
        } else {
            cardLabel.text = getString(R.string.definition_label)
            cardContent.text = card.definition
        }

        // Update progress
        val progress = (state.currentIndex.toFloat() / state.totalCards * 100).toInt()
        progressBar.progress = progress
        progressText.text = "${state.currentIndex} / ${state.totalCards}"

        // Update button states
        btnPrevious.isEnabled = false // Not supported in simple advance mode yet
        btnPrevious.alpha = 0.5f

        // Update next button text for last card
        if (state.currentIndex == state.totalCards) {
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
                val state = viewModel.uiState.value
                if (state is StudyUiState.Study) {
                    displayCurrentCard(state)
                }
                flashcardContainer.animate()
                    .scaleX(1f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }

    private fun showCompletionMessage(totalCards: Int) {
        flashcardContainer.visibility = View.GONE
        btnPrevious.visibility = View.GONE
        btnNext.visibility = View.GONE
        progressText.visibility = View.GONE
        progressBar.visibility = View.GONE

        completionStats.text = "You studied $totalCards cards."
        completionView.visibility = View.VISIBLE
    }

    companion object {
        private const val ARG_DECK_ID = "deckId"
        private const val ARG_DECK_NAME = "deckName"

        fun newInstance(deckId: String, deckName: String) = StudyFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_DECK_ID, deckId)
                putString(ARG_DECK_NAME, deckName)
            }
        }
    }
}


