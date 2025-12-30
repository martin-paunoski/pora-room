package com.example.roomdemo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Glavna baza podatkov
 * version = 1: različica sheme (pomembno za migracije)
 */
@Database(
    entities = [Task::class],
    version = 1,
    exportSchema = false
)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getDatabase(context: Context): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration() // Za demo - v produkciji uporabite migracije
                    .build()

                INSTANCE = instance
                instance
            }
        }

        /**
         * Callback za inicializacijo baze s privzetimi podatki
         */
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.taskDao())
                    }
                }
            }
        }

        suspend fun populateDatabase(taskDao: TaskDao) {
            // Vstavimo nekaj privzetih opravil
            val tasks = listOf(
                Task(title = "Nakup živil", description = "Mleko, kruh, maslo", priority = 1),
                Task(title = "Študij za izpit", description = "Poglavja 1-5", priority = 2),
                Task(title = "Telovadba", description = "30 minut hoje", priority = 0)
            )
            tasks.forEach { taskDao.insertTask(it) }
        }
    }
}