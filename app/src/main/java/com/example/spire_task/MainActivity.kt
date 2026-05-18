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
                    var currentUserEmail by remember { mutableStateOf("") }
                    var currentUserId by remember { mutableStateOf("") }
                    var currentAuthProvider by remember { mutableStateOf("local") }
                    val scope = rememberCoroutineScope()

                    val authRepository = remember { AuthRepository(this@MainActivity) }
                    val loginViewModel = remember { LoginViewModel(authRepository, this@MainActivity) }
                    val localRegisterViewModel = remember { LocalRegisterViewModel(authRepository) }

                    // Verificar sesión guardada al iniciar
                    LaunchedEffect(Unit) {
                        if (authRepository.isUserLoggedIn()) {
                            currentUserName = authRepository.getCurrentUserName() ?: ""
                            currentUserId = authRepository.getCurrentUserId() ?: ""
                            currentUserEmail = authRepository.getCurrentUserEmail() ?: ""
                            currentAuthProvider = authRepository.getCurrentAuthProvider() ?: "local"
                            currentScreen = "dashboard"
                        }
                    }

                    when (currentScreen) {
                        "dashboard" -> {
                            DashboardScreen(
                                userName = currentUserName,
                                userEmail = currentUserEmail,
                                userId = currentUserId,
                                authProvider = currentAuthProvider,
                                level = 1,
                                xp = 0f,
                                monedas = 0,
                                racha = 0,
                                onLogout = {
                                    authRepository.logout()
                                    currentScreen = "login"
                                    currentUserName = ""
                                    currentUserEmail = ""
                                    currentUserId = ""
                                    currentAuthProvider = "local"
                                    loginViewModel.resetState()
                                    localRegisterViewModel.resetState()
                                }
                            )
                        }
                        "local_register" -> {
                            LocalRegisterScreen(
                                viewModel = localRegisterViewModel,
                                onRegisterSuccess = { userId, userName ->
                                    currentUserId = userId
                                    currentUserName = userName
                                    currentUserEmail = ""
                                    currentAuthProvider = "local"
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
                                onLoginSuccess = { userId, userName, email, authProvider ->
                                    currentUserId = userId
                                    currentUserName = userName
                                    currentUserEmail = email
                                    currentAuthProvider = authProvider
                                    currentScreen = "dashboard"
                                },
                                onNavigateToLocalRegister = {
                                    scope.launch {
                                        val result = loginViewModel.checkAndHandleGuest()
                                        when (result) {
                                            is GuestCheckResult.Exists -> {
                                                Log.d("GUEST", "Invitado existente: ${result.userName}")
                                                currentUserId = result.userId
                                                currentUserName = result.userName
                                                currentUserEmail = ""
                                                currentAuthProvider = "local"
                                                currentScreen = "dashboard"
                                            }
                                            GuestCheckResult.None -> {
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