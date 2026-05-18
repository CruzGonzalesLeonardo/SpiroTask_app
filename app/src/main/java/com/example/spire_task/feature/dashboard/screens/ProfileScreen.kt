package com.example.spire_task.feature.dashboard.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(
    userName: String,
    userEmail: String,
    userId: String,
    authProvider: String = "local",
    onLogout: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Información del usuario
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "👤 Mi Perfil",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Nombre: $userName", style = MaterialTheme.typography.bodyLarge)
                    Text("Email: $userEmail", style = MaterialTheme.typography.bodyLarge)
                    Text("ID: ${userId.take(10)}...", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = if (authProvider == "google") "✅ Cuenta Google" else "📱 Cuenta Local",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Opciones
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⚙️ Opciones", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    Divider()

                    TextButton(
                        onClick = onEditProfileClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("✏️ Editar perfil")
                    }

                    Divider()

                    TextButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔧 Configuración")
                    }

                    Divider()

                    TextButton(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("🚪 Cerrar sesión")
                    }
                }
            }
        }

        // Estadísticas avanzadas
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🏆 Logros", style = MaterialTheme.typography.titleMedium)
                    Text("Próximamente...")
                }
            }
        }
    }
}