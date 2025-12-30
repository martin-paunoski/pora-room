package com.example.roomdemo.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity razred predstavlja tabelo v bazi podatkov
 * Vsak atribut postane stolpec v tabeli
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "priority")
    val priority: Int = 0, // 0=nizka, 1=srednja, 2=visoka

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)