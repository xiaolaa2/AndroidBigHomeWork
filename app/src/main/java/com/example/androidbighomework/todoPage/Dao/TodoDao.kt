package com.example.androidbighomework.todoPage.Dao

import androidx.room.*

@Dao
interface TodoDao {
    /*
     * 获取所有的todo待办
     */
    @Query("select * from todos")
    fun getAllTodo(): List<Todo>
    @Query("select * from todos where is_complete = 0")
    fun getAllTodoNotComplete(): Array<Todo>
    @Query("select * from todos limit 1")
    fun getTheFirstTodo(): Todo
    @Query("select * from todos where id = :todoId")
    fun getTodoById(todoId: Long): Todo
    /*
     * 插入一个todo
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTodo(todo: Todo): Long
    // 删除一个todo
    @Delete
    fun deleteTodo(todo: Todo): Int
    @Update
    fun updateTodo(todo: Todo): Int
}