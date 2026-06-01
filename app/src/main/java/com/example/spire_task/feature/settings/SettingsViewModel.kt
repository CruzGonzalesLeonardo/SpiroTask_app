package com.example.spire_task.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.spire_task.data.local.entities.SettingsEntity
import com.example.spire_task.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val userId: String
) : ViewModel() {

    val settings: StateFlow<SettingsEntity?> = repository.getSettings(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value ?: SettingsEntity(userId)
            repository.updateSettings(current.copy(isDarkMode = enabled))
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value ?: SettingsEntity(userId)
            repository.updateSettings(current.copy(notificationsEnabled = enabled))
        }
    }

    fun updateSleepTime(start: String, end: String) {
        viewModelScope.launch {
            val current = settings.value ?: SettingsEntity(userId)
            repository.updateSettings(current.copy(petSleepStartTime = start, petSleepEndTime = end))
        }
    }
}

class SettingsViewModelFactory(
    private val repository: SettingsRepository,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
