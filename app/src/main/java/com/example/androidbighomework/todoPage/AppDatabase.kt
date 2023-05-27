package com.example.androidbighomework.todoPage

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.androidbighomework.todoPage.Dao.Todo
import com.example.androidbighomework.todoPage.Dao.TodoDao

@Database(entities = [Todo::class], version = 3)
abstract class AppDatabase :RoomDatabase() {
    abstract fun todoDao(): TodoDao
}