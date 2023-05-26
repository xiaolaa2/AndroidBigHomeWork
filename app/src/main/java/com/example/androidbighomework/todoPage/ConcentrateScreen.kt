package com.example.androidbighomework.todoPage

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.androidbighomework.R
import com.example.androidbighomework.Theme.MyTheme
import kotlinx.coroutines.delay

class ConcentrateScreen : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_concentrate_screen)
        supportActionBar?.hide()
        initCompose()
    }

    // 根据当前秒获取分钟和秒的字符串
    private fun getCurrentTime(sec: Int): String {
        // 要前导零
        val second = sec % 60
        val minute = (sec / 60).toInt()
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

    @RequiresApi(Build.VERSION_CODES.P)
    private fun initCompose() {
        requireViewById<ComposeView>(R.id.compose_view2).setContent {
            MyTheme {
                // 当前的背景图片
                val nowBgPic by remember {
                    mutableStateOf(R.drawable.bg3)
                }
                // 是否开始计时、倒计时
                var isCount by remember {
                    mutableStateOf(true)
                }
                // 当前已经计时的时间
                var nowCountTime by remember {
                    mutableStateOf(0)
                }
                // 打开暂停对话框
                var dialogVisible by remember {
                    mutableStateOf(false)
                }
                // 打开暂停对话框
                var stopDialogVisible by remember {
                    mutableStateOf(false)
                }
                val totalTime = 121

                // 计时器
                if (isCount) {
                    LaunchedEffect(isCount) {
                        repeat(totalTime) {
                            nowCountTime++
                            delay(1000)   // 延迟一秒
                        }
                    }
                } else {
                    Toast.makeText(this, "计时停止", Toast.LENGTH_SHORT).show()
                }

                if (nowCountTime == totalTime) {
                    isCount = false
                    Toast.makeText(this, "时间到啦", Toast.LENGTH_SHORT).show()
                }

                // 停止弹窗
                MyDialog(
                    dialogVisible = stopDialogVisible,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(MyTheme.size.roundedCorner))
                        .background(color = Color.White),
                    onClose = {
                        stopDialogVisible = false
                    },
                    title = {
                            Text(
                                text = "添加待办",
                                style = MyTheme.typography.todoListTitle.copy(
                                    color = Color(0xff303133)
                                )
                            )
                    },
                    content = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("退出当前计时")
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("退出当前计时")
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("取消")
                            }
                        }
                    }
                )

                // 暂停弹框
                MyDialog(
                    dialogVisible = dialogVisible,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(MyTheme.size.roundedCorner))
                        .background(color = Color.White),
                    onClose = {
                        dialogVisible = false
                    },
                    title = {
                            Text(
                                text = "添加待办",
                                style = MyTheme.typography.todoListTitle.copy(
                                    color = Color(0xff303133)
                                )
                            )
                    },
                    content = {

                    }
                )

                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxSize()
                        .paint(
                            painter = painterResource(id = nowBgPic),
                            contentScale = ContentScale.Crop,
                        )
                ) {
                    val (countTimeText, topActionBar, controlBar) = createRefs()
                    // 顶部的ActionBar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MyTheme.elevation.sidePadding)
                            .constrainAs(topActionBar) {
                                top.linkTo(parent.top, margin = 10.dp)
                            },
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                modifier = Modifier.size(MyTheme.size.actionBarIcon),
                                imageVector = Icons.Default.Close,
                                tint = MyTheme.colors.actionBarIcon,
                                contentDescription = "关闭专注页返回待办页面"
                            )
                        }
                        Row() {
                            IconButton(onClick = { /*TODO*/ }) {
                                Icon(
                                    modifier = Modifier.size(MyTheme.size.actionBarIcon),
                                    imageVector = Icons.Default.Edit,
                                    tint = MyTheme.colors.actionBarIcon,
                                    contentDescription = null
                                )
                            }
                            IconButton(onClick = { /*TODO*/ }) {
                                Icon(
                                    modifier = Modifier.size(MyTheme.size.actionBarIcon),
                                    imageVector = Icons.Default.Share,
                                    tint = MyTheme.colors.actionBarIcon,
                                    contentDescription = null
                                )
                            }
                            IconButton(onClick = { /*TODO*/ }) {
                                Icon(
                                    modifier = Modifier.size(MyTheme.size.actionBarIcon),
                                    imageVector = Icons.Default.Menu,
                                    tint = MyTheme.colors.actionBarIcon,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                    // 显示当前时长
                    Text(
                        text = getCurrentTime(nowCountTime),
                        modifier = Modifier
                            .constrainAs(countTimeText) {
                                linkTo(
                                    start = parent.start,
                                    top = parent.top,
                                    bottom = parent.bottom,
                                    end = parent.end,
                                    horizontalBias = 0.5f,
                                    verticalBias = 0.5f
                                )
                            },
                        style = MyTheme.typography.todoListTitle.copy(
                            fontSize = 46.sp,
                            shadow = MyTheme.shadow.toDoListText
                        )
                    )

                    // 控制栏
                    Row(
                        modifier = Modifier
                            .constrainAs(controlBar) {
                                // 在底部
                                bottom.linkTo(parent.bottom, margin = 100.dp)
                                // 水平居中
                                centerHorizontallyTo(parent, 0.5f)
                            },
                        horizontalArrangement = Arrangement.spacedBy(15.dp)  // 图标的间隔
                    ) {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                modifier = Modifier.size(MyTheme.size.actionBarIcon),
                                imageVector = Icons.Default.Brightness5,
                                tint = MyTheme.colors.actionBarIcon,
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                modifier = Modifier.size(MyTheme.size.actionBarIcon),
                                imageVector = Icons.Default.LibraryMusic,
                                tint = MyTheme.colors.actionBarIcon,
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = {
                            isCount = false
                            dialogVisible = true
                        }) {
                            Icon(
                                modifier = Modifier.size(MyTheme.size.actionBarIcon),
                                imageVector = Icons.Default.Pause,
                                tint = MyTheme.colors.actionBarIcon,
                                contentDescription = "暂停计时"
                            )
                        }
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                modifier = Modifier.size(MyTheme.size.actionBarIcon),
                                imageVector = Icons.Default.Refresh,
                                tint = MyTheme.colors.actionBarIcon,
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = {
                            stopDialogVisible = true
                        }) {
                            Icon(
                                modifier = Modifier.size(MyTheme.size.actionBarIcon),
                                imageVector = Icons.Default.Stop,
                                tint = MyTheme.colors.actionBarIcon,
                                contentDescription = "停止计时"
                            )
                        }
                    }

                }
            }
        }
    }

    @Composable
    private fun MyDialog(
        dialogVisible: Boolean,
        modifier: Modifier,
        onClose: ()->Unit,
        title: @Composable ()->Unit,
        content: @Composable ()->Unit,
    ) {
        // 添加待办弹窗
        AnimatedVisibility(
            modifier = Modifier
                .zIndex(1f),
            visible = dialogVisible,
            enter = fadeIn(animationSpec = tween(durationMillis = 450)),
            exit = fadeOut(animationSpec = tween(durationMillis = 450))
        ) {
            Box(modifier = Modifier) {
                // 遮罩层最大
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 60.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {

                            })
                        }
                ) {

                }

                // 内容部分
                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(2f)
                ) {
                    val dialogBody = createRef()
                    // 渐入渐出动画
                    AnimatedVisibility(
                        modifier = Modifier.constrainAs(dialogBody) {
                            centerTo(parent)
                        },
                        visible = dialogVisible,
                        enter = fadeIn(animationSpec = tween(durationMillis = 550)),
                        exit = fadeOut(animationSpec = tween(durationMillis = 550))
                    ) {
                        // 弹出框本身
                        Column(
                            modifier = modifier
                        ) {
                            // 标题栏和按钮
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .height(60.dp)
                                    .fillMaxWidth()
                                    .background(
                                        Color(0xffE6E8EB)
                                    )
                                    .padding(horizontal = MyTheme.elevation.sidePadding)
                            ) {
                                title()
                                Row() {
                                    // 提交按钮
                                    IconButton(onClick = {
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Done,
                                            contentDescription = null
                                        )
                                    }
                                    IconButton(onClick = {
                                        onClose()
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null
                                        )
                                    }
                                }
                            }

                            // 底部内容
                            content()
                        }
                    }
                }
            }
        }
    }
}