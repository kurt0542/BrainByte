package com.example.brainbyte.ocr

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.brainbyte.R
import com.google.android.material.textfield.TextInputEditText
import android.widget.TextView

data class FlashcardPair(
    var term: String = "",
    var definition: String = ""
)

class ScannedFlashcardAdapter(
    private val flashcards: MutableList<FlashcardPair>,
    private val onDeleteClick: (Int) -> Unit,
    private val onFlashcardChanged: () -> Unit
) : RecyclerView.Adapter<ScannedFlashcardAdapter.FlashcardViewHolder>() {

    inner class FlashcardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardNumberBadge: TextView = itemView.findViewById(R.id.cardNumberBadge)
        val termEditText: TextInputEditText = itemView.findViewById(R.id.itemTermEditText)
        val definitionEditText: TextInputEditText = itemView.findViewById(R.id.itemDefinitionEditText)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteCard)

        private var termWatcher: TextWatcher? = null
        private var definitionWatcher: TextWatcher? = null

        fun bind(flashcard: FlashcardPair, position: Int) {
            cardNumberBadge.text = (position + 1).toString()

            termWatcher?.let { termEditText.removeTextChangedListener(it) }
            definitionWatcher?.let { definitionEditText.removeTextChangedListener(it) }

            termEditText.setText(flashcard.term)
            definitionEditText.setText(flashcard.definition)

            termWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    flashcard.term = s.toString()
                    onFlashcardChanged()
                }
            }

            definitionWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    flashcard.definition = s.toString()
                    onFlashcardChanged()
                }
            }

            termEditText.addTextChangedListener(termWatcher)
            definitionEditText.addTextChangedListener(definitionWatcher)

            btnDelete.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onDeleteClick(pos)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlashcardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scanned_flashcard, parent, false)
        return FlashcardViewHolder(view)
    }

    override fun onBindViewHolder(holder: FlashcardViewHolder, position: Int) {
        holder.bind(flashcards[position], position)
    }

    override fun getItemCount(): Int = flashcards.size

    fun addFlashcard(flashcard: FlashcardPair) {
        flashcards.add(flashcard)
        notifyItemInserted(flashcards.size - 1)
    }

    fun removeFlashcard(position: Int) {
        if (position in flashcards.indices) {
            flashcards.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, flashcards.size - position)
        }
    }

    fun clearAll() {
        val size = flashcards.size
        flashcards.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun getFlashcards(): List<FlashcardPair> = flashcards.toList()
}
