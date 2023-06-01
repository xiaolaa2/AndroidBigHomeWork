package com.example.androidbighomework

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import androidx.sqlite.db.SupportSQLiteQueryBuilder

class MyContentProvider : ContentProvider() {

    private val authority = "com.example.androidbighomework"

    enum class table {
        Musics, todos
    }

    private val uriMatcher by lazy {
        val matcher = UriMatcher(UriMatcher.NO_MATCH)
        matcher.addURI(authority, "todos", table.todos.ordinal)
        matcher.addURI(authority, "Musics", table.Musics.ordinal)
        matcher
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        // 不提供删除
        TODO("Implement this to handle requests to delete one or more rows")
    }

    override fun getType(uri: Uri): String? {
        return when(uriMatcher.match(uri)) {
            table.todos.ordinal -> "vnd.android.cursor.dir/vnd.com.example.androidbighomework.provider.todos"
            table.Musics.ordinal -> "vnd.android.cursor.dir/vnd.com.example.androidbighomework.provider.Musics"
            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        // 不提供插入
        TODO(
            "Implement this to handle requests for the MIME type of the data" +
                    "at the given URI"
        )
    }

    override fun onCreate(): Boolean {
        TODO("Implement this to initialize your content provider on startup.")
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        val openHelper = MyApplication.db.openHelper
        val cursor: Cursor? = when(uriMatcher.match(uri)) {
            table.todos.ordinal -> {
                openHelper.writableDatabase.query(
                    SupportSQLiteQueryBuilder.builder("todos")
                        .selection(selection, selectionArgs)
                        .columns(projection)
                        .orderBy(sortOrder)
                        .create()
                )
            }
            table.Musics.ordinal -> {
                openHelper.writableDatabase.query(
                    SupportSQLiteQueryBuilder.builder("Musics")
                        .selection(selection, selectionArgs)
                        .columns(projection)
                        .orderBy(sortOrder)
                        .create()
                )
            }
            else -> null
        }
        return cursor
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        // 不提供更新服务
        TODO("Implement this to handle requests to update one or more rows.")
    }
}