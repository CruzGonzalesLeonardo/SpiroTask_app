package com.example.spire_task.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.spire_task.feature.dashboard.components.AppBar
import com.example.spire_task.feature.dashboard.components.BottomNavigationBar
import com.example.spire_task.feature.dashboard.screens.HomeScreen
import com.example.spire_task.feature.dashboard.screens.KanbanDashboardScreen
import com.example.spire_task.feature.dashboard.screens.ProfileScreen
import com.example.spire_task.feature.dashboard.screens.TareaPendiente

@Composable
fun DashboardScreen(
    userName: String,
    userEmail: String,
    userId: String,
    authProvider: String = "local",
    level: Int = 1,
    xp: Float = 0f,
    monedas: Int = 0,
    racha: Int = 0,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    val tareasPendientes = listOf(
        TareaPendiente("1", "Terminar informe"),
        TareaPendiente("2", "Estudiar Kotlin"),
        TareaPendiente("3", "Hacer ejercicio")
    )

    Scaffold(
        topBar = {
            AppBar(
                userName = userName,
                level = level,
                onLogout = onLogout
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    userName = userName,
                    level = level,
                    xp = xp,
                    xpNeeded = level * 100,
                    monedas = monedas,
                    racha = racha,
                    tareasPendientes = tareasPendientes
                )
                1 -> KanbanDashboardScreen()
                2 -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Pantalla Mascota - Próximamente")
                    }
                }
                3 -> ProfileScreen(
                    userName = userName,
                    userEmail = userEmail,
                    userId = userId,
                    authProvider = authProvider,
                    onLogout = onLogout
                )
            }
        }
    }
}