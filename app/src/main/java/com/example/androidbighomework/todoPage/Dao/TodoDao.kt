package com.example.androidbighomework.todoPage.Dao

import androidx.room.*

@Dao
interface TodoDao {

    /*
     * 获取所有的todo待办
     */
    @Query("select * from todos")
    fun getAllTodo(): Array<Todo>

    @Query("select * from todos where is_complete = 0")
    fun getAllTodoNotComplete(): Array<Todo>

    @Query("select * from todos where id = :todoId")
    fun getTodoById(todoId: Int): Todo

    /*
     * 插入一个todo
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTodo(todo: Todo): Long

    @Update
    fun updateTodo(todo: Todo): Int
}