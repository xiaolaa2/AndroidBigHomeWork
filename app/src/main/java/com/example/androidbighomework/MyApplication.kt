package com.example.androidbighomework

import android.app.Application
import androidx.room.Room
import com.example.androidbighomework.todoPage.AppDatabase

class MyApplication : Application(){
    override fun onCreate() {
        super.onCreate()

        // 初始化数据库
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "tomatoTodo"
        ).build()
    }

    companion object {
        // 初始化数据库，设置为静态变量使得可以全局访问
        lateinit var db: AppDatabase
    }
}