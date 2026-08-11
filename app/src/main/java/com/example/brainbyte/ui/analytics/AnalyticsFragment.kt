package com.example.brainbyte.ui.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.brainbyte.R
import com.google.android.material.progressindicator.CircularProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AnalyticsFragment : Fragment() {

    private val viewModel: AnalyticsViewModel by viewModels()

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
        setupRecyclerView()
        observeViewModel()
    }

    private fun initViews(view: View) {
        circularChart = view.findViewById(R.id.circularChart)
        percentageText = view.findViewById(R.id.percentageText)
        historyRecyclerView = view.findViewById(R.id.historyRecyclerView)
    }

    private fun setupRecyclerView() {
        adapter = AnalyticsHistoryAdapter(emptyList())
        historyRecyclerView.layoutManager = LinearLayoutManager(context)
        historyRecyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                circularChart.progress = state.accuracyPercentage
                percentageText.text = "${state.accuracyPercentage}%"
                adapter.updateData(state.history)
            }
        }
    }
}
