package com.example.spire_task.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.spire_task.data.local.entidades.*
import com.example.spire_task.data.local.daos.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PerfilUsuarioEntity::class,
        MascotaBaseEntity::class,
        MascotaUsuarioEntity::class,
        TableroEntity::class,
        TareaEntity::class,
        SubtareaEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SpiroDatabase : RoomDatabase() {

    abstract fun perfilUsuarioDao(): PerfilUsuarioDao
    abstract fun mascotaBaseDao(): MascotaBaseDao
    abstract fun mascotaUsuarioDao(): MascotaUsuarioDao
    abstract fun tableroDao(): TableroDao
    abstract fun tareaDao(): TareaDao
    abstract fun subtareaDao(): SubtareaDao

    companion object {
        @Volatile
        private var INSTANCE: SpiroDatabase? = null

        fun getInstance(context: Context): SpiroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpiroDatabase::class.java,
                    "spiro_task_db"
                )
                    .addCallback(PrecargaCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class PrecargaCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        precargarDatos(database)
                    }
                }
            }

            private suspend fun precargarDatos(database: SpiroDatabase) {
                database.mascotaBaseDao().insertarTodas(DatosIniciales.mascotasBase)
            }
        }
    }
}