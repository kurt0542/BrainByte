package com.example.brainbyte.ui.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.brainbyte.R
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QuizFragment : Fragment() {

    private val viewModel: QuizViewModel by viewModels()
    private var deckId: String = ""

    private lateinit var progressLoading: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var tvExplanation: TextView
    private lateinit var btnNext: MaterialButton
    private lateinit var layoutOptions: LinearLayout
    private lateinit var layoutCompleted: LinearLayout
    private lateinit var tvFinalScore: TextView
    private lateinit var btnFinishQuiz: MaterialButton

    private val optionButtons = mutableListOf<MaterialButton>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quiz, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        arguments?.let {
            deckId = it.getString(ARG_DECK_ID, "") ?: ""
        }

        initViews(view)
        setupClickListeners()
        observeViewModel()

        if (deckId.isNotEmpty()) {
            viewModel.startQuiz(deckId)
        } else {
            Toast.makeText(requireContext(), "Invalid Deck ID", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    private fun initViews(view: View) {
        progressLoading = view.findViewById(R.id.progress_loading)
        tvProgress = view.findViewById(R.id.tv_progress)
        tvScore = view.findViewById(R.id.tv_score)
        tvQuestion = view.findViewById(R.id.tv_question)
        tvExplanation = view.findViewById(R.id.tv_explanation)
        btnNext = view.findViewById(R.id.btn_next)
        layoutOptions = view.findViewById(R.id.layout_options)
        layoutCompleted = view.findViewById(R.id.layout_completed)
        tvFinalScore = view.findViewById(R.id.tv_final_score)
        btnFinishQuiz = view.findViewById(R.id.btn_finish_quiz)

        optionButtons.add(view.findViewById(R.id.btn_option_0))
        optionButtons.add(view.findViewById(R.id.btn_option_1))
        optionButtons.add(view.findViewById(R.id.btn_option_2))
        optionButtons.add(view.findViewById(R.id.btn_option_3))
    }

    private fun setupClickListeners() {
        optionButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                viewModel.submitAnswer(index)
            }
        }

        btnNext.setOnClickListener {
            viewModel.nextQuestion()
        }

        btnFinishQuiz.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is QuizUiState.Loading -> showLoading()
                    is QuizUiState.Active -> showActiveQuestion(state)
                    is QuizUiState.Completed -> showCompleted(state)
                    is QuizUiState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }
                }
            }
        }
    }

    private fun showLoading() {
        progressLoading.visibility = View.VISIBLE
        layoutOptions.visibility = View.GONE
        tvQuestion.visibility = View.GONE
        tvProgress.visibility = View.GONE
        tvScore.visibility = View.GONE
        btnNext.visibility = View.GONE
        tvExplanation.visibility = View.GONE
    }

    private fun showActiveQuestion(state: QuizUiState.Active) {
        progressLoading.visibility = View.GONE
        layoutOptions.visibility = View.VISIBLE
        tvQuestion.visibility = View.VISIBLE
        tvProgress.visibility = View.VISIBLE
        tvScore.visibility = View.VISIBLE

        tvProgress.text = "Question ${state.currentIndex} of ${state.totalQuestions}"
        tvScore.text = "Score: ${state.correctAnswers}"
        tvQuestion.text = state.currentQuestion.questionText

        state.currentQuestion.options.forEachIndexed { index, optionText ->
            if (index < optionButtons.size) {
                val button = optionButtons[index]
                button.text = optionText
                button.isEnabled = !state.isAnswered
                
                if (state.isAnswered) {
                    if (index == state.currentQuestion.correctOptionIndex) {
                        button.setBackgroundColor(resources.getColor(android.R.color.holo_green_light, null))
                    } else if (index == state.selectedOptionIndex) {
                        button.setBackgroundColor(resources.getColor(android.R.color.holo_red_light, null))
                    } else {
                        button.setBackgroundColor(resources.getColor(android.R.color.transparent, null))
                    }
                } else {
                    button.setBackgroundColor(resources.getColor(android.R.color.transparent, null))
                }
            }
        }

        if (state.isAnswered) {
            btnNext.visibility = View.VISIBLE
            tvExplanation.visibility = View.VISIBLE
            tvExplanation.text = "Explanation: ${state.currentQuestion.explanation}"
        } else {
            btnNext.visibility = View.GONE
            tvExplanation.visibility = View.GONE
        }
    }

    private fun showCompleted(state: QuizUiState.Completed) {
        layoutCompleted.visibility = View.VISIBLE
        tvFinalScore.text = "You scored ${state.score} out of ${state.totalQuestions}!"
    }

    companion object {
        const val ARG_DECK_ID = "deckId"

        fun newInstance(deckId: String) = QuizFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_DECK_ID, deckId)
            }
        }
    }
}
