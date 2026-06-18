package com.example.spire_task.feature.splash


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.spire_task.SpiroTaskApplication
import com.example.spire_task.data.local.database.SpiroDatabase

/**
 * Factory para crear SplashViewModel con la dependencia de la base de datos.
 */
class SplashViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SplashViewModel::class.java)) {
            val database = SpiroDatabase.getInstance(
                SpiroTaskApplication.instance
            )
            @Suppress("UNCHECKED_CAST")
            return SplashViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}