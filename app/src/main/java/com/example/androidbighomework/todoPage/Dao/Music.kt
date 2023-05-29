package com.example.androidbighomework.todoPage.Dao

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Musics")
data class Music (
    @PrimaryKey(autoGenerate = true) var id: Long,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "uri") var uri: String,
    @ColumnInfo(name = "is_custom") var is_custom: Int,
)