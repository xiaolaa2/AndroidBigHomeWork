package com.example.androidbighomework.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidbighomework.MyApplication
import com.example.androidbighomework.todoPage.ConcentrateStatus
import com.example.androidbighomework.todoPage.Dao.Todo
import com.example.androidbighomework.todoPage.MediaPlayerStatus
import com.example.androidbighomework.todoPage.PageType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConcentrateScreenViewModel : ViewModel() {
    // 倒计时全局变量
    var countDown = MutableStateFlow<Int>(0)
    var todoStatus = MutableStateFlow(ConcentrateStatus.NotBegin)
    var nowPickMusicIndex = MutableStateFlow<Int>(-1)
    var nowPlayingMusicIndex = MutableStateFlow<Int>(-1)
    var mediaPlayerStatus = MutableStateFlow<MediaPlayerStatus>(MediaPlayerStatus.Wait)
    var page = MutableStateFlow<PageType>(PageType.CountDown)
    var todo = MutableStateFlow<Todo>(Todo(-1, "", 1, 1, 0, "", 0, "", "", "", 0, 0))

    init {
        // 实时更新页数
        viewModelScope.launch {
            page.collect {
                MyApplication.page.value = it
            }
        }
        viewModelScope.launch {
            todo.collect {
                MyApplication.todo.value = it
                MyApplication.totalTime = it.total_time
                when (it.count_type) {
                    "倒计时" -> {
                        page.value = PageType.CountDown
                    }
                    "正向计时" -> {
                        page.value = PageType.ForwardTiming
                    }
                }
            }
        }
        viewModelScope.launch {
            page.collect {
                when (it) {
                    PageType.Break -> {
                        MyApplication.BreakcountDown.value = 0
                    }
                    else -> {}
                }
            }
        }
        // 更新todo的当前时间
        viewModelScope.launch {
            MyApplication.countDown.collect {
                todo.value.current_progress = it
                withContext(Dispatchers.IO) {
                    MyApplication.db.todoDao().updateTodo(todo.value)
                }
            }
        }
    }

    fun changePage(pageType: PageType) {
        page.value = pageType
    }

}