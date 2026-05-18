package com.example.spire_task.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.spire_task.data.local.dao.ProfileDao
import com.example.spire_task.data.local.entities.ProfileEntity

@Database(
    entities = [ProfileEntity::class],
    version = 1
)
abstract class SpiroDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao

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
                instance
            }
        }
    }
}