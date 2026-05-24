package com.example.spire_task.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.spire_task.data.local.dao.ColumnDao
import com.example.spire_task.data.local.dao.ProfileDao
import com.example.spire_task.data.local.dao.TaskDao
import com.example.spire_task.data.local.entities.ColumnEntity
import com.example.spire_task.data.local.entities.ProfileEntity
import com.example.spire_task.data.local.entities.SubTaskEntity
import com.example.spire_task.data.local.entities.TaskEntity
import com.example.spire_task.data.local.entities.TaskHistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProfileEntity::class,
        TaskEntity::class,
        SubTaskEntity::class,
        ColumnEntity::class,
        TaskHistoryEntity::class
    ],
    version = 1,
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
                ).build()

                INSTANCE = instance

                CoroutineScope(Dispatchers.IO).launch {
                    val columnas = instance.columnDao().getAllColumnsOnce()
                    if (columnas.isEmpty()) {
                        instance.columnDao().insertColumns(
                            listOf(
                                ColumnEntity(name = "Por hacer", wipLimit = 10, order = 1),
                                ColumnEntity(name = "En progreso", wipLimit = 5, order = 2),
                                ColumnEntity(name = "Completado", wipLimit = 999, order = 3)
                            )
                        )
                    }
                }

                instance
            }
        }
    }
}