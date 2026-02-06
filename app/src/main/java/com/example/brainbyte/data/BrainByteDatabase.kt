package com.example.brainbyte.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.brainbyte.data.dao.DeckDao
import com.example.brainbyte.data.dao.FlashcardDao
import com.example.brainbyte.data.entity.Deck
import com.example.brainbyte.data.entity.Flashcard

@Database(
    entities = [Deck::class, Flashcard::class],
    version = 1,
    exportSchema = false
)
abstract class BrainByteDatabase : RoomDatabase() {

    abstract fun deckDao(): DeckDao
    abstract fun flashcardDao(): FlashcardDao

    companion object {
        @Volatile
        private var INSTANCE: BrainByteDatabase? = null

        fun getDatabase(context: Context): BrainByteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BrainByteDatabase::class.java,
                    "brainbyte_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

