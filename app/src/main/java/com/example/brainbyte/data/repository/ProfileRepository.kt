package com.example.brainbyte.data.repository

import com.example.brainbyte.data.dao.ProfileDao
import com.example.brainbyte.data.entity.Profile
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val profileDao: ProfileDao
) {
    suspend fun getProfile(userId: String): Profile? = profileDao.getProfile(userId)

    suspend fun insertProfile(profile: Profile) = profileDao.insertProfile(profile)

    suspend fun updateProfile(profile: Profile) = profileDao.updateProfile(profile)
}
