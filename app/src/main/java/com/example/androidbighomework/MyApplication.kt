package com.example.androidbighomework

import android.app.Application
import android.util.Log
import com.example.androidbighomework.Theme.MyDataBaseHelper

class MyApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        // 初始化数据库
        val db = MyDataBaseHelper(this, "tomatoTodo", 2)
        db.readableDatabase
    }
}