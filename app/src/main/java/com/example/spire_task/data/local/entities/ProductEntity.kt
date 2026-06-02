package com.example.spire_task.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tblProductos")
data class ProductEntity(
    @PrimaryKey
    val idProduct: String,
    val name: String,
    val description: String,
    val price: Int,
    val type: String, // "PET", "ACCESSORY"
    val assetPath: String? // Path to animation or image
)
