package com.example.spire_task.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "tblAjustes",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["idUser"],
            childColumns = ["idUser"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SettingsEntity(
    @PrimaryKey
    val idUser: String,
    val isDarkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val petSleepStartTime: String = "22:00",
    val petSleepEndTime: String = "08:00"
)
