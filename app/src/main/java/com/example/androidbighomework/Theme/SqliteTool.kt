package com.example.androidbighomework.Theme

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDataBaseHelper(context: Context, name: String, version: Int) :
    SQLiteOpenHelper(context, name, null, version) {

    private val createTodoDB =
        "create table todoList (" +
                "id integer primary key autoincrement,"+
                "name varchar," +
                "total_time integer," +      // 总共时间，分钟为单位
                "current_progress integer," +
                "add_date integer," +
                "count_type varchar," +    // 计时类型
                "break_time integer," +    // 休息时长
                "todo_notes," + //备注
                "todo_type," + // todo的类型，是倒计时还是正向计时还是养习惯
                "repeat_time varchar)"   // 重复时间，每天每周每月

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("drop table if exists [tomatoTodo].[todoList]")
        db?.execSQL(createTodoDB)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

}