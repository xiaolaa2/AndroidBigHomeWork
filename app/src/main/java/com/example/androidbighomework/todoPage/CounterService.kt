package com.example.androidbighomework.todoPage

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.androidbighomework.MyApplication
import com.example.androidbighomework.R
import kotlinx.coroutines.*

class CounterService : Service() {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        Log.d("CountDownService", "倒计时Service开始")
        val notificationChannel: NotificationChannel = NotificationChannel(
            "CountDownService",
            "CountDownService",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        @SuppressLint("UnspecifiedImmutableFlag")
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, Intent(this, ConcentrateScreen::class.java), PendingIntent.FLAG_IMMUTABLE)
        MyApplication.appStatus.value = AppStatus.Background
        notificationManager.createNotificationChannel(notificationChannel)
        val notification = NotificationCompat.Builder(applicationContext, "CountDownService")
            .setSmallIcon(R.drawable.home)
            .setOngoing(true)
            .setContentTitle(
                when (MyApplication.todoStatus.value) {
                    ConcentrateStatus.Focusing -> "专注中"
                    ConcentrateStatus.Breaking -> "休息中"
                    else -> {
                        ""
                    }
                }
            )
            .setContentText("点击通知返回专注界面")
            .setContentIntent(pendingIntent)
            .build()
        // 发布通知消息
        startForeground(1, notification)
    }

    override fun onDestroy() {
        Log.d("CountDownService", "倒计时Service结束")
        coroutineScope.cancel()   // 取消倒计时的协程任务
        super.onDestroy()
    }

    // 每次调用statService的时候都会调用这个函数，但是只有不存在该Service的时候才会调用OnCreated()
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("CountDownService", "倒计时Service开始")
        val notificationChannel: NotificationChannel = NotificationChannel(
            "CountDownService",
            "CountDownService",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)

        @SuppressLint("UnspecifiedImmutableFlag")
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, Intent(this, ConcentrateScreen::class.java), PendingIntent.FLAG_IMMUTABLE)
        CoroutineScope(Dispatchers.Main).launch {
            // 如果app返回，就停止这个Service
            MyApplication.appStatus.collect {
                if (it == AppStatus.Front) {
                    stopSelf()
                }
            }
        }
        coroutineScope.launch {
            while (MyApplication.countDown.value <= MyApplication.totalTime) {
                MyApplication.countDown.value++
                delay(1000)
            }
            if (MyApplication.countDown.value >= MyApplication.totalTime) {
                val notification =
                    NotificationCompat.Builder(applicationContext, "CountDownService")
                        .setSmallIcon(R.drawable.home)
                        .setOngoing(true)
                        .setContentTitle("时间到")
                        .setContentText("点击通知返回专注界面")
                        .setContentIntent(pendingIntent)
                        .build()
                notificationManager.notify(1, notification)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
}