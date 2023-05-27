package com.example.androidbighomework.todoPage

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibratorManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.androidbighomework.R
import com.example.androidbighomework.Theme.MyTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

class ConcentrateScreen : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_concentrate_screen)
        supportActionBar?.hide()
        initCompose()
    }

    @SuppressLint("DiscouragedApi")
    private fun GetRandomPicture(): Int {
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

    @SuppressLint("ServiceCast")
    @RequiresApi(Build.VERSION_CODES.S)
    private fun initCompose() {
        requireViewById<ComposeView>(R.id.compose_view2).setContent {
            var countState by remember {
                mutableStateOf("倒计时")
            }
            // 页面切换加动画
            Crossfade(targetState = countState) {page ->
                when(page) {
                    "倒计时" -> CountDown(changePageBreak = {countState="休息中"})
                    "正向计时" -> ForwardTiming()
                    "休息中" -> BreakTime()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @Composable
    private fun BreakTime() {
          MyTheme {
            // 当前的背景图片
            val nowBgPic by remember {
                mutableStateOf(GetRandomPicture())
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
            // 判断是否已经到达时间
            var hadStop by remember {
                mutableStateOf(false)
            }
            //待办名称
            val todoNameText by remember {
                mutableStateOf("这是一个待办")
            }
            val breakTime = 2400
            //震动马达
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator

            // 计时器
            if (isCount) {
                LaunchedEffect(isCount) {
                    repeat(breakTime) {
                        nowCountTime++
                        delay(1000)   // 延迟一秒
                    }
                }
            } else {
//                    Toast.makeText(this, "计时停止", Toast.LENGTH_SHORT).show()
            }

            // 时间到了
            if (nowCountTime == breakTime && !hadStop) {
                isCount = false
                Toast.makeText(this, "时间到啦", Toast.LENGTH_SHORT).show()
                hadStop = true
                // 响1秒，停0.5秒，重复播放震动
//                    vibrator.vibrate(
//                        VibrationEffect.createWaveform(longArrayOf(0, 1000, 0, 500),
//                        intArrayOf(0, 255, 0, 255),
//                        1))
            }

            // 停止弹窗
            MyDialog(
                dialogVisible = stopDialogVisible,
                modifier = Modifier
                    .fillMaxWidth(0.75f)   // 设置大小为父元素的75%
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
                    Column(modifier = Modifier.width(200.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    Log.d("test", "was cick")
                                }
                                .padding(top = 20.dp, bottom = 20.dp, start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("放弃当前计时", style = MyTheme.typography.regularText.copy(color = Color.Red))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { }
                                .padding(top = 20.dp, bottom = 20.dp, start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("退出当前计时", style = MyTheme.typography.regularText)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {

                                }
                                .padding(top = 20.dp, bottom = 20.dp, start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("取消", style = MyTheme.typography.regularText)
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
                    isCount = true
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
                val (
                    progressBar,
                    todoStatusText,
                    todoName,
                    countTimeText,
                    topActionBar,
                    controlBar) = createRefs()


                val current_progress = remember(nowCountTime) {
                    derivedStateOf { (breakTime - nowCountTime) / breakTime.toFloat() }
                }
                val initialProgressBarSize = 300f

                // 进度条的动画显示
                val transition = rememberInfiniteTransition()
                val progressSize = transition.animateFloat(
                    initialValue = initialProgressBarSize,
                    targetValue = initialProgressBarSize + 30f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1500),
                        repeatMode = RepeatMode.Restart
                    )
                )
                val progressAlpha = transition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1500),
                        repeatMode = RepeatMode.Restart
                    )
                )
                val strokeSize = 6f

                // 加一点遮罩背景
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)))

                // 外部的倒计时进度条
                Canvas(modifier = Modifier.constrainAs(progressBar) {
                    centerTo(parent)
                }) {

                    // 底部圈
                    drawArc (
                        color = Color.White,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        size = Size(width = progressSize.value, height = progressSize.value),
                        topLeft = Offset(x = -(progressSize.value / 2), y = -(progressSize.value / 2)),
                        style = Stroke(width = strokeSize),
                        alpha = progressAlpha.value
                    )
                    // 白色色进度条
                    drawArc (
                        color = Color.White,
                        startAngle = -90f,
                        sweepAngle = current_progress.value * 360f,
                        useCenter = false,
                        size = Size(width = initialProgressBarSize, height = initialProgressBarSize),
                        topLeft = Offset(x = -(initialProgressBarSize / 2), y = -(initialProgressBarSize / 2)),
                        style = Stroke(width = strokeSize),
                    )
                }

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
                    text = getCurrentTime(breakTime - nowCountTime),
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

                val todoNameTextMargin = with(LocalDensity.current){
                    initialProgressBarSize.toDp() / 2 + 30.dp
                }

                // todoNameText
                Text(
                    text = todoNameText,
                    modifier = Modifier.constrainAs(todoName){
                        top.linkTo(progressBar.bottom, margin = todoNameTextMargin)
                        centerHorizontallyTo(parent)
                    },
                    style = MyTheme.typography.todoListTitle.copy(shadow = MyTheme.shadow.toDoListText))

                // todoStatusText
                Text(
                    text = "休息中",
                    modifier = Modifier.constrainAs(todoStatusText){
                        top.linkTo(todoName.bottom, margin = 8.dp)
                        centerHorizontallyTo(parent)
                    },
                    style = MyTheme.typography.todoListText.copy(
                        shadow = MyTheme.shadow.toDoListText,
                    ))

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

    @RequiresApi(Build.VERSION_CODES.S)
    @Composable
    private fun CountDown(
        changePageBreak: () -> Unit
    ){
         MyTheme {
            // 当前的背景图片
            val nowBgPic by remember {
                mutableStateOf(GetRandomPicture())
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
            // 判断是否已经到达时间
            var hadStop by remember {
                mutableStateOf(false)
            }
            //待办名称
            val todoNameText by remember {
                mutableStateOf("这是一个待办")
            }
            val totalTime = 10
            //震动马达
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator

            // 计时器
            if (isCount) {
                LaunchedEffect(isCount) {
                    repeat(totalTime) {
                        nowCountTime++
                        delay(1000)   // 延迟一秒
                    }
                }
            } else {
//                    Toast.makeText(this, "计时停止", Toast.LENGTH_SHORT).show()
            }

            // 时间到了
            if (nowCountTime == totalTime && !hadStop) {
                isCount = false
                Toast.makeText(this, "时间到啦", Toast.LENGTH_SHORT).show()
                hadStop = true
                // 响1秒，停0.5秒，重复播放震动
//                    vibrator.vibrate(
//                        VibrationEffect.createWaveform(longArrayOf(0, 1000, 0, 500),
//                        intArrayOf(0, 255, 0, 255),
//                        1))
                changePageBreak()
            }

            // 停止弹窗
            MyDialog(
                dialogVisible = stopDialogVisible,
                modifier = Modifier
                    .fillMaxWidth(0.75f)   // 设置大小为父元素的75%
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
                    Column(modifier = Modifier.width(200.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    Log.d("test", "was cick")
                                }
                                .padding(top = 20.dp, bottom = 20.dp, start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("放弃当前计时", style = MyTheme.typography.regularText.copy(color = Color.Red))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { }
                                .padding(top = 20.dp, bottom = 20.dp, start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("退出当前计时", style = MyTheme.typography.regularText)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {

                                }
                                .padding(top = 20.dp, bottom = 20.dp, start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("取消", style = MyTheme.typography.regularText)
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
                    isCount = true
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
                val (
                    progressBar,
                    todoStatusText,
                    todoName,
                    countTimeText,
                    topActionBar,
                    controlBar) = createRefs()


                val current_progress = remember(nowCountTime) {
                    derivedStateOf { (totalTime - nowCountTime) / totalTime.toFloat() }
                }
                val initialProgressBarSize = 300f

                // 进度条的动画显示
                val transition = rememberInfiniteTransition()
                val progressSize = transition.animateFloat(
                    initialValue = initialProgressBarSize,
                    targetValue = initialProgressBarSize + 30f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1500),
                        repeatMode = RepeatMode.Restart
                    )
                )
                val progressAlpha = transition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1500),
                        repeatMode = RepeatMode.Restart
                    )
                )
                val strokeSize = 6f

                // 加一点遮罩背景
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)))

                // 外部的倒计时进度条
                Canvas(modifier = Modifier.constrainAs(progressBar) {
                    centerTo(parent)
                }) {

                    // 底部圈
                    drawArc (
                        color = Color.White,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        size = Size(width = progressSize.value, height = progressSize.value),
                        topLeft = Offset(x = -(progressSize.value / 2), y = -(progressSize.value / 2)),
                        style = Stroke(width = strokeSize),
                        alpha = progressAlpha.value
                    )
                    // 白色色进度条
                    drawArc (
                        color = Color.White,
                        startAngle = -90f,
                        sweepAngle = current_progress.value * 360f,
                        useCenter = false,
                        size = Size(width = initialProgressBarSize, height = initialProgressBarSize),
                        topLeft = Offset(x = -(initialProgressBarSize / 2), y = -(initialProgressBarSize / 2)),
                        style = Stroke(width = strokeSize),
                    )
                }

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
                    text = getCurrentTime(totalTime - nowCountTime),
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

                val todoNameTextMargin = with(LocalDensity.current){
                    initialProgressBarSize.toDp() / 2 + 30.dp
                }

                // todoNameText
                Text(
                    text = todoNameText,
                    modifier = Modifier.constrainAs(todoName){
                        top.linkTo(progressBar.bottom, margin = todoNameTextMargin)
                        centerHorizontallyTo(parent)
                    },
                    style = MyTheme.typography.todoListTitle.copy(shadow = MyTheme.shadow.toDoListText))

                // todoStatusText
                Text(
                    text = if(isCount) "进行中" else "已暂停",
                    modifier = Modifier.constrainAs(todoStatusText){
                        top.linkTo(todoName.bottom, margin = 8.dp)
                        centerHorizontallyTo(parent)
                    },
                    style = MyTheme.typography.todoListText.copy(
                        shadow = MyTheme.shadow.toDoListText,
                    ))

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

    // 正向计时的界面
    @RequiresApi(Build.VERSION_CODES.S)
    @Composable
    private fun ForwardTiming() {
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
            // 判断是否已经到达时间
            var hadStop by remember {
                mutableStateOf(false)
            }
            //待办名称
            var todoNameText by remember {
                mutableStateOf("这是一个待办")
            }
            val totalTime = 10
            //震动马达
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator

            // 计时器
            if (isCount) {
                LaunchedEffect(isCount) {
                    repeat(totalTime) {
                        nowCountTime++
                        delay(1000)   // 延迟一秒
                    }
                }
            } else {
//                    Toast.makeText(this, "计时停止", Toast.LENGTH_SHORT).show()
            }

            // 停止弹窗
            MyDialog(
                dialogVisible = stopDialogVisible,
                modifier = Modifier
                    .fillMaxWidth(0.75f)   // 设置大小为父元素的75%
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
                    Column(modifier = Modifier.width(200.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    Log.d("test", "was cick")
                                }
                                .padding(top = 20.dp, bottom = 20.dp, start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("放弃当前计时", style = MyTheme.typography.regularText.copy(color = Color.Red))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { }
                                .padding(top = 20.dp, bottom = 20.dp, start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("退出当前计时", style = MyTheme.typography.regularText)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {

                                }
                                .padding(top = 20.dp, bottom = 20.dp, start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("取消", style = MyTheme.typography.regularText)
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
                    isCount = false
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
                val (todoStatusText, todoName, countTimeText, topActionBar, controlBar) = createRefs()
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

                // todoNameText
                Text(
                    text = todoNameText,
                    modifier = Modifier.constrainAs(todoName){
                        top.linkTo(countTimeText.bottom, margin = 50.dp)
                        centerHorizontallyTo(parent)
                    },
                    style = MyTheme.typography.todoListTitle.copy(shadow = MyTheme.shadow.toDoListText))

                // todoNameText
                Text(
                    text = if(isCount) "进行中" else "已暂停",
                    modifier = Modifier.constrainAs(todoStatusText){
                        top.linkTo(todoName.bottom, margin = 20.dp)
                        centerHorizontallyTo(parent)
                    },
                    style = MyTheme.typography.todoListTitle.copy(shadow = MyTheme.shadow.toDoListText))

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