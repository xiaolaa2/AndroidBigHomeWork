package com.example.androidbighomework.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.androidbighomework.todoPage.ConcentrateStatus
import com.example.androidbighomework.todoPage.MediaPlayerStatus
import kotlinx.coroutines.flow.MutableStateFlow

class ConcentrateScreenViewModel: ViewModel() {
    // 倒计时全局变量
    var countDown = MutableStateFlow<Int>(0)
    var todoStatus = MutableStateFlow(ConcentrateStatus.NotBegin)
    var nowPickMusicIndex = MutableStateFlow<Int>(-1)
    var nowPlayingMusicIndex = MutableStateFlow<Int>(-1)
    var mediaPlayerStatus = MutableStateFlow<MediaPlayerStatus>(MediaPlayerStatus.Wait)
}