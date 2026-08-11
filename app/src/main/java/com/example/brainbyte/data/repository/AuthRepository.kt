package com.example.brainbyte.data.repository

import io.appwrite.services.Account
import io.appwrite.models.User
import io.appwrite.models.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val account: Account
) {
    suspend fun createAccount(email: String, password: String, name: String): User<Map<String, Any>> {
        return withContext(Dispatchers.IO) {
            account.create(
                userId = io.appwrite.ID.unique(),
                email = email,
                password = password,
                name = name
            )
        }
    }

    suspend fun login(email: String, password: String): Session {
        return withContext(Dispatchers.IO) {
            account.createEmailPasswordSession(
                email = email,
                password = password
            )
        }
    }

    suspend fun logout() {
        withContext(Dispatchers.IO) {
            account.deleteSession("current")
        }
    }

    suspend fun getCurrentUser(): User<Map<String, Any>> {
        return withContext(Dispatchers.IO) {
            account.get()
        }
    }
}
