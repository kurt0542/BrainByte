package com.example.brainbyte.ui.analytics

import com.example.brainbyte.R

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class QuizHistoryItem(
    val title: String,
    val score: String
)

class AnalyticsHistoryAdapter(
    private val items: List<QuizHistoryItem>
) : RecyclerView.Adapter<AnalyticsHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val quizTitle: TextView = view.findViewById(R.id.quizTitle)
        val quizScore: TextView = view.findViewById(R.id.quizScore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quiz_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.quizTitle.text = item.title
        holder.quizScore.text = item.score
    }

    override fun getItemCount() = items.size
}
