package com.example.brainbyte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.brainbyte.data.BrainByteDatabase
import com.example.brainbyte.data.entity.Deck
import com.example.brainbyte.data.repository.FlashcardRepository
import com.example.brainbyte.ocr.DocumentScannerFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var tvTotalDecks: TextView
    private lateinit var tvTotalCards: TextView
    private lateinit var cardContinueStudying: MaterialCardView
    private lateinit var tvRecentDeckName: TextView
    private lateinit var tvRecentDeckCount: TextView
    private lateinit var tvEmptyDecksMsg: TextView
    private lateinit var btnStudyNow: MaterialButton
    private lateinit var actionScan: MaterialCardView
    private lateinit var actionCreate: MaterialCardView

    private lateinit var repository: FlashcardRepository
    private var mostRecentDeck: Deck? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initDatabase()
        initViews(view)
        setupClickListeners()
        loadData()
    }

    private fun initDatabase() {
        val database = BrainByteDatabase.getDatabase(requireContext())
        repository = FlashcardRepository(database)
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
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DocumentScannerFragment.newInstance())
                .addToBackStack(null)
                .commit()
        }

        actionCreate.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ImportFragment2.newInstance())
                .addToBackStack(null)
                .commit()
        }

        btnStudyNow.setOnClickListener {
            mostRecentDeck?.let { deck ->
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, StudyFragment.newInstance(deck.id, deck.name))
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getAllDecks().collectLatest { decks ->
                tvTotalDecks.text = decks.size.toString()
                
                var totalCards = 0
                for (deck in decks) {
                    totalCards += repository.getFlashcardCountByDeck(deck.id)
                }
                tvTotalCards.text = totalCards.toString()

                if (decks.isNotEmpty()) {
                    val recentDeck = decks.maxByOrNull { it.updatedAt }
                    mostRecentDeck = recentDeck
                    if (recentDeck != null) {
                        tvRecentDeckName.text = recentDeck.name
                        val count = repository.getFlashcardCountByDeck(recentDeck.id)
                        tvRecentDeckCount.text = "$count cards"
                        
                        cardContinueStudying.visibility = View.VISIBLE
                        tvEmptyDecksMsg.visibility = View.GONE
                    }
                } else {
                    cardContinueStudying.visibility = View.GONE
                    tvEmptyDecksMsg.visibility = View.VISIBLE
                }
            }
        }
    }
}