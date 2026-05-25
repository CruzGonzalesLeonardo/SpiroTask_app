package com.example.spire_task.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.spire_task.data.local.dao.ColumnDao
import com.example.spire_task.data.local.dao.ProfileDao
import com.example.spire_task.data.local.dao.TaskDao
import com.example.spire_task.data.local.entities.ColumnEntity
import com.example.spire_task.data.local.entities.ProfileEntity
import com.example.spire_task.data.local.entities.SubTaskEntity
import com.example.spire_task.data.local.entities.TaskEntity
import com.example.spire_task.data.local.entities.TaskHistoryEntity

@Database(
    entities = [
        ProfileEntity::class,
        TaskEntity::class,
        SubTaskEntity::class,
        ColumnEntity::class,
        TaskHistoryEntity::class
    ],
    version = 1,  // ✅ Mantén versión 1 durante desarrollo
    exportSchema = false
)
abstract class SpiroDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao
    abstract fun taskDao(): TaskDao
    abstract fun columnDao(): ColumnDao

    companion object {
        @Volatile
        private var INSTANCE: SpiroDatabase? = null

        fun getDatabase(context: Context): SpiroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpiroDatabase::class.java,
                    "spire_task.db"
                )
                    // ⚠️ SOLO PARA DESARROLLO: Destruye y recrea la DB si hay cambios
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}