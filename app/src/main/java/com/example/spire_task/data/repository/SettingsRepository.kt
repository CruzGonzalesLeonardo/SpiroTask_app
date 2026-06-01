package com.example.spire_task.data.repository

import com.example.spire_task.data.local.dao.SettingsDao
import com.example.spire_task.data.local.entities.SettingsEntity
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val settingsDao: SettingsDao) {

    fun getSettings(userId: String): Flow<SettingsEntity?> = settingsDao.getSettings(userId)

    suspend fun updateSettings(settings: SettingsEntity) {
        settingsDao.saveSettings(settings)
    }

    suspend fun deleteSettings(userId: String) {
        settingsDao.deleteSettings(userId)
    }
}
