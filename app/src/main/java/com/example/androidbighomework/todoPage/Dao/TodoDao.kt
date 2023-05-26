package com.example.androidbighomework.todoPage.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TodoDao {

    /*
     * 获取所有的todo待办
     */
    @Query("select * from todos")
    fun getAllTodo(): Array<Todo>

    /*
     * 插入一个todo
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTodo(todo: Todo): Long

}