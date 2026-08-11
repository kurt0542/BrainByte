package com.example.brainbyte.ui.analytics

import com.example.brainbyte.R

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.example.brainbyte.data.entity.StudySession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AnalyticsHistoryAdapter(
    private var items: List<StudySession>
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
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val dateString = dateFormat.format(Date(item.startTime))
        
        holder.quizTitle.text = "Session on $dateString"
        holder.quizScore.text = "${item.correctCount}/${item.totalCards}"
    }

    override fun getItemCount() = items.size
    
    fun updateData(newItems: List<StudySession>) {
        items = newItems
        notifyDataSetChanged()
    }
}
