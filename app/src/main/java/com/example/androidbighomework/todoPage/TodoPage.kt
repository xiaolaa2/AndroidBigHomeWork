package com.example.androidbighomework.todoPage

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import com.example.androidbighomework.R
import com.example.androidbighomework.Theme.MyTheme

class TodoPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo_page)
        // hide actionBar
        supportActionBar?.hide()
    }

    override fun onStart() {
        super.onStart()
        initCompose()
    }

    private fun initCompose() {
        findViewById<ComposeView>(R.id.compose_view).setContent {
            // 顶部的大背景
            MyTheme {
                Column(
                    modifier = Modifier
                        .background(color = MyTheme.colors.background[1])
                        .fillMaxSize()
                ) {
                    // 顶部ActionBar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                vertical = MyTheme.elevation.topTitlePadding,
                                horizontal = MyTheme.elevation.sidePadding
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "待办",
                            style = MyTheme.typography.topTitle
                        )
                        // IconList
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(
                                onClick = {

                                }
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    modifier = Modifier.size(MyTheme.size.actionBarIcon),
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                            IconButton(
                                onClick = {

                                }
                            ) {
                                Icon(
                                    Icons.Default.Settings,
                                    modifier = Modifier
                                        .size(MyTheme.size.actionBarIcon),
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }

                            IconButton(
                                onClick = {

                                }
                            ) {
                                Icon(
                                    Icons.Default.Menu,
                                    modifier = Modifier
                                        .size(MyTheme.size.actionBarIcon),
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // 正文内容
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(shape = RoundedCornerShape(MyTheme.size.roundedCorner))
                            .background(color = Color.White)
                            .padding(horizontal = MyTheme.elevation.sidePadding),
                    ) {
                        Spacer(modifier = Modifier.height(40.dp))
                        Text(text = "待办事项", style = MyTheme.typography.title)
                        Spacer(modifier = Modifier.height(MyTheme.elevation.contentPadding))
                        Text(
                            "待办是指您要专注的事，点击卡片\"+\"添加，长按待办编辑删除。",
                            style = MyTheme.typography.regularText
                        )
                        Spacer(modifier = Modifier.height(MyTheme.elevation.contentPadding))

                        // 待办列表
                        LazyColumn() {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(shape = RoundedCornerShape(MyTheme.size.roundedCorner))
                                        .background(MyTheme.colors.background[1])
                                        .padding(horizontal = 20.dp, vertical = 10.dp)
                                        .pointerInput(Unit) {
                                            detectTapGestures(onTap = {
                                                Log.d("Todo", "Todo was clicked")
                                            })
                                        }
                                    ,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxHeight().width(0.dp).weight(1f),
                                    ) {
                                         Text(text = "待办是您要专注的事情", style = MyTheme.typography.todoListTitle)
                                         Spacer(modifier = Modifier.height(20.dp))
                                         Text(text = "1分钟", style = MyTheme.typography.todoListText)
                                    }
                                    Column(
                                        modifier = Modifier,
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        TextButton(onClick = { /*TODO*/ }) {
                                            Text(text = "开始", style = MyTheme.typography.textButton)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}