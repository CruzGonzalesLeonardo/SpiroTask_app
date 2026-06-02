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

    // ✅ Variables para la selección de primera mascota
    var availablePets by mutableStateOf<List<ProductEntity>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            repository.initStoreCatalog()
            loadAvailablePets()
        }
    }

    // ✅ Cargar todas las mascotas disponibles en la tienda
    private suspend fun loadAvailablePets() {
        isLoading = true
        try {
            val all = repository.allProducts.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList()).value
            availablePets = all.filter {
                it.type == "PET"
            }
        } catch (e: Exception) {
            errorMessage = "Error al cargar mascotas: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // ✅ Adoptar primera mascota (SIEMPRE GRATIS)
    fun adoptFirstPet(petId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                val result = repository.adoptFirstPet(userId, petId)
                if (result.isSuccess) {
                    errorMessage = null
                    onComplete(true)
                } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "No se pudo adoptar la mascota"
                    onComplete(false)
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
                onComplete(false)
            } finally {
                isLoading = false
            }
        }
    }

    fun clearError() {
        errorMessage = null
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