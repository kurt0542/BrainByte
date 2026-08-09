package com.example.brainbyte.ui.deck

import com.example.brainbyte.R

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.brainbyte.data.entity.Deck
import java.util.concurrent.TimeUnit

class DeckAdapter(
    private val onDeckClick: (Deck) -> Unit,
    private val getCardCount: suspend (String) -> Int
) : ListAdapter<Deck, DeckAdapter.DeckViewHolder>(DeckDiffCallback()) {

    private val cardCounts = mutableMapOf<String, Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.deck_item, parent, false)
        return DeckViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeckViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateCardCount(deckId: String, count: Int) {
        cardCounts[deckId] = count
        val position = currentList.indexOfFirst { it.id == deckId }
        if (position >= 0) {
            notifyItemChanged(position)
        }
    }

    inner class DeckViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val deckName: TextView = itemView.findViewById(R.id.deck_name)
        private val cardCount: TextView = itemView.findViewById(R.id.card_count)
        private val lastUpdated: TextView = itemView.findViewById(R.id.last_updated)

        fun bind(deck: Deck) {
            deckName.text = deck.name

            // Display card count
            val count = cardCounts[deck.id] ?: 0
            cardCount.text = count.toString()

            // Display relative time
            lastUpdated.text = getRelativeTimeSpan(deck.updatedAt)

            itemView.setOnClickListener {
                onDeckClick(deck)
            }
        }

        private fun getRelativeTimeSpan(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
                diff < TimeUnit.HOURS.toMillis(1) -> {
                    val mins = TimeUnit.MILLISECONDS.toMinutes(diff)
                    "${mins}min ago"
                }
                diff < TimeUnit.DAYS.toMillis(1) -> {
                    val hours = TimeUnit.MILLISECONDS.toHours(diff)
                    "${hours}h ago"
                }
                diff < TimeUnit.DAYS.toMillis(7) -> {
                    val days = TimeUnit.MILLISECONDS.toDays(diff)
                    "${days}d ago"
                }
                diff < TimeUnit.DAYS.toMillis(30) -> {
                    val weeks = TimeUnit.MILLISECONDS.toDays(diff) / 7
                    "${weeks}w ago"
                }
                diff < TimeUnit.DAYS.toMillis(365) -> {
                    val months = TimeUnit.MILLISECONDS.toDays(diff) / 30
                    "${months}mo ago"
                }
                else -> {
                    val years = TimeUnit.MILLISECONDS.toDays(diff) / 365
                    "${years}y ago"
                }
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

