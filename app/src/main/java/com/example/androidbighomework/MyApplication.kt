package com.example.androidbighomework

import android.app.Application
import androidx.room.Room
import com.example.androidbighomework.todoPage.AppDatabase
import com.example.androidbighomework.todoPage.AppStatus
import com.example.androidbighomework.todoPage.ConcentrateStatus
import kotlinx.coroutines.flow.MutableStateFlow

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
        // 倒计时全局变量
        var countDown = MutableStateFlow<Int>(0)
        var totalTime = 0
        var todoStatus = MutableStateFlow(ConcentrateStatus.NotBegin)
        var appStatus = MutableStateFlow<AppStatus>(AppStatus.Front)
    }
}