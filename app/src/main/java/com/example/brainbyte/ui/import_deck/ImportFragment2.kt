package com.example.brainbyte

import com.example.brainbyte.R

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.brainbyte.data.BrainByteDatabase
import com.example.brainbyte.data.repository.FlashcardRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ImportFragment2 : Fragment() {

    private lateinit var decksRecyclerView: RecyclerView
    private lateinit var createBtn: Button
    private lateinit var viewAllText: TextView
    private lateinit var repository: FlashcardRepository
    private lateinit var deckAdapter: DeckSelectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_import2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initDatabase()
        initViews(view)
        setupRecyclerView()
        setupClickListeners()
        loadDecks()
    }

    private fun initDatabase() {
        val database = BrainByteDatabase.getDatabase(requireContext())
        repository = FlashcardRepository(database)
    }

    private fun initViews(view: View) {
        decksRecyclerView = view.findViewById(R.id.decks_recycler_view)
        createBtn = view.findViewById(R.id.createBtn)
        viewAllText = view.findViewById(R.id.view_all_text)
    }

    private fun setupRecyclerView() {
        deckAdapter = DeckSelectionAdapter(
            onDeckClick = { deck -> navigateToAddFlashcards(deck.name, deck.id) }
        )
        decksRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = deckAdapter
        }
    }

    private fun loadDecks() {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getAllDecks().collectLatest { decks ->
                deckAdapter.submitList(decks)
            }
        }
    }

    private fun setupClickListeners() {
        createBtn.setOnClickListener {
            navigateToAddFlashcards("New Deck")
        }

        viewAllText.setOnClickListener {
        }
    }

    private fun navigateToAddFlashcards(deckName: String, deckId: Long? = null) {
        val recognizedText = arguments?.getString("recognizedText")

        val fragment = ImportFragment3.newInstance(
            deckName = deckName,
            deckId = deckId,
            recognizedText = recognizedText
        )

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    companion object {
        fun newInstance(recognizedText: String? = null): ImportFragment2 {
            return ImportFragment2().apply {
                arguments = Bundle().apply {
                    recognizedText?.let { putString("recognizedText", it) }
                }
            }
        }
    }
}