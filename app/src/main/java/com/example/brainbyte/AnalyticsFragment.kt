package com.example.brainbyte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator

class AnalyticsFragment : Fragment() {

    private lateinit var circularChart: CircularProgressIndicator
    private lateinit var percentageText: TextView
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var adapter: AnalyticsHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_analytics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupMockData()
    }

    private fun initViews(view: View) {
        circularChart = view.findViewById(R.id.circularChart)
        percentageText = view.findViewById(R.id.percentageText)
        historyRecyclerView = view.findViewById(R.id.historyRecyclerView)
    }

    private fun setupMockData() {
        // Mock overall progress
        val mockPercentage = 85
        circularChart.progress = mockPercentage
        percentageText.text = "$mockPercentage%"

        // Mock quiz history
        val mockHistory = listOf(
            QuizHistoryItem("Biology 101 - Cell Structure", "9/10"),
            QuizHistoryItem("Spanish Vocabulary - Week 3", "18/20"),
            QuizHistoryItem("World History - WWII", "7/10"),
            QuizHistoryItem("Chemistry - Periodic Table", "10/10"),
            QuizHistoryItem("Geography - Capitals", "14/15")
        )

        adapter = AnalyticsHistoryAdapter(mockHistory)
        historyRecyclerView.layoutManager = LinearLayoutManager(context)
        historyRecyclerView.adapter = adapter
    }
}