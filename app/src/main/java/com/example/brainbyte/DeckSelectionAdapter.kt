package com.example.brainbyte

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.brainbyte.data.entity.Deck
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DeckSelectionAdapter(
    private val onDeckClick: (Deck) -> Unit
) : ListAdapter<Deck, DeckSelectionAdapter.DeckViewHolder>(DeckDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_deck_selection, parent, false)
        return DeckViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeckViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DeckViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val deckName: TextView = itemView.findViewById(R.id.deckNameText)
        private val deckDate: TextView = itemView.findViewById(R.id.deckDateText)

        fun bind(deck: Deck) {
            deckName.text = deck.name

            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            deckDate.text = "Updated: ${dateFormat.format(Date(deck.updatedAt))}"

            itemView.setOnClickListener {
                onDeckClick(deck)
            }
        }
    }

    class DeckDiffCallback : DiffUtil.ItemCallback<Deck>() {
        override fun areItemsTheSame(oldItem: Deck, newItem: Deck): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Deck, newItem: Deck): Boolean {
            return oldItem == newItem
        }
    }
}

