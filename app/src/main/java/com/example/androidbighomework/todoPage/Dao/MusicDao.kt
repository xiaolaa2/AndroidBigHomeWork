package com.example.androidbighomework.todoPage.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MusicDao {
    // 添加一首自定义音乐
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addMusic(music: Music): Long

    // 获取所有的音乐
    @Query("select * from Musics")
    fun getAllMusic(): Array<Music>
}