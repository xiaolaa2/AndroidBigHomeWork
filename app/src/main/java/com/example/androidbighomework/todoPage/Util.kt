package com.example.androidbighomework.todoPage

import android.annotation.SuppressLint
import com.example.androidbighomework.R
import kotlin.random.Random

@SuppressLint("DiscouragedApi")
public fun GetRandomPicture(): Int {
    val ranId = Random(System.currentTimeMillis()).nextInt(5)
    val imageList = listOf<Int>(
        R.drawable.bg1,
        R.drawable.bg2,
        R.drawable.bg3,
        R.drawable.bg4,
        R.drawable.bg5
    )
    return imageList[ranId]
}

// 根据当前秒获取分钟和秒的字符串
public fun getCurrentTime(sec: Int): String {
    // 要前导零
    val second = sec % 60
    val minute = (sec / 60)
    val secondStr = when (second) {
        in 0..9 -> {
            "0$second"
        }
        else -> {
            "$second"
        }
    }
    val minuteStr = when (minute) {
        in 0..9 -> {
            "0$minute"
        }
        else -> {
            "$minute"
        }
    }
    return "$minuteStr:$secondStr"
}