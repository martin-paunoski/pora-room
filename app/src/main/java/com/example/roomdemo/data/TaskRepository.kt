package com.example.roomdemo.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository sloj za abstrakcijo dostopa do podatkov
 * Omogoča lažje testiranje in zamenjavo vira podatkov
 */
class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    fun getTasksByStatus(isCompleted: Boolean): Flow<List<Task>> {
        return taskDao.getTasksByStatus(isCompleted)
    }

    fun getTasksByPriority(priority: Int): Flow<List<Task>> {
        return taskDao.getTasksByPriority(priority)
    }

    suspend fun getTaskById(id: Int): Task? {
        return taskDao.getTaskById(id)
    }

    /**
     * Vstavi novo opravilo
     * @throws Exception če pride do napake pri vstavljanju
     */
    suspend fun insert(task: Task): Long {
        return try {
            taskDao.insertTask(task)
        } catch (e: Exception) {
            throw DatabaseException("Napaka pri vstavljanju opravila: ${e.message}")
        }
    }

    suspend fun update(task: Task) {
        try {
            taskDao.updateTask(task)
        } catch (e: Exception) {
            throw DatabaseException("Napaka pri posodabljanju opravila: ${e.message}")
        }
    }

    suspend fun delete(task: Task) {
        try {
            taskDao.deleteTask(task)
        } catch (e: Exception) {
            throw DatabaseException("Napaka pri brisanju opravila: ${e.message}")
        }
    }

    suspend fun deleteCompleted(): Int {
        return taskDao.deleteCompletedTasks()
    }

    suspend fun deleteAll() {
        taskDao.deleteAllTasks()
    }
}

/**
 * Prilagojena izjema za napake v bazi
 */
class DatabaseException(message: String) : Exception(message)