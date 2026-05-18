package com.example.spire_task.feature.auth.login

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spire_task.data.remote.auth.GoogleSignInManager
import com.example.spire_task.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val context: Context
) : ViewModel() {

    private val googleSignInManager = GoogleSignInManager(context)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun getGoogleSignInIntent(): Intent {
        return googleSignInManager.getSignInIntent()
    }

    suspend fun handleGoogleSignInResult(data: Intent?): Boolean {
        return try {
            val account = googleSignInManager.handleSignInResult(data).getOrNull()
                ?: return false

            val authResult = googleSignInManager.firebaseAuthWithGoogle(account).getOrNull()
                ?: return false

            val firebaseUser = authResult.user
            val userId = firebaseUser?.uid ?: return false
            val userName = firebaseUser.displayName ?: firebaseUser.email?.split("@")?.first() ?: "Usuario"

            // Guardar en nuestra base de datos local
            val result = authRepository.loginWithGoogleAccount(account)

            if (result.isSuccess) {
                _uiState.value = LoginUiState(
                    isSuccess = true,
                    userId = userId,
                    userName = userName
                )
                true
            } else {
                _uiState.value = LoginUiState(
                    errorMessage = result.exceptionOrNull()?.message ?: "Error al guardar usuario"
                )
                false
            }
        } catch (e: Exception) {
            _uiState.value = LoginUiState(errorMessage = e.message ?: "Error desconocido")
            false
        }
    }

    suspend fun checkAndHandleGuest(): GuestCheckResult {
        val existeInvitado = authRepository.existeUsuarioInvitado()
        return if (existeInvitado) {
            val invitado = authRepository.getUsuarioInvitado()
            if (invitado != null) {
                GuestCheckResult.Exists(invitado.idUser, invitado.userName)
            } else {
                GuestCheckResult.None
            }
        } else {
            GuestCheckResult.None
        }
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