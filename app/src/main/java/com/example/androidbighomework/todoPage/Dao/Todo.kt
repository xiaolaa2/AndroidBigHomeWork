package com.example.androidbighomework.todoPage.Dao

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "name") val todoName: String,
    @ColumnInfo(name = "total_time") val total_time: Int,  // 总共计时时间，分钟为单位
    @ColumnInfo(name = "current_progress") val current_progress: Int,  // 当前已经计时的时长
    @ColumnInfo(name = "add_date") val add_date: Long,  // todo创建时间
    @ColumnInfo(name = "count_type") val count_type: String,  // 计时类型
    @ColumnInfo(name = "break_time") val break_time: Int,  // 计时类型
    @ColumnInfo(name = "todo_notes") val todo_notes: String,  // 备注
    @ColumnInfo(name = "todo_type") val todo_type: String,  // todo的类型
    @ColumnInfo(name = "repeat_time") val repeat_time: String,  // 重复时间
)
