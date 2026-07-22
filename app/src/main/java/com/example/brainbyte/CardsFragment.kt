package com.example.brainbyte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.brainbyte.data.BrainByteDatabase
import com.example.brainbyte.data.entity.Deck
import com.example.brainbyte.data.repository.FlashcardRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CardsFragment : Fragment() {

    private lateinit var flashcardsRecyclerView: RecyclerView
    private lateinit var recentlyViewedRecyclerView: RecyclerView
    private lateinit var createFlashcardBtn: Button
    private lateinit var flashcardsViewAll: TextView
    private lateinit var recentlyViewedViewAll: TextView

    private lateinit var repository: FlashcardRepository
    private lateinit var deckAdapter: DeckAdapter
    private lateinit var recentDeckAdapter: DeckAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cards, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initDatabase()
        initViews(view)
        setupRecyclerViews()
        setupClickListeners()
        observeDecks()
    }

    private fun initDatabase() {
        val database = BrainByteDatabase.getDatabase(requireContext())
        repository = FlashcardRepository(database)
    }

    private fun initViews(view: View) {
        flashcardsRecyclerView = view.findViewById(R.id.flashcards_recycler_view)
        recentlyViewedRecyclerView = view.findViewById(R.id.recently_viewed_recycler_view)
        createFlashcardBtn = view.findViewById(R.id.create_flashcard_btn)
        flashcardsViewAll = view.findViewById(R.id.flashcards_view_all)
        recentlyViewedViewAll = view.findViewById(R.id.recently_viewed_view_all)
    }

    private fun setupRecyclerViews() {
        // Main decks adapter
        deckAdapter = DeckAdapter(
            onDeckClick = { deck -> navigateToStudy(deck) },
            getCardCount = { deckId -> repository.getFlashcardCountByDeck(deckId) }
        )
        flashcardsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = deckAdapter
        }

        // Recently viewed adapter (same functionality for now)
        recentDeckAdapter = DeckAdapter(
            onDeckClick = { deck -> navigateToStudy(deck) },
            getCardCount = { deckId -> repository.getFlashcardCountByDeck(deckId) }
        )
        recentlyViewedRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recentDeckAdapter
        }
    }

    private fun setupClickListeners() {
        createFlashcardBtn.setOnClickListener {
            // Navigate to import/create flashcard flow
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ImportFragment2())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun observeDecks() {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getAllDecks().collectLatest { decks ->
                deckAdapter.submitList(decks)

                // Load card counts for each deck
                decks.forEach { deck ->
                    launch {
                        val count = repository.getFlashcardCountByDeck(deck.id)
                        deckAdapter.updateCardCount(deck.id, count)
                    }
                }

                // For recently viewed, show the 3 most recent
                val recentDecks = decks.sortedByDescending { it.updatedAt }.take(3)
                recentDeckAdapter.submitList(recentDecks)
                recentDecks.forEach { deck ->
                    launch {
                        val count = repository.getFlashcardCountByDeck(deck.id)
                        recentDeckAdapter.updateCardCount(deck.id, count)
                    }
                }
            }
        }
    }

    private fun navigateToStudy(deck: Deck) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, StudyFragment.newInstance(deck.id, deck.name))
            .addToBackStack(null)
            .commit()
    }

    companion object {
        @JvmStatic
        fun newInstance() = CardsFragment()
    }
}