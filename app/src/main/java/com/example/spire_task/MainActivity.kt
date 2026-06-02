package com.example.spire_task

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.spire_task.data.repository.AuthRepository
import com.example.spire_task.feature.auth.localregister.LocalRegisterScreen
import com.example.spire_task.feature.auth.localregister.LocalRegisterViewModel
import com.example.spire_task.feature.auth.login.GuestCheckResult
import com.example.spire_task.feature.auth.login.LoginScreen
import com.example.spire_task.feature.auth.login.LoginViewModel
import com.example.spire_task.feature.dashboard.DashboardScreen
import com.example.spire_task.feature.onboarding.SelectPetScreen
import com.example.spire_task.ui.theme.Spire_TaskTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val app = context.applicationContext as SpiroApplication
            val settingsRepo = app.settingsRepository

            var isDarkMode by remember { mutableStateOf(false) }
            var currentUserIdState by remember { mutableStateOf("") }

            val authRepository = remember { app.authRepository }

            LaunchedEffect(currentUserIdState) {
                if (currentUserIdState.isNotEmpty()) {
                    settingsRepo.getSettings(currentUserIdState).collect { settings ->
                        isDarkMode = settings?.isDarkMode ?: false
                    }
                }
            }

            Spire_TaskTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf("login") }
                    var currentUserName by remember { mutableStateOf("") }
                    var currentUserEmail by remember { mutableStateOf("") }
                    var currentUserId by remember { mutableStateOf("") }
                    var currentAuthProvider by remember { mutableStateOf("local") }

                    // ✅ Variables para la selección de mascota
                    var showPetSelection by remember { mutableStateOf(false) }
                    var tempUserId by remember { mutableStateOf("") }
                    var tempUserName by remember { mutableStateOf("") }

                    val scope = rememberCoroutineScope()

                    val loginViewModel = remember { LoginViewModel(authRepository, this@MainActivity) }
                    val localRegisterViewModel = remember { LocalRegisterViewModel(authRepository) }

                    // ✅ Verificar si el usuario ya tiene mascota al iniciar sesión
                    LaunchedEffect(Unit) {
                        if (authRepository.isUserLoggedIn()) {
                            val userId = authRepository.getCurrentUserId() ?: ""
                            val userName = authRepository.getCurrentUserName() ?: ""
                            val userEmail = authRepository.getCurrentUserEmail() ?: ""
                            val authProvider = authRepository.getCurrentAuthProvider() ?: "local"

                            currentUserId = userId
                            currentUserIdState = userId
                            currentUserName = userName
                            currentUserEmail = userEmail
                            currentAuthProvider = authProvider

                            // Verificar si ya tiene mascota
                            val hasPet = app.storeRepository.hasUserAdoptedFirstPet(userId)
                            if (hasPet) {
                                currentScreen = "dashboard"
                            } else {
                                // Guardar datos temporalmente y mostrar selección de mascota
                                tempUserId = userId
                                tempUserName = userName
                                showPetSelection = true
                            }
                        }
                    }

                    // ✅ Pantalla de selección de mascota (prioridad)
                    if (showPetSelection) {
                        SelectPetScreen(
                            userId = tempUserId,
                            userName = tempUserName,
                            onPetSelected = {
                                showPetSelection = false
                                currentScreen = "dashboard"
                            }
                        )
                    } else {
                        when (currentScreen) {
                            "dashboard" -> {
                                DashboardScreen(
                                    userName = currentUserName,
                                    userEmail = currentUserEmail,
                                    userId = currentUserId,
                                    authProvider = currentAuthProvider,
                                    onLogout = {
                                        if (currentAuthProvider == "google") {
                                            scope.launch {
                                                authRepository.logoutWithGoogle()
                                            }
                                        } else {
                                            authRepository.logout()
                                        }
                                        currentScreen = "login"
                                        currentUserName = ""
                                        currentUserEmail = ""
                                        currentUserId = ""
                                        currentUserIdState = ""
                                        currentAuthProvider = "local"
                                        showPetSelection = false
                                        tempUserId = ""
                                        tempUserName = ""
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
                                        currentUserIdState = userId
                                        currentUserName = userName
                                        currentUserEmail = ""
                                        currentAuthProvider = "local"

                                        // ✅ Verificar si necesita mascota después de registro
                                        scope.launch {
                                            val hasPet = app.storeRepository.hasUserAdoptedFirstPet(userId)
                                            if (hasPet) {
                                                currentScreen = "dashboard"
                                            } else {
                                                tempUserId = userId
                                                tempUserName = userName
                                                showPetSelection = true
                                            }
                                        }
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
                                        currentUserIdState = userId
                                        currentUserName = userName
                                        currentUserEmail = email
                                        currentAuthProvider = authProvider

                                        // ✅ Verificar si necesita mascota después de login
                                        scope.launch {
                                            val hasPet = app.storeRepository.hasUserAdoptedFirstPet(userId)
                                            if (hasPet) {
                                                currentScreen = "dashboard"
                                            } else {
                                                tempUserId = userId
                                                tempUserName = userName
                                                showPetSelection = true
                                            }
                                        }
                                    },
                                    onNavigateToLocalRegister = {
                                        scope.launch {
                                            val result = loginViewModel.checkAndHandleGuest()
                                            when (result) {
                                                is GuestCheckResult.Exists -> {
                                                    val userId = result.userId
                                                    val userName = result.userName

                                                    currentUserId = userId
                                                    currentUserIdState = userId
                                                    currentUserName = userName
                                                    currentUserEmail = ""
                                                    currentAuthProvider = "local"

                                                    // ✅ Verificar si necesita mascota para invitado
                                                    val hasPet = app.storeRepository.hasUserAdoptedFirstPet(userId)
                                                    if (hasPet) {
                                                        currentScreen = "dashboard"
                                                    } else {
                                                        tempUserId = userId
                                                        tempUserName = userName
                                                        showPetSelection = true
                                                    }
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
}