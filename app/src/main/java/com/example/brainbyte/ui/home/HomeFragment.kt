package com.example.brainbyte.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.brainbyte.R
import com.example.brainbyte.data.entity.Deck
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var tvTotalDecks: TextView
    private lateinit var tvTotalCards: TextView
    private lateinit var cardContinueStudying: MaterialCardView
    private lateinit var tvRecentDeckName: TextView
    private lateinit var tvRecentDeckCount: TextView
    private lateinit var tvEmptyDecksMsg: TextView
    private lateinit var btnStudyNow: MaterialButton
    private lateinit var actionScan: MaterialCardView
    private lateinit var actionCreate: MaterialCardView

    private var mostRecentDeck: Deck? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupClickListeners()
        observeViewModel()
    }

    private fun initViews(view: View) {
        tvTotalDecks = view.findViewById(R.id.tv_total_decks)
        tvTotalCards = view.findViewById(R.id.tv_total_cards)
        cardContinueStudying = view.findViewById(R.id.card_continue_studying)
        tvRecentDeckName = view.findViewById(R.id.tv_recent_deck_name)
        tvRecentDeckCount = view.findViewById(R.id.tv_recent_deck_count)
        tvEmptyDecksMsg = view.findViewById(R.id.tv_empty_decks_msg)
        btnStudyNow = view.findViewById(R.id.btn_study_now)
        actionScan = view.findViewById(R.id.action_scan)
        actionCreate = view.findViewById(R.id.action_create)
    }

    private fun setupClickListeners() {
        actionScan.setOnClickListener {
            findNavController().navigate(R.id.documentScannerFragment)
        }

        actionCreate.setOnClickListener {
            findNavController().navigate(R.id.importFragment2)
        }

        btnStudyNow.setOnClickListener {
            mostRecentDeck?.let { deck ->
                val action = HomeFragmentDirections.actionHomeFragmentToStudyFragment(deck.id, deck.name)
                findNavController().navigate(action)
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                tvTotalDecks.text = state.totalDecks.toString()
                tvTotalCards.text = state.totalCards.toString()

                if (state.recentDeck != null) {
                    mostRecentDeck = state.recentDeck
                    tvRecentDeckName.text = state.recentDeck.name
                    tvRecentDeckCount.text = "${state.recentDeckCardCount} cards"
                    
                    cardContinueStudying.visibility = View.VISIBLE
                    tvEmptyDecksMsg.visibility = View.GONE
                } else {
                    cardContinueStudying.visibility = View.GONE
                    tvEmptyDecksMsg.visibility = View.VISIBLE
                }
            }
        }
    }

}