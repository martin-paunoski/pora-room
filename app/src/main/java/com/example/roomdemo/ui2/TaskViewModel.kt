package com.example.roomdemo.ui2

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.roomdemo.data.DatabaseException
import com.example.roomdemo.data.Task
import com.example.roomdemo.data.TaskDatabase
import com.example.roomdemo.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Modern ViewModel using StateFlow for better coroutine support
 */
class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    val allTasks: StateFlow<List<Task>>

    // StateFlow for error messages
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // StateFlow for success messages
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        val taskDao = TaskDatabase.getDatabase(application).taskDao()
        repository = TaskRepository(taskDao)
        allTasks = repository.allTasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun insert(task: Task) = viewModelScope.launch {
        try {
            if (task.title.isBlank()) {
                throw IllegalArgumentException("Naslov ne sme biti prazen")
            }
            repository.insert(task)
            _successMessage.value = "Opravilo uspešno dodano"
        } catch (e: IllegalArgumentException) {
            _errorMessage.value = e.message
        } catch (e: DatabaseException) {
            _errorMessage.value = e.message
        } catch (e: Exception) {
            _errorMessage.value = "Neznana napaka: ${e.message}"
        }
    }

    fun update(task: Task) = viewModelScope.launch {
        try {
            repository.update(task)
            _successMessage.value = "Opravilo posodobljeno"
        } catch (e: DatabaseException) {
            _errorMessage.value = e.message
        } catch (e: Exception) {
            _errorMessage.value = "Napaka pri posodabljanju: ${e.message}"
        }
    }

    fun delete(task: Task) = viewModelScope.launch {
        try {
            repository.delete(task)
            _successMessage.value = "Opravilo izbrisano"
        } catch (e: DatabaseException) {
            _errorMessage.value = e.message
        } catch (e: Exception) {
            _errorMessage.value = "Napaka pri brisanju: ${e.message}"
        }
    }

    fun toggleComplete(task: Task) = viewModelScope.launch {
        try {
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            repository.update(updatedTask)
        } catch (e: Exception) {
            _errorMessage.value = "Napaka: ${e.message}"
        }
    }

    fun deleteCompleted() = viewModelScope.launch {
        try {
            val count = repository.deleteCompleted()
            _successMessage.value = "Izbrisanih $count dokončanih opravil"
        } catch (e: Exception) {
            _errorMessage.value = "Napaka pri brisanju: ${e.message}"
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccess() {
        _successMessage.value = null
    }
}