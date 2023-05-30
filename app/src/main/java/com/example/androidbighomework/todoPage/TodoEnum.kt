package com.example.androidbighomework.todoPage

enum class ConcentrateStatus {   // 专注中和休息中以及还没有开始专注
    Focusing, Breaking, NotBegin
}

enum class MediaPlayerStatus {  // Wait表示的是还未初始化完毕，NotStart表示准备好开始播放，
    Pausing, Playing, NotStart, Wait
}

// 控制应用程序状态
enum class AppStatus {
    Front, Background
}