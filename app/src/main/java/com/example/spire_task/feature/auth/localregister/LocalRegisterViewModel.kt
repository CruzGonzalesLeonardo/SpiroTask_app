package com.example.spire_task.feature.auth.localregister

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spire_task.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LocalRegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocalRegisterUiState())
    val uiState: StateFlow<LocalRegisterUiState> = _uiState

    fun registerLocalUser(userName: String) {
        if (userName.isBlank()) {
            _uiState.value = LocalRegisterUiState(errorMessage = "Ingresa un nombre de usuario")
            return
        }

        viewModelScope.launch {
            _uiState.value = LocalRegisterUiState(isLoading = true)

            val result = authRepository.registerLocalUser(userName)

            _uiState.value = when {
                result.isSuccess -> {
                    LocalRegisterUiState(
                        isSuccess = true,
                        userId = result.getOrNull()?.idUser,
                        userName = userName
                    )
                }
                result.isFailure -> {
                    LocalRegisterUiState(
                        errorMessage = result.exceptionOrNull()?.message ?: "Error desconocido"
                    )
                }
                else -> LocalRegisterUiState(errorMessage = "Error inesperado")
            }
        }
    }

    fun resetState() {
        _uiState.value = LocalRegisterUiState()
    }
}

data class LocalRegisterUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val userId: String? = null,
    val userName: String? = null
)