package com.example.spire_task.feature.store

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.spire_task.data.local.entities.ProductEntity

@Composable
fun StoreScreen(
    viewModel: StoreViewModel,
    onProductClick: (ProductEntity) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val products by viewModel.allProducts.collectAsState()
    val ownedProducts by viewModel.ownedProducts.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Tienda", modifier = Modifier.padding(16.dp))
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Mi Colección", modifier = Modifier.padding(16.dp))
            }
        }

        when (selectedTab) {
            0 -> ProductGrid(products = products, onProductClick = onProductClick)
            1 -> CollectionGrid(
                ownedProducts = ownedProducts.filter { it.type == "PET" },
                onActivate = { viewModel.activatePet(it.idProduct) }
            )
        }
    }
}

@Composable
fun ProductGrid(products: List<ProductEntity>, onProductClick: (ProductEntity) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(products) { product ->
            ProductCard(product = product, onClick = { onProductClick(product) })
        }
    }
}

@Composable
fun ProductCard(product: ProductEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("📦 ${product.name}") // Placeholder for image/animation
            }
            Text(text = product.name, style = MaterialTheme.typography.titleMedium)
            Text(text = "${product.price} 💰", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun CollectionGrid(ownedProducts: List<ProductEntity>, onActivate: (ProductEntity) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(ownedProducts) { product ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = product.name, style = MaterialTheme.typography.titleMedium)
                    Button(
                        onClick = { onActivate(product) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Activar")
                    }
                }
            }
        }
    }
}
