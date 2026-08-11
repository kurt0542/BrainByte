package com.example.brainbyte.di

import android.content.Context
import com.example.brainbyte.constants.APPWRITE_PROJECT_ID
import com.example.brainbyte.constants.APPWRITE_PUBLIC_ENDPOINT
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.appwrite.Client
import io.appwrite.services.Account
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideAppwriteClient(@ApplicationContext context: Context): Client {
        return Client(context)
            .setEndpoint(APPWRITE_PUBLIC_ENDPOINT)
            .setProject(APPWRITE_PROJECT_ID)
    }

    @Provides
    @Singleton
    fun provideAccount(client: Client): Account {
        return Account(client)
    }
}
