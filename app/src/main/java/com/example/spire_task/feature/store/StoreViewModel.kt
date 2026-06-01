package com.example.spire_task.feature.store

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.spire_task.data.local.entities.ProductEntity
import com.example.spire_task.data.repository.StoreRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StoreViewModel(
    private val repository: StoreRepository,
    private val userId: String
) : ViewModel() {

    var purchaseStatus by mutableStateOf<Result<Unit>?>(null)
        private set

    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ownedProducts: StateFlow<List<ProductEntity>> = repository.getOwnedProducts(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePet: StateFlow<ProductEntity?> = repository.getActivePet(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            repository.initStoreCatalog()
        }
    }

    fun purchaseProduct(productId: String) {
        viewModelScope.launch {
            val result = repository.purchaseProduct(userId, productId)
            purchaseStatus = result
        }
    }

    fun clearPurchaseStatus() {
        purchaseStatus = null
    }

    fun activatePet(productId: String) {
        viewModelScope.launch {
            repository.activatePet(userId, productId)
        }
    }
}

class StoreViewModelFactory(
    private val repository: StoreRepository,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoreViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StoreViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
