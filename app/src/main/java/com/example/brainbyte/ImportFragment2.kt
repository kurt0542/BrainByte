package com.example.brainbyte

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ImportFragment2 : Fragment() {

    private lateinit var decksRecyclerView: RecyclerView
    private lateinit var createBtn: Button
    private lateinit var viewAllText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_import2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupClickListeners()
    }

    private fun initViews(view: View) {
        decksRecyclerView = view.findViewById(R.id.decks_recycler_view)
        createBtn = view.findViewById(R.id.createBtn)
        viewAllText = view.findViewById(R.id.view_all_text)
    }

    private fun setupRecyclerView() {
        decksRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun setupClickListeners() {
        createBtn.setOnClickListener {
            navigateToAddFlashcards("New Deck")
        }

        viewAllText.setOnClickListener {
        }
    }

    private fun navigateToAddFlashcards(deckName: String) {
        val recognizedText = arguments?.getString("recognizedText")

        val fragment = ImportFragment3.newInstance(
            deckName = deckName,
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