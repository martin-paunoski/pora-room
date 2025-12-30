package com.example.roomdemo.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) - vmesnik za dostop do baze
 * Room avtomatsko generira implementacijo
 */
@Dao
interface TaskDao {

    // Flow omogoča avtomatsko posodabljanje UI-ja
    @Query("SELECT * FROM tasks ORDER BY created_at DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): Task?

    @Query("SELECT * FROM tasks WHERE is_completed = :isCompleted")
    fun getTasksByStatus(isCompleted: Boolean): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE priority = :priority")
    fun getTasksByPriority(priority: Int): Flow<List<Task>>

    // Demonstracija možne napake - neobstoječ stolpec
    // @Query("SELECT * FROM tasks WHERE invalid_column = 1")
    // fun getInvalidQuery(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Insert
    suspend fun insertTasks(tasks: List<Task>)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE is_completed = 1")
    suspend fun deleteCompletedTasks(): Int

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
}