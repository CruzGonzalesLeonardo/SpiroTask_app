package com.example.spire_task.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.spire_task.data.local.entities.ProductEntity
import com.example.spire_task.data.repository.AuthRepository
import com.example.spire_task.data.repository.StoreRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val storeRepository: StoreRepository,
    private val userId: String
) : ViewModel() {

    val purchaseHistory: StateFlow<List<ProductEntity>> = storeRepository.getOwnedProducts(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateProfile(newName: String, newEmail: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = authRepository.updateProfile(userId, newName, newEmail)
            if (result.isSuccess) {
                onSuccess()
            }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = authRepository.deleteAccount(userId)
            if (result.isSuccess) {
                onSuccess()
            }
        }
    }
}

class ProfileViewModelFactory(
    private val authRepository: AuthRepository,
    private val storeRepository: StoreRepository,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(authRepository, storeRepository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
