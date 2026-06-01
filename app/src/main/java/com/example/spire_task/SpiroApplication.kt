package com.example.spire_task

import android.app.Application
import com.example.spire_task.data.local.database.SpiroDatabase
import com.example.spire_task.data.repository.AuthRepository
import com.example.spire_task.data.repository.SettingsRepository
import com.example.spire_task.data.repository.StoreRepository
import com.example.spire_task.data.repository.TaskRepositoryImpl
import com.example.spire_task.domain.repositories.ITaskRepository
import com.google.firebase.FirebaseApp
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.SupervisorJob


class SpiroApplication : Application(), ImageLoaderFactory {

    lateinit var taskRepository: ITaskRepository
        private set
    lateinit var authRepository: AuthRepository
        private set
    lateinit var storeRepository: StoreRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set

    companion object {
        lateinit var instance: SpiroApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)

        // Inicializar base de datos
        val database = SpiroDatabase.getDatabase(this)

        // Inicializar repositorios
        taskRepository = TaskRepositoryImpl(
            taskDao = database.taskDao(),
            columnDao = database.columnDao()
        )
        authRepository = AuthRepository(this)
        storeRepository = StoreRepository(
            storeDao = database.storeDao(),
            profileDao = database.profileDao()
        )
        settingsRepository = SettingsRepository(database.settingsDao())

        // 🛠️ CÓDIGO TEMPORAL PARA PRUEBAS: Otorgar 1000 monedas al iniciar
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            val profileDao = database.profileDao()
            profileDao.obtenerUsuarioInvitado()?.let {
                profileDao.sumarMonedas(it.idUser, 1000)
            }
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }
}