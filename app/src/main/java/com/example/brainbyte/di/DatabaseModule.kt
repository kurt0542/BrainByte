package com.example.brainbyte.di

import android.content.Context
import com.example.brainbyte.data.BrainByteDatabase
import com.example.brainbyte.data.dao.DeckDao
import com.example.brainbyte.data.dao.FlashcardDao
import com.example.brainbyte.data.repository.FlashcardRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BrainByteDatabase {
        return BrainByteDatabase.getDatabase(context)
    }

    @Provides
    fun provideDeckDao(database: BrainByteDatabase): DeckDao {
        return database.deckDao()
    }

    @Provides
    fun provideFlashcardDao(database: BrainByteDatabase): FlashcardDao {
        return database.flashcardDao()
    }

    @Provides
    @Singleton
    fun provideFlashcardRepository(database: BrainByteDatabase): FlashcardRepository {
        return FlashcardRepository(database)
    }
}
