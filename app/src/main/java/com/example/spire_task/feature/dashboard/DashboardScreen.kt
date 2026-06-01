package com.example.spire_task.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spire_task.data.local.database.SpiroDatabase
import com.example.spire_task.data.repository.AuthRepository
import com.example.spire_task.data.repository.SettingsRepository
import com.example.spire_task.data.repository.StoreRepository
import com.example.spire_task.feature.dashboard.components.AppBar
import com.example.spire_task.feature.dashboard.components.BottomNavigationBar
import com.example.spire_task.feature.dashboard.home.HomeScreen
import com.example.spire_task.feature.kanban.main.KanbanScreen
import com.example.spire_task.feature.kanban.main.KanbanViewModel
import com.example.spire_task.feature.kanban.main.KanbanViewModelFactory
import com.example.spire_task.feature.profile.ProfileViewModel
import com.example.spire_task.feature.profile.ProfileViewModelFactory
import com.example.spire_task.feature.profile.main.ProfileScreen
import com.example.spire_task.feature.settings.SettingsScreen
import com.example.spire_task.feature.settings.SettingsViewModel
import com.example.spire_task.feature.settings.SettingsViewModelFactory
import com.example.spire_task.feature.store.ProductDetailScreen
import com.example.spire_task.feature.store.StoreScreen
import com.example.spire_task.feature.store.StoreViewModel
import com.example.spire_task.feature.store.StoreViewModelFactory
import androidx.compose.ui.platform.LocalContext
import com.example.spire_task.data.local.entities.ProductEntity

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
    
    // Navegación interna para sub-pantallas
    var currentSubScreen by remember { mutableStateOf<String?>(null) }
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }

    val context = LocalContext.current
    val database = remember { SpiroDatabase.getDatabase(context) }
    
    // Repositorios
    val authRepo = remember { AuthRepository(context) }
    val storeRepo = remember { StoreRepository(database.storeDao()) }
    val settingsRepo = remember { SettingsRepository(database.settingsDao()) }

    // ViewModels
    val kanbanViewModel: KanbanViewModel = viewModel(
        key = userId,
        factory = KanbanViewModelFactory.createFactory(userId)
    )
    
    val profileViewModel: ProfileViewModel = viewModel(
        key = "profile_$userId",
        factory = ProfileViewModelFactory(authRepo, storeRepo, userId)
    )

    val storeViewModel: StoreViewModel = viewModel(
        key = "store_$userId",
        factory = StoreViewModelFactory(storeRepo, userId)
    )

    val settingsViewModel: SettingsViewModel = viewModel(
        key = "settings_$userId",
        factory = SettingsViewModelFactory(settingsRepo, userId)
    )

    Scaffold(
        topBar = {
            if (currentSubScreen == null) {
                AppBar(
                    userName = userName,
                    level = level,
                    onLogout = onLogout
                )
            }
        },
        bottomBar = {
            if (currentSubScreen == null) {
                BottomNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (currentSubScreen == "settings") {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBack = { currentSubScreen = null }
                )
            } else if (currentSubScreen == "product_detail" && selectedProduct != null) {
                ProductDetailScreen(
                    product = selectedProduct!!,
                    onPurchase = { 
                        storeViewModel.purchaseProduct(it.idProduct)
                        currentSubScreen = null
                    },
                    onBack = { currentSubScreen = null }
                )
            } else {
                when (selectedTab) {
                    0 -> HomeScreen(
                        userName = userName,
                        userId = userId,
                        level = level,
                        xp = xp,
                        xpNeeded = level * 100,
                        monedas = monedas,
                        racha = racha,
                        onNavigateToKanban = { selectedTab = 1 },
                        onCreateTaskClick = { mostrarDialogoCrearRapida = true }
                    )
                    1 -> KanbanScreen(
                        viewModel = kanbanViewModel
                    )
                    2 -> StoreScreen(
                        viewModel = storeViewModel,
                        onProductClick = {
                            selectedProduct = it
                            currentSubScreen = "product_detail"
                        }
                    )
                    3 -> ProfileScreen(
                        viewModel = profileViewModel,
                        userName = userName,
                        userEmail = userEmail,
                        userId = userId,
                        authProvider = authProvider,
                        onLogout = onLogout,
                        onSettingsClick = { currentSubScreen = "settings" }
                    )
                }
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