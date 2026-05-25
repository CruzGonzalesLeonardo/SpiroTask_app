package com.example.spire_task.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spire_task.feature.dashboard.components.AppBar
import com.example.spire_task.feature.dashboard.components.BottomNavigationBar
import com.example.spire_task.feature.dashboard.home.HomeScreen
import com.example.spire_task.feature.kanban.main.KanbanScreen
import com.example.spire_task.feature.kanban.main.KanbanViewModel
import com.example.spire_task.feature.kanban.main.KanbanViewModelFactory
import com.example.spire_task.feature.profile.main.ProfileScreen

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
    var mostrarDialogoCrearRapida by remember { mutableStateOf(false) }

    // ✅ Crear KanbanViewModel con key
    val kanbanViewModel: KanbanViewModel = viewModel(
        key = userId,
        factory = KanbanViewModelFactory.createFactory(userId)
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
                    userId = userId,  // ✅ Pasar userId
                    level = level,
                    xp = xp,
                    xpNeeded = level * 100,
                    monedas = monedas,
                    racha = racha,
                    onNavigateToKanban = { selectedTab = 1 },  // ✅ Navegar a Kanban
                    onCreateTaskClick = { mostrarDialogoCrearRapida = true }  // ✅ Crear tarea rápida
                )
                1 -> KanbanScreen(
                    viewModel = kanbanViewModel
                )
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

    // Diálogo para crear tarea rápida desde Home
    if (mostrarDialogoCrearRapida) {
        // Aquí puedes reutilizar tu CreateTaskDialog
        // O abrir el Kanban directamente
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { mostrarDialogoCrearRapida = false },
            title = { Text("Crear tarea rápida") },
            text = { Text("Ve a la pestaña Kanban para crear tu tarea") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoCrearRapida = false
                    selectedTab = 1  // Ir a Kanban
                }) {
                    Text("Ir a Kanban")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCrearRapida = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}