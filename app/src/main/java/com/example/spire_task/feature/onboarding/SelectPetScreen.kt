// feature/onboarding/SelectPetScreen.kt
package com.example.spire_task.feature.onboarding

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.spire_task.SpiroApplication
import com.example.spire_task.data.local.entities.ProductEntity
import com.example.spire_task.feature.store.StoreViewModel
import com.example.spire_task.feature.store.StoreViewModelFactory
import kotlinx.coroutines.launch

// ✅ Datos simulados de mascotas
private val simulatedPets = listOf(
    ProductEntity(
        idProduct = "pet_cat_sim",
        name = "🐱 Gato Cósmico",
        description = "Un compañero leal de las estrellas",
        price = 100,
        type = "PET",
        assetPath = "null"
    ),
    ProductEntity(
        idProduct = "pet_dog_sim",
        name = "🐶 Perro Galáctico",
        description = "Siempre listo para una aventura espacial",
        price = 150,
        type = "PET",
        assetPath = "null"
    ),
    ProductEntity(
        idProduct = "pet_dragon_sim",
        name = "🐉 Dragón Mágico",
        description = "Un amigo que escupe fuego colorido",
        price = 200,
        type = "PET",
        assetPath = "null"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectPetScreen(
    userId: String,
    userName: String,
    onPetSelected: () -> Unit
) {
    Log.d("SelectPetScreen", "========== INICIO SELECT PET SCREEN ==========")
    Log.d("SelectPetScreen", "userId: $userId")
    Log.d("SelectPetScreen", "userName: $userName")

    // ✅ Usar mascotas simuladas en lugar de la base de datos
    val availablePets = simulatedPets
    val isLoading = false
    val errorMessage = remember { mutableStateOf<String?>(null) }

    var selectedPet by remember { mutableStateOf<ProductEntity?>(null) }
    var isAdopting by remember { mutableStateOf(false) }

    Log.d("SelectPetScreen", "Mascotas simuladas: ${availablePets.size}")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "🎉 ¡Bienvenido, $userName! 🎉",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Elige tu primera mascota",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "¡Tu primera mascota es completamente gratis!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Grid de mascotas disponibles
            if (isLoading) {
                Log.d("SelectPetScreen", "Mostrando loading...")
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (availablePets.isEmpty()) {
                Log.d("SelectPetScreen", "No hay mascotas disponibles")
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🐱", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay mascotas disponibles",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                Log.d("SelectPetScreen", "Mostrando ${availablePets.size} mascotas")
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(availablePets) { pet ->
                        PetSelectionCard(
                            pet = pet,
                            isSelected = selectedPet?.idProduct == pet.idProduct,
                            onClick = {
                                Log.d("SelectPetScreen", "Mascota seleccionada: ${pet.name}")
                                selectedPet = pet
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de continuar
            Button(
                onClick = {
                    selectedPet?.let { pet ->
                        Log.d("SelectPetScreen", "Adoptando mascota: ${pet.name}")
                        isAdopting = true

                        // ✅ Simular adopción exitosa
                        kotlinx.coroutines.GlobalScope.launch {
                            kotlinx.coroutines.delay(1000)
                            isAdopting = false
                            onPetSelected()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = selectedPet != null && !isLoading && !isAdopting,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isAdopting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = if (selectedPet != null) "Adoptar ${selectedPet!!.name} (GRATIS)" else "Selecciona una mascota",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PetSelectionCard(
    pet: ProductEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen/Emoji de la mascota
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                // Extraer emoji del nombre (primeros caracteres)
                val emoji = pet.name.take(2)
                Text(emoji, fontSize = 60.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nombre de la mascota (sin emoji)
            val cleanName = pet.name.substring(3)
            Text(
                text = cleanName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Indicador de precio (GRATIS para primera mascota)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (pet.price > 0) {
                        Text(
                            text = "${pet.price}💰",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "→",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Text(
                        text = "✨ GRATIS ✨",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            // Indicador de selección
            if (isSelected) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Seleccionado",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}