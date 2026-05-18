package com.example.spire_task.feature.dashboard.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

@Composable
fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val items = listOf(
        BottomNavItem("Inicio", Icons.Default.Home, Icons.Default.Home),
        BottomNavItem("Kanban", Icons.Default.List, Icons.Default.List),
        BottomNavItem("Mascota", Icons.Default.Pets, Icons.Default.Pets),
        BottomNavItem("Perfil", Icons.Default.Person, Icons.Default.Person)
    )

    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        if (selectedTab == index) item.selectedIcon else item.icon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) }
            )
        }
    }
}