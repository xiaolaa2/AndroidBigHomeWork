package com.example.androidbighomework.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.androidbighomework.MyApplication
import com.example.androidbighomework.todoPage.Dao.Todo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class TodoPageViewModel : ViewModel() {
    val todoList = MutableStateFlow<MutableList<Todo>>(mutableListOf())

    private val getAllTodoList = flow<List<Todo>> {
        // 访问db获取所有todo实例
        val db = MyApplication.db.todoDao()
        emit(db.getAllTodo())
    }.flowOn(Dispatchers.IO)

    fun addTodo(todo: Todo) {
        todoList.value.add(todo)
    }

    fun updateTodoByIndex(index: Int, todo: Todo) {
        todoList.value[index] = todo
    }

    fun removeTodoByIndex(index: Int) {
        todoList.value.removeAt(index)
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            // 初始化的时候获取所有的todoList
            getAllTodoList.collect{
                todoList.value.addAll(it)
            }
        }
    }
}