package com.example.spire_task.feature.store

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.spire_task.data.local.entities.ProductEntity

@Composable
fun ProductImage(assetPath: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        if (assetPath != null) {
            AsyncImage(
                model = assetPath,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Text("📦", fontSize = 40.sp)
        }
    }
}

@Composable
fun StoreScreen(
    viewModel: StoreViewModel,
    monedas: Int,  // ✅ Nuevo parámetro para recibir las monedas
    onProductClick: (ProductEntity) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val products by viewModel.allProducts.collectAsState()
    val ownedProducts by viewModel.ownedProducts.collectAsState()
    val activePet by viewModel.activePet.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ✅ Header con título y monedas
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tienda",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // ✅ Tarjeta de monedas
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "💰",
                            fontSize = 20.sp
                        )
                        Text(
                            text = monedas.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            divider = {}
        ) {
            StoreTab(
                selected = selectedTab == 0,
                text = "Tienda",
                icon = Icons.Default.ShoppingBag,
                onClick = { selectedTab = 0 }
            )
            StoreTab(
                selected = selectedTab == 1,
                text = "Mi Colección",
                icon = Icons.Default.Pets,
                onClick = { selectedTab = 1 }
            )
        }

        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "StoreTransition"
        ) { targetTab ->
            when (targetTab) {
                0 -> ProductGrid(
                    products = products,
                    monedas = monedas,  // ✅ Pasar monedas al grid
                    onProductClick = onProductClick
                )
                1 -> CollectionGrid(
                    ownedProducts = ownedProducts.filter { it.type == "PET" },
                    activePetId = activePet?.idProduct,
                    onActivate = { viewModel.activatePet(it.idProduct) }
                )
            }
        }
    }
}

@Composable
fun StoreTab(selected: Boolean, text: String, icon: ImageVector, onClick: () -> Unit) {
    Tab(
        selected = selected,
        onClick = onClick,
        modifier = Modifier.height(64.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProductGrid(
    products: List<ProductEntity>,
    monedas: Int,  // ✅ Recibir monedas
    onProductClick: (ProductEntity) -> Unit
) {
    if (products.isEmpty()) {
        EmptyState(message = "La tienda está vacía por ahora...")
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(products) { product ->
                ProductCard(
                    product = product,
                    monedas = monedas,  // ✅ Pasar monedas a la tarjeta
                    onClick = { onProductClick(product) }
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    product: ProductEntity,
    monedas: Int,  // ✅ Recibir monedas
    onClick: () -> Unit
) {
    val canAfford = monedas >= product.price  // ✅ Verificar si puede comprar

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            ProductImage(
                assetPath = product.assetPath,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ✅ Mostrar precio con indicador si no alcanza
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        if (canAfford) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        } else {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        },
                        CircleShape
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${product.price}",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (canAfford) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = " 💰",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (canAfford) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }

            // ✅ Mostrar indicador si no alcanza
            if (!canAfford) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "❌ No te alcanza",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun CollectionGrid(
    ownedProducts: List<ProductEntity>,
    activePetId: String?,
    onActivate: (ProductEntity) -> Unit
) {
    if (ownedProducts.isEmpty()) {
        EmptyState(message = "Aún no tienes mascotas. ¡Adopta una en la tienda!")
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(ownedProducts) { product ->
                val isActive = product.idProduct == activePetId
                CollectionPetCard(
                    product = product,
                    isActive = isActive,
                    onActivate = { onActivate(product) }
                )
            }
        }
    }
}

@Composable
fun CollectionPetCard(product: ProductEntity, isActive: Boolean, onActivate: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = if (isActive) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 8.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(contentAlignment = Alignment.TopEnd) {
                ProductImage(
                    assetPath = product.assetPath,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
                if (isActive) {
                    Surface(
                        modifier = Modifier.padding(4.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp).padding(2.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onActivate,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isActive,
                shape = RoundedCornerShape(8.dp),
                colors = if (isActive) {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Text(if (isActive) "Activo" else "Activar")
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📭", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}