package com.example.spire_task.data.local.entities

import android.R
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tblPerfiles")
data class ProfileEntity(
    @PrimaryKey
    val idUser: String,           // Para local: "local_xxxx", para Google: UID de Firebase
    val userName: String,         // Nombre que elige el usuario
    val xpTotal: Float = 0f,
    val level: Int = 1,
    val monedas: Int = 0,
    val racha: Int = 0,
    val maxRacha: Int = 0,
    val authProvider: String = "local",  // "local" o "google"
    val email: String = ""
)