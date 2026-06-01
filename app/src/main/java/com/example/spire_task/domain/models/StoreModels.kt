package com.example.spire_task.domain.models

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Int,
    val type: ProductType,
    val assetPath: String
)

enum class ProductType {
    PET, ACCESSORY
}

data class Purchase(
    val id: Long,
    val userId: String,
    val productId: String,
    val purchaseDate: Long,
    val isActive: Boolean
)
