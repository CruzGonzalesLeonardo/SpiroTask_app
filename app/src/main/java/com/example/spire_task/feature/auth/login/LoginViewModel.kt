package com.example.spire_task.feature.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spire_task.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    // Verificar si ya existe un invitado y navegar directamente
    suspend fun checkAndHandleGuest(): GuestCheckResult {
        val existeInvitado = authRepository.existeUsuarioInvitado()
        return if (existeInvitado) {
            val invitado = authRepository.getUsuarioInvitado()
            if (invitado != null) {
                // Login automático con el invitado existente
                authRepository.loginAsGuest(invitado.userName)
                GuestCheckResult.Exists(invitado.idUser, invitado.userName)
            } else {
                GuestCheckResult.None
            }
        } else {
            GuestCheckResult.None
        }
    }

    // Login con Google
    fun loginWithGoogle() {
        _uiState.value = LoginUiState(errorMessage = "Google Sign-In próximo a implementar")
    }

    fun resetState() {
        _uiState.value = LoginUiState()
    }
}

sealed class GuestCheckResult {
    data class Exists(val userId: String, val userName: String) : GuestCheckResult()
    object None : GuestCheckResult()
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val userId: String? = null,
    val userName: String? = null
)