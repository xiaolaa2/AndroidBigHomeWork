package com.example.androidbighomework.todoPage

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.androidbighomework.todoPage.Dao.Music
import com.example.androidbighomework.todoPage.Dao.MusicDao
import com.example.androidbighomework.todoPage.Dao.Todo
import com.example.androidbighomework.todoPage.Dao.TodoDao

@Database(entities = [Todo::class, Music::class], version = 1)
abstract class AppDatabase :RoomDatabase() {
    abstract fun todoDao(): TodoDao
    abstract fun MusicDao(): MusicDao
}