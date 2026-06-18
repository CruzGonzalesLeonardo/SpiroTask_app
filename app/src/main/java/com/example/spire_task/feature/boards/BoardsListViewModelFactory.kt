package com.example.spire_task.feature.boards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.spire_task.SpiroTaskApplication
import com.example.spire_task.data.local.database.SpiroDatabase

/**
 * Factory para crear BoardsListViewModel con la dependencia de la base de datos.
 */
class BoardsListViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BoardsListViewModel::class.java)) {
            val database = SpiroDatabase.getInstance(SpiroTaskApplication.instance)
            @Suppress("UNCHECKED_CAST")
            return BoardsListViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}