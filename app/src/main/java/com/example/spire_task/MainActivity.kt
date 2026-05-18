package com.example.spire_task

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.spire_task.data.repository.AuthRepository
import com.example.spire_task.feature.auth.localregister.LocalRegisterScreen
import com.example.spire_task.feature.auth.localregister.LocalRegisterViewModel
import com.example.spire_task.feature.auth.login.GuestCheckResult
import com.example.spire_task.feature.auth.login.LoginScreen
import com.example.spire_task.feature.auth.login.LoginViewModel
import com.example.spire_task.feature.dashboard.DashboardScreen
import com.example.spire_task.ui.theme.Spire_TaskTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Spire_TaskTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf("login") }
                    var currentUserName by remember { mutableStateOf("") }
                    val scope = rememberCoroutineScope()

                    val authRepository = remember { AuthRepository(this@MainActivity) }
                    val loginViewModel = remember { LoginViewModel(authRepository, this@MainActivity) }
                    val localRegisterViewModel = remember { LocalRegisterViewModel(authRepository) }

                    // Verificar sesión guardada al iniciar
                    LaunchedEffect(Unit) {
                        if (authRepository.isUserLoggedIn()) {
                            currentUserName = authRepository.getCurrentUserName() ?: ""
                            currentScreen = "dashboard"
                        }
                    }

                    when (currentScreen) {
                        "dashboard" -> {
                            DashboardScreen(
                                userName = currentUserName,
                                onLogout = {
                                    authRepository.logout()
                                    currentScreen = "login"
                                    currentUserName = ""
                                    loginViewModel.resetState()
                                    localRegisterViewModel.resetState()
                                }
                            )
                        }
                        "local_register" -> {
                            LocalRegisterScreen(
                                viewModel = localRegisterViewModel,
                                onRegisterSuccess = { userId, userName ->
                                    currentUserName = userName
                                    currentScreen = "dashboard"
                                },
                                onNavigateBack = {
                                    currentScreen = "login"
                                    localRegisterViewModel.resetState()
                                }
                            )
                        }
                        else -> {
                            LoginScreen(
                                viewModel = loginViewModel,
                                onLoginSuccess = { userId, userName ->
                                    currentUserName = userName
                                    currentScreen = "dashboard"
                                },
                                onNavigateToLocalRegister = {
                                    // Verificar si ya existe un invitado antes de ir al registro
                                    scope.launch {
                                        val result = loginViewModel.checkAndHandleGuest()
                                        when (result) {
                                            is GuestCheckResult.Exists -> {
                                                // Invitado existe, iniciar sesión automáticamente
                                                Log.d("GUEST", "Invitado existente: ${result.userName}")
                                                currentUserName = result.userName
                                                currentScreen = "dashboard"
                                            }
                                            GuestCheckResult.None -> {
                                                // No existe invitado, ir a pantalla de registro
                                                currentScreen = "local_register"
                                                loginViewModel.resetState()
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}