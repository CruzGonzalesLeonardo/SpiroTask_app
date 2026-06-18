package com.example.spire_task.feature.kanban

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.spire_task.SpiroTaskApplication
import com.example.spire_task.data.local.database.SpiroDatabase

class KanbanViewModelFactory(
    private val tableroId: Int
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KanbanViewModel::class.java)) {
            val database = SpiroDatabase.getInstance(SpiroTaskApplication.instance)
            @Suppress("UNCHECKED_CAST")
            return KanbanViewModel(database, tableroId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}