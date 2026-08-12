package com.example.brainbyte.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.brainbyte.R
import com.example.brainbyte.constants.APPWRITE_PROJECT_ID
import com.example.brainbyte.constants.APPWRITE_PUBLIC_ENDPOINT
import com.example.brainbyte.data.entity.Deck
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Avatars
import kotlinx.coroutines.launch
import android.graphics.BitmapFactory

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
    private lateinit var btnHomeTakeQuiz: MaterialButton
    private lateinit var actionScan: MaterialCardView
    private lateinit var actionCreate: MaterialCardView
    private lateinit var profileIcon: ImageView

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
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val avatarFile = java.io.File(requireContext().cacheDir, "avatar.png")
        if (avatarFile.exists()) {
            val bitmap = BitmapFactory.decodeFile(avatarFile.absolutePath)
            profileIcon.setImageBitmap(bitmap)
            profileIcon.alpha = 1f
            return
        }

        val client = Client(requireContext())
            .setEndpoint(APPWRITE_PUBLIC_ENDPOINT)
            .setProject(APPWRITE_PROJECT_ID)
        val account = Account(client)
        val avatars = Avatars(client)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = account.get()
                val initialsBytes = avatars.getInitials(name = user.name)
                
                avatarFile.writeBytes(initialsBytes)
                
                val bitmap = BitmapFactory.decodeByteArray(initialsBytes, 0, initialsBytes.size)
                profileIcon.setImageBitmap(bitmap)
                profileIcon.animate().alpha(1f).setDuration(300).start()
            } catch (e: Exception) {
                profileIcon.setImageResource(R.drawable.ic_profile)
                profileIcon.animate().alpha(1f).setDuration(300).start()
            }
        }
    }

    private fun initViews(view: View) {
        tvTotalDecks = view.findViewById(R.id.tv_total_decks)
        tvTotalCards = view.findViewById(R.id.tv_total_cards)
        cardContinueStudying = view.findViewById(R.id.card_continue_studying)
        tvRecentDeckName = view.findViewById(R.id.tv_recent_deck_name)
        tvRecentDeckCount = view.findViewById(R.id.tv_recent_deck_count)
        tvEmptyDecksMsg = view.findViewById(R.id.tv_empty_decks_msg)
        btnStudyNow = view.findViewById(R.id.btn_study_now)
        btnHomeTakeQuiz = view.findViewById(R.id.btn_home_take_quiz)
        actionScan = view.findViewById(R.id.action_scan)
        actionCreate = view.findViewById(R.id.action_create)
        profileIcon = view.findViewById(R.id.profile_icon)
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

        btnHomeTakeQuiz.setOnClickListener {
            mostRecentDeck?.let { deck ->
                val action = HomeFragmentDirections.actionHomeFragmentToQuizFragment(deck.id)
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