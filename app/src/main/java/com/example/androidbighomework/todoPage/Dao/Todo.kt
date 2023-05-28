package com.example.androidbighomework.todoPage.Dao

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true) var id: Long,
    @ColumnInfo(name = "name") var todoName: String,
    @ColumnInfo(name = "total_time") var total_time: Int,  // 总共计时时间，分钟为单位
    @ColumnInfo(name = "current_progress", defaultValue = 0.toString()) var current_progress: Int,  // 当前已经计时的时长
    @ColumnInfo(name = "add_date") var add_date: Long,  // todo创建时间
    @ColumnInfo(name = "count_type") var count_type: String,  // 计时类型
    @ColumnInfo(name = "break_time") var break_time: Int,  // 休息时长
    @ColumnInfo(name = "todo_notes") var todo_notes: String,  // 备注
    @ColumnInfo(name = "todo_type") var todo_type: String,  // todo的类型
    @ColumnInfo(name = "repeat_time") var repeat_time: String,  // 重复时间
    @ColumnInfo(name = "is_complete", defaultValue = 0.toString()) var is_complete: Int
)
