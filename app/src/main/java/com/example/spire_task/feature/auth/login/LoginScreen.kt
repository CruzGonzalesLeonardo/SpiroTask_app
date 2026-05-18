package com.example.spire_task.feature.auth.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (userId: String, userName: String) -> Unit,
    onNavigateToLocalRegister: () -> Unit   // Invitado → crear cuenta local
) {
    val uiState by viewModel.uiState.collectAsState()

    // Observar login exitoso (para Google)
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess && uiState.userId != null && uiState.userName != null) {
            onLoginSuccess(uiState.userId!!, uiState.userName!!)
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Spire Task",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Botón: Continuar como invitado (crea cuenta local con nombre)
        Button(
            onClick = onNavigateToLocalRegister,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar como invitado")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón: Iniciar con Google (próximamente)
        OutlinedButton(
            onClick = { viewModel.loginWithGoogle() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar con Google")
        }

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.errorMessage!!,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}